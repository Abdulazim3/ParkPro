package com.example.parkingapp.Activities

import com.example.parkingapp.Adapters.ReviewAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingapp.DataModels.Review
import com.example.parkingapp.databinding.ActivityReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewBinding
    private val database = FirebaseDatabase.getInstance("insert url here")
    private var carParkId: String? = null
    private var carParkName: String? = null
    private var carParkImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setting up bindings
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    //retrieve data passed from previous activity
        carParkId = intent.getStringExtra("carParkId")
        carParkName = intent.getStringExtra("carParkName")
        carParkImageUrl = intent.getStringExtra("carParkImageUrl")

        // Set the car park name and image
        binding.carParkNameReview.text = carParkName ?: "Car Park"
        Picasso.get().load(carParkImageUrl).into(binding.carParkImageReview)
    //fetching and displays reviews from database
        fetchReviews()
        // set up button click listen for submiting review
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            val reviewText = binding.etReview.text.toString().trim()
            //checks if review is emptyy ybefore submitting
            if (reviewText.isNotBlank()) {
                fetchAndSubmitReview(rating, reviewText)
            } else {
                Toast.makeText(this, "Review cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //fetch user details and submit the review if all requirments are met
    private fun fetchAndSubmitReview(rating: Int, reviewText: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            //fetch users first name
            database.reference.child("Users").child(userId).child("firstname")
                .get()
                .addOnSuccessListener { snapshot ->
                    val firstname = snapshot.getValue(String::class.java) ?: "Anonymous" // set to Anonymous if firstname is not found
                    submitReview(carParkId ?: "", firstname, rating, reviewText)
                }
                .addOnFailureListener {
                    Log.e("ReviewActivity", "Failed to fetch user details", it)
                    submitReview(carParkId ?: "", "Anonymous", rating, reviewText)
                }
        }
    }
    //function to fetch all reviews from datbaase
    private fun fetchReviews() {
        carParkId?.let { id ->
            //fetches all reviews from data base
            database.reference.child("CarParks").child(id).child("reviews")
                .addValueEventListener(object : ValueEventListener {
                    //map data snapshot to review object
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val reviews = mutableListOf<Review>()
                        // mapping datasnapshot to review object
                        dataSnapshot.children.mapNotNullTo(reviews) { it.getValue(Review::class.java) }
                        val adapter = ReviewAdapter(reviews)
                        //set up recyclerview with Review adapte
                        binding.rvReviews.layoutManager = LinearLayoutManager(this@ReviewActivity)
                        binding.rvReviews.adapter = adapter
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@ReviewActivity, "Failed to load reviews: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    //function to submit a review to the database
    private fun submitReview(carParkId: String, firstname: String, rating: Int, reviewText: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentDate = sdf.format(System.currentTimeMillis())
        val review = Review(firstname, rating, reviewText, currentDate)
        val reviewId = database.reference.child("CarParks").child(carParkId).child("reviews").push().key

        reviewId?.let {
            database.reference.child("CarParks").child(carParkId).child("reviews").child(it).setValue(review)
                .addOnSuccessListener {
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                    fetchReviews()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                }
        }
    }



}
