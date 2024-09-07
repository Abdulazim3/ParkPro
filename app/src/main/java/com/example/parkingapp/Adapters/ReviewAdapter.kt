package com.example.parkingapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingapp.R
import com.example.parkingapp.DataModels.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(private val reviewList: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    //initialises relvant layout components
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView = itemView.findViewById(R.id.review_user_name)
        var reviewText: TextView = itemView.findViewById(R.id.review_text_view)
        var reviewDate: TextView = itemView.findViewById(R.id.review_date)
        var ratingBar: RatingBar = itemView.findViewById(R.id.review_rating_bar)
    }
    // inflates the review layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(itemView)
    }
    //bings the review data into the layout
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userName.text = review.userId  // Ideally, you'd fetch the user's name here
        holder.reviewText.text = review.text
        // Formatting the date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        try {
            val parsedDate = inputFormat.parse(review.date)
            val formattedDate = parsedDate?.let { outputFormat.format(it) } ?: "Date Unknown"
            holder.reviewDate.text = formattedDate
        } catch (e: Exception) {
            holder.reviewDate.text = "Date Error"
            e.printStackTrace()
        }
        holder.ratingBar.rating = review.stars.toFloat()
    }

    override fun getItemCount() = reviewList.size
}
