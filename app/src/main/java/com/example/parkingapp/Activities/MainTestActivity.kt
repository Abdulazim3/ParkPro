package com.example.parkingapp.Activities
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import android.location.Geocoder
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.parkingapp.DataModels.CarParks
import com.example.parkingapp.R
import com.example.parkingapp.databinding.ActivityMaintestBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

class MainTestActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMaintestBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleMap: GoogleMap



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("insert url here")

        binding = ActivityMaintestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //find and initialise views for toolbar, drawer layout and navigation view
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
        //calling functions
        fetchWeatherUpdates()
        fetchTrafficUpdates()
        displayMotAndTaxReminders()
        displayStoredVehicleDetails()
        fetchAndSetName()
        setupMap()
    }
    //function to fetch and set username in the header
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

    //function to get weather API and parse
    private fun fetchWeatherUpdates() {
        // creates an instance of OkHttpClient to make HTTP requests
        val client = OkHttpClient()
        //  URL with latitude, longitude, API key, and units
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=51.5072&lon=-0.1276&appid=--insert API key here --&units=metric"
        //  request object with the API endpoint URL
        val request = Request.Builder().url(url).build()
        // enqueue the HTTP request
        client.newCall(request).enqueue(object : Callback {
            // callback function  when the request fails
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            // callback function  when the request receives a response
            override fun onResponse(call: Call, response: Response) {
                // retrieve the response body as a string
                val responseBody = response.body?.string() ?: return
                try {
                    //parse the JSON response string into a JSONObject
                    val json = JSONObject(responseBody)
                    // extract weather related information from the JSON object
                    val weatherArray = json.getJSONArray("weather")
                    //goes into the main, within the weather object
                    val main = json.getJSONObject("main")
                    val weatherCondition = if (weatherArray.length() > 0) weatherArray.getJSONObject(0).getString("main") else ""
                    val description = if (weatherArray.length() > 0) weatherArray.getJSONObject(0).getString("description") else ""
                    val temperature = main.getDouble("temp")
                    val iconCode = if (weatherArray.length() > 0) weatherArray.getJSONObject(0).getString("icon") else ""
                    val rain = if (json.has("rain")) json.getJSONObject("rain").getDouble("1h").toString() + " mm" else "No rain"

                    // get the current date and location
                    val currentDate = getCurrentDate()
                    val location = "London, United Kingdom" // hard coded as ParkPro is only in London currently

                    // format weather information into a readable string
                    val output = """
                    Date: $currentDate
                    $location
              
                    Weather Condition: $weatherCondition
                    Description: $description
                    Temperature: $temperature
                    Rain: $rain
                """.trimIndent()

                    // Update the UI with the weather information
                    runOnUiThread {
                        binding.weatherTextView.text = output
                        // Load the weather icon using Picasso library
                        loadWeatherIcon(iconCode, binding.weatherIconImageView)
                    }//api error handling
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
    // function to load the weather icon using the OpenWeatherMap API
    private fun loadWeatherIcon(iconCode: String, imageView: ImageView) {
        // Construct the URL for the weather icon based on the icon code
        val iconUrl = "https://openweathermap.org/img/wn/$iconCode.png"
        // use Picasso library to load the image from the URL into the imageview holder
        Picasso.get().load(iconUrl).into(imageView)
    }
    //function to get today's date used in weather display
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        return sdf.format(Date())
    }
    //funciton to get TFL API call
    private fun fetchTrafficUpdates() {
        // create okhttpclient instance for making HTTP requests
        val client = OkHttpClient()
        // build request to fetch traffic update
        val request = Request.Builder()
            .url("https://api.tfl.gov.uk/Road/")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // handle network request failure
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                // handle network request success
                response.body?.string()?.let { responseBody ->
                    // parse the JSON response
                    parseTrafficUpdates(responseBody)
                }
            }
        })
    } //parses trafic update, added to distinguish between severity levels and create better format
    private fun parseTrafficUpdates(jsonData: String) {
        try {
            // convert JSON string to JSONArray
            val updatesArray = JSONArray(jsonData)
            // stringBuilder to store formatted traffic updates
            val stringBuilder = SpannableStringBuilder()
            //  colors for traffic severity levels
            val seriousColor = ContextCompat.getColor(applicationContext, R.color.seriousColor)
            val goodColor = ContextCompat.getColor(applicationContext, R.color.goodColor)
            // iterates over each traffic update
            for (i in 0 until updatesArray.length()) {
                val updateObject = updatesArray.getJSONObject(i)
                val displayName = updateObject.getString("displayName")
                val statusSeverity = updateObject.getString("statusSeverity")
                val statusSeverityDescription = updateObject.getString("statusSeverityDescription")
                // create formatted update text
                val updateText =
                    "Road: $displayName\nStatus: $statusSeverity - $statusSeverityDescription\n\n"
                val spannableString = SpannableString(updateText)
                // apply color to text based on severity
                val color = if (statusSeverity == "Good") goodColor else seriousColor
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    updateText.indexOf("Status:"),
                    updateText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // append formatted update to stringbuilder
                stringBuilder.append(spannableString)
            }
            // update traffic updates TextView on the UI thread
            runOnUiThread {
                val trafficUpdatesTextView: TextView = findViewById(R.id.trafficUpdatesTextView)
                trafficUpdatesTextView.text = stringBuilder
            }
        } catch (e: JSONException) {
            // Handle JSON parsing error
            e.printStackTrace()

            // error message if parsing fails
            runOnUiThread {
                findViewById<TextView>(R.id.trafficUpdatesTextView).text =
                    "No Traffic Updates - Error"
            }
        }
    }
    // Function that calls API and retrieves mot and tax due dates only, uses shared preferences to fetch numberplate,

    private fun displayMotAndTaxReminders() {
        //retrieve userid from firebase auth,
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
        //access shareprefernces, stores vehicle info by useriD
        val sharedPreferences = getSharedPreferences("VehicleInfoPreferences_$userId", MODE_PRIVATE)
        val reg = sharedPreferences.getString("reg_$userId", null)
            ?: return // Return if registration number is null
        //setup http client
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "{\"registrationNumber\":\"$reg\"}")
        //prepare request iwth necessary headers and the body
        val request = Request.Builder()
            .url("https://driver-vehicle-licensing.api.gov.uk/vehicle-enquiry/v1/vehicles")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", getString(R.string.vehicle_api_key))
            .build()
        // executes the network call asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""

                try {
                    //parse JSON response to retrieve mot and road tax dates
                    val jsonObject = JSONObject(responseData)
                    val motExpiry = jsonObject.getString("motExpiryDate")
                    val taxDueDate = jsonObject.getString("taxDueDate")

                    // converts the strin dates to localdate using formatter
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val motExpiryDate = LocalDate.parse(motExpiry, formatter)
                    val taxDueDateLocal = LocalDate.parse(taxDueDate, formatter)

                    // calculates the days between currentday and the due dates
                    val daysUntilMOT = ChronoUnit.DAYS.between(LocalDate.now(), motExpiryDate)
                    val daysUntilTax = ChronoUnit.DAYS.between(LocalDate.now(), taxDueDateLocal)
                    //updates UI thread
                    runOnUiThread {
                        binding.motTextView.text = "Days Until MOT Expires: $daysUntilMOT"
                        binding.roadtaxTextView.text = "Days Until Road Tax Expires: $daysUntilTax"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
    //function to displa stored vehicle from SharedPreferences
    private fun displayStoredVehicleDetails() {
        //retrieves user ID from firebase auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
        //acceses share preference specific to each user
        val sharedPreferences = getSharedPreferences("VehicleInfoPreferences_$userId", MODE_PRIVATE)
        //retrieve stored details
        val make = sharedPreferences.getString("make_$userId", " ")
        val colour = sharedPreferences.getString("colour_$userId", " ")
        val reg = sharedPreferences.getString("reg_$userId", " ")

        if (make != null && colour != null && reg != null) {
            val vehicleDetails = "Registration: $reg\nColor: $colour\nMake: $make"
            binding.vehicleDetailsTextView.text = vehicleDetails
        }
    }
    //set up map fragment
    private fun setupMap() {

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map2) as? SupportMapFragment
        // ayschronously get the googlemap object
        mapFragment?.getMapAsync { gMap ->
            googleMap = gMap

            val london = LatLng(51.48306, -0.00646) // Coordinates for UofGreenwichs
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 15f)) // zoom level for map 1 (world view) and 21 (street view)
            fetchCarParksAndAddMarkers()
        }
    }
    //function tofetch car park details and add markers
    private fun fetchCarParksAndAddMarkers() {
        //access CarParks node
        val carParksRef = database.reference.child("CarParks")
        //attach a listen to read car park data
        carParksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //iterate through each car park entry in the snapshot
                snapshot.children.forEach { carParkSnapshot ->
                    //convert the snapshot into a CarParks object
                    val carPark = carParkSnapshot.getValue(CarParks::class.java)
                    //add marker to map
                    carPark?.let { addMarkerForCarPark(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainTestActivity", "Firebase error: ${error.message}")
            }
        })
    }
    //function to add marker to map based on address
    private fun addMarkerForCarPark(carPark: CarParks) {
        ///create geocoder instance to convert addres into latlng
        val geocoder = Geocoder(this)
        geocoder.getFromLocationName(carPark.carparkAddress, 1)?.let { addressList ->
            //if address not found retrieve the first result
            if (addressList.isNotEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                //adds carpark name to each marker
                val markerOptions = MarkerOptions().position(latLng).title(carPark.carparkName)
                googleMap.addMarker(markerOptions)?.tag = carPark
            } else {
                Log.w("MainTestActivity", "Address not found: ${carPark.carparkName}")
            }
        }
    }


}
