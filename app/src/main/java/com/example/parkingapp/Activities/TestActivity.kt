package com.example.parkingapp.Activities

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.parkingapp.DataModels.CarParks
import com.example.parkingapp.Adapters.PlacesAutocompleteAdapter
import com.example.parkingapp.R
import com.example.parkingapp.DataModels.Review
import com.example.parkingapp.databinding.ActivityTestBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.Locale
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle


    private var selectedPlaceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //initiate firebase database and auth
        database = FirebaseDatabase.getInstance("insert url here")
        auth = FirebaseAuth.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        // Setup ActionBarDrawerToggle to make the drawer open and close events
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Ensure the navigation view  set up
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Refresh the page
                    startActivity(Intent(this, MainTestActivity::class.java))
                }

                R.id.nav_settings -> {
                    // Start the SettingsActivity
                    startActivity(Intent(this, SettingsActivity::class.java))
                }

                R.id.nav_vehicle -> {
                    // Start the VehicleActivity
                    startActivity(Intent(this, VehicleActivity::class.java))
                }

                R.id.nav_parking -> {
                    // Start the ParkingActivity
                    startActivity(Intent(this, TestActivity::class.java))
                }

                R.id.nav_logout -> {
                    // Log out the user
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish() // Close the current activity
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }
        //initialises google places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_place_api_key))
        }
        placesClient = Places.createClient(this)
        //initialises a basic map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
        //set the places auto complete adapter
        val autocompleteTextView = binding.placesAutocomplete
        val adapter = PlacesAutocompleteAdapter(this, placesClient)
        autocompleteTextView.setAdapter(adapter)
        autocompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedPlace = adapter.getItem(position)
            selectedPlaceId = selectedPlace?.placeId
            autocompleteTextView.setText(selectedPlace?.getPrimaryText(null).toString())
        }
        //binds search button to move the map to place
        val searchButton: Button = binding.searchButton
        searchButton.setOnClickListener {
            selectedPlaceId?.let { id ->
                fetchLocationAndMoveMap(id)
            }
        }
        fetchAndSetName()

    }
    //retrives location and moves camera
    private fun fetchLocationAndMoveMap(placeId: String) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            moveMapToLocation(place.latLng)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e("TestActivity", "place not found zzz: ${exception.statusCode}")
            }
        }
    }
    //moves camera
    private fun moveMapToLocation(latLng: LatLng?, zoomLevel: Float = 15f) {
        latLng?.let {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, zoomLevel))
        }
    }
    // sets up the car park markers
    private val callback = OnMapReadyCallback { gMap ->
        googleMap = gMap

        // After setting the default location, load the car park markers.
        fetchCarParksAndAddMarkers()
        // listen for window clicks on marker, is carparks object is available, showcarparkoverlay is called
        googleMap.setOnInfoWindowClickListener { marker ->
            val carPark = marker.tag as? CarParks
            carPark?.let {
                showCarParkDetailsOverlay(it)
            }
        }
    }

    // checks database for all carparks and places markers on the map
    private fun fetchCarParksAndAddMarkers() {
        // a reference to the "CarParks" node in the Firebase database
        val carParksRef = database.reference.child("CarParks")

        // listen for single data change event
        carParksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // iterate through each child node (car park) in the received DataSnapshot
                snapshot.children.forEach { carParkSnapshot ->
                    // attempt to parse the data into a CarParks object
                    val carPark = carParkSnapshot.getValue(CarParks::class.java)
                    // if parsing  successful
                    carPark?.let {
                        // add a marker on the map for the retrieved car park data
                        addMarkerForCarPark(it)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // handle database error
                Log.e("TestActivity", "Firebase error: ${error.message}")
            }
        })
    }
    // places marker using the carpark address
    private fun addMarkerForCarPark(carPark: CarParks) {
        // create  Geocoder object to convert car park address to lat and long
        val geocoder = Geocoder(this)
        try {
            // retrieve address
            val carParkAddress = geocoder.getFromLocationName(carPark.carparkAddress, 1)
            if (carParkAddress!!.isNotEmpty()) {
                val address = carParkAddress!![0]
                // extract lat and long f
                val latLng = LatLng(address.latitude, address.longitude)
                // run on the UI thread to update UI elements
                runOnUiThread {
                    // create marker options for the car parks
                    val markerOptions = MarkerOptions().position(latLng).title(carPark.carparkName)
                        .snippet("Tap For More Details")
                    // add marker to the Google Map
                    val marker = googleMap.addMarker(markerOptions)
                    // tag the marker with CarParks object for later retrieval
                    if (marker != null) {
                        marker.tag = carPark
                    }
                    //
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            } else {
                //
                Log.w("TestActivity", "Address not foundd: ${carPark.carparkName}")
            }
        } catch (e: Exception) {
            // error if geocoding fails
            Log.e("TestActivity", "Geocoder failed", e)
        }
    }

    // function to display average rating in each car park
    private fun fetchAndDisplayAverageRating(carPark: CarParks, ratingBar: RatingBar, averageRatingTextView: TextView) {
        //goes to review node in firebase
        val reviewsRef = database.reference.child("CarParks").child(carPark.carparkID).child("reviews")
        //listen to retreieve reviews from database
        reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRating = 0f
                var reviewsCount = 0
            // iterate through child node review
                for (reviewSnapshot in snapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    review?.let {
           // if the review object is not null, calculate the total rating and increment the reviews count
                        totalRating += it.stars
                        reviewsCount++
                    }
                }

                if (reviewsCount > 0) {
                    val averageRating = totalRating / reviewsCount
                    ratingBar.rating = averageRating //set rating bare to average
                    averageRatingTextView.text = String.format(Locale.getDefault(), "%.2f", averageRating)
                } else {
                    // if there are no reviews set to No reviews
                    ratingBar.rating = 0f
                    averageRatingTextView.text = "No Reviews"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TestActivity", "Failed to calculate average rating: ${error.message}")
            }
        })
    }

    //function to display carpark data in the layout
    private fun showCarParkDetailsOverlay(carPark: CarParks) {
        //inflate layout for car parks details dialog xml
        val dialogView = layoutInflater.inflate(R.layout.dialog_car_park_details, null)
        //initilaise views from inflated layout

        val carParkEntranceImageView = dialogView.findViewById<ImageView>(R.id.carParkEntranceImageView)
        val carParkNameTextView = dialogView.findViewById<TextView>(R.id.carParkNameTextView)
        val carParkAddressTextView = dialogView.findViewById<TextView>(R.id.carParkAddressTextView)
        val entranceDescriptionTextView = dialogView.findViewById<TextView>(R.id.entranceDescriptionTextView)
        val restrictionsTextView = dialogView.findViewById<TextView>(R.id.restrictionsTextView)
        val standardBaysTextView = dialogView.findViewById<TextView>(R.id.standardBaysTextView)
        val disabledBaysTextView = dialogView.findViewById<TextView>(R.id.disabledBaysTextView)
        val carParkOpeningTimesTextView = dialogView.findViewById<TextView>(R.id.carParkOpeningTimesTextView)
        val carParkPricesTextView = dialogView.findViewById<TextView>(R.id.carparkPricesTextView)
        val paymentMethodsTextView= dialogView.findViewById<TextView>(R.id.paymentMethodsTextView)

        // binding data from carpark object to the views
        carParkNameTextView.text = carPark.carparkName
        carParkAddressTextView.text = carPark.carparkAddress
        entranceDescriptionTextView.text = carPark.entranceDescription
        restrictionsTextView.text = carPark.restrictions
        standardBaysTextView.text = "Standard Bays: ${carPark.standardBays}"
        disabledBaysTextView.text = "Disabled Bays: ${carPark.disabledBays}"
        paymentMethodsTextView.text = carPark.paymentMethods
        //build strings for opening times and prices
        val orderedDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val openingTimesBuilder = StringBuilder()

        orderedDays.forEach { day ->
            carPark.carparkOpeningTimes[day]?.let { times ->
                openingTimesBuilder.append("$day: $times\n")
            }
        }

        carParkOpeningTimesTextView.text = openingTimesBuilder.toString().trimEnd()

        val pricesBuilder = StringBuilder()
        carPark.carparkPrices.forEach { (duration, price) ->
            pricesBuilder.append("$duration: $price\n")
        }
        carParkPricesTextView.text = pricesBuilder.toString().trimEnd()
        //load carpark entrance pic using picasso
        Picasso.get()
            .load(carPark.entranceImageUrl)
            .into(carParkEntranceImageView)
    // listener for the view reviews button
        val viewReviewsButton: Button = dialogView.findViewById(R.id.btnViewReviews)
        viewReviewsButton.setOnClickListener {
            // Call a function to show the reviews.
            //calls function that opens review activity with specific carpark
            showReviews(carPark)
        }

        carParkEntranceImageView.setOnClickListener {
            showFullSizeImage(carPark.entranceImageUrl)
        }
        //create and show dialog with inflated layout
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        dialog.show()
    // adjusts size of dialog to make it big to show more info
        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        // find and intitilaise views for rating bar and avg rating text view
        val ratingBar: RatingBar = dialogView.findViewById(R.id.averageRatingBar)
        val averageRatingTextView = dialogView.findViewById<TextView>(R.id.averageRatingTextView)

        fetchAndDisplayAverageRating(carPark, ratingBar, averageRatingTextView)



    }

// uses another xml file to only show image
    private fun showFullSizeImage(imageUrl: String?) {
    //inflatte layout for displaying full size image
        val fullSizeView = layoutInflater.inflate(R.layout.carpark_entrance_fullsize_image, null)
        //find the image view in inflated layout
        val fullSizeImageView = fullSizeView.findViewById<ImageView>(R.id.fullSizeImageView)
//load image url that is provied
        Picasso.get().load(imageUrl).into(fullSizeImageView)
 // create an alert dialog to full size image
        val imageDialog = AlertDialog.Builder(this)
            .setView(fullSizeView)
            .setPositiveButton("Close", null) //positive button to close
            .create()

        imageDialog.show()
    }
    private fun showReviews(carPark: CarParks) {
        // this function launches Activity that shows the reviews.

        val intent = Intent(this, ReviewActivity::class.java)
        intent.putExtra("carParkId", carPark.carparkID) // ar park ID
        intent.putExtra("carParkName", carPark.carparkName) //  car park name
        intent.putExtra("carParkImageUrl", carPark.entranceImageUrl) // entrance image URL
        startActivity(intent)
        }
    private fun fetchAndSetName() {
        //retrieve current authenticated user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.reference.child("Users").child(userId) //access the Users node in Database
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val firstname = snapshot.child("firstname").getValue(String::class.java).orEmpty()
                        // capitalise the first letter of firstname
                        val formattedName = firstname.capitalize()
                        if (formattedName.isNotEmpty()) {
                            // set username in the header
                            val textViewUserName = findViewById<TextView>(R.id.textViewHeaderName)
                            textViewUserName.text = "Welcome $formattedName" //add Welcome before name
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // handle possible errors.
                    }
                })
        }
    }
}








