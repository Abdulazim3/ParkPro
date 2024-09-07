package com.example.parkingapp.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.parkingapp.DataModels.MainVehicleInfo
import com.example.parkingapp.R
import com.example.parkingapp.DataModels.Vehicles
import com.example.parkingapp.databinding.ActivityVehicleBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class VehicleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityVehicleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var retrievedNumberPlate: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initilaise firebase auth and database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("insert url here")


        binding.numberPlateEntryButton.setOnClickListener{
            saveVehicle()

            Handler(Looper.getMainLooper()).postDelayed({
                // intent to restart the VehicleActivity
                val restartIntent = Intent(this, VehicleActivity::class.java)
                startActivity(restartIntent)

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }, 1000) //1 sec
        }


        // Toolbar setup
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

        fetchNumberPlate()
        fetchAndSetName()




    }
    //saves vehicle to firebase
    private fun saveVehicle() {
        // takes current user id
        val currentUser = auth.currentUser
        val userID = currentUser?.uid

        val numberPlate = binding.numberPlateEntry.text.toString().trim()
        if (numberPlate.isEmpty()) {
            Toast.makeText(this, "Please enter a number plate", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the user already has a vehicle
        database.reference.child("Vehicles").orderByChild("userID").equalTo(userID)
            .addListenerForSingleValueEvent(object : ValueEventListener {   //listen to check for vehicle in database
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Displays toasts if user also has vehicle in database
                        Toast.makeText(
                            this@VehicleActivity, "You can only add one vehicle", Toast.LENGTH_SHORT).show()
                        binding.root.postDelayed({
                            Toast.makeText(
                                this@VehicleActivity, "Go to settings to change vehicle", Toast.LENGTH_SHORT).show()
                        }, 1000
                        )

                    } else {
                        // User doesn't have a vehicle registered, proceed to save the new one
                        if (userID != null) {
                            addNewVehicle(userID, numberPlate)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@VehicleActivity,
                        "Database error: ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    // add a new vehicle for the first time
    private fun addNewVehicle(userID: String, numberPlate: String) {
        // generate a unique ID for the vehicle
        val vehicleID = database.reference.child("Vehicles").push().key ?: ""

        val vehicle = Vehicles(numberPlate, vehicleID, userID)

        // save vehicle to Firebase
        database.reference.child("Vehicles").child(vehicleID).setValue(vehicle)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Vehicle saved successfully", Toast.LENGTH_SHORT).show()
                } else {

                    Toast.makeText(this, "Failed to save vehicle", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // fetches number plate from database
    private fun fetchNumberPlate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.reference.child("Vehicles")
                .orderByChild("userID").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (vehicleSnapshot in dataSnapshot.children) {
                            retrievedNumberPlate =
                                vehicleSnapshot.child("number_plate").getValue(String::class.java)

                            break
                        }
                        getVehicleDetails()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
        }

    }
    //function to get vehicle detail from API
    private fun getVehicleDetails() {
        //initiliase okHttpClient to send network requests
        val client = OkHttpClient()
        val numberplate = retrievedNumberPlate //numberplate retrieved
        // build the http request
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "{\"registrationNumber\":\"$numberplate\"}")
        val request = Request.Builder()
            .url("https://driver-vehicle-licensing.api.gov.uk/vehicle-enquiry/v1/vehicles")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", getString(R.string.vehicle_api_key)) // API KEY
            .build()
        // asynchronously send request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { // error handling

                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""

                // Parse the JSON response
                try {
                    val jsonObject = JSONObject(responseData)
                    val make = jsonObject.getString("make")
                    val colour = jsonObject.getString("colour")
                    val co2Emissions = jsonObject.getString("co2Emissions")
                    val fueltype = jsonObject.getString("fuelType")
                    val MOTstatus = jsonObject.getString("motStatus")
                    val plate = jsonObject.getString("registrationNumber")
                    val taxduedate = jsonObject.getString("taxDueDate")
                    val taxStatus = jsonObject.getString("taxStatus")
                    val MOTExpiry = jsonObject.getString("motExpiryDate")
                    val enginecapacity = jsonObject.getString("engineCapacity")
                    val carYear = jsonObject.getString("yearOfManufacture")


                    //bind all values into UI thread
                    runOnUiThread {
                        binding.MakeTV.text = make
                        binding.ColourTV.text = colour
                        binding.C02EmissionsTV.text = co2Emissions
                        binding.FuelTypeTV.text = fueltype
                        binding.MOTTV.text = MOTstatus
                        binding.NumberPlateTV.text = plate
                        binding.TaxDateTV.text = taxduedate
                        binding.TaxStatusTV.text = taxStatus
                        binding.MOTExpiryTV.text = MOTExpiry
                        binding.EngineCapacityTV.text = enginecapacity
                        binding.YearTV.text = carYear
                    }
                    val vehicleInfo = MainVehicleInfo(make, colour, plate)
                    storeVehicleDetailsInPreferences(vehicleInfo)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
    //store in preferences so other activies can access information
    private fun storeVehicleDetailsInPreferences(vehicleInfo: MainVehicleInfo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
        val sharedPreferences = getSharedPreferences("VehicleInfoPreferences_$userId", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("make_$userId", vehicleInfo.make)
            putString("colour_$userId", vehicleInfo.colour)
            putString("reg_$userId", vehicleInfo.reg)
            apply()
        }
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



























































//    private fun getVehicleDetails() {
//        // Check if retrievedNumberPlate is not null and not empty
//        val registrationNumber = retrievedNumberPlate.takeIf { !it.isNullOrBlank() } ?: return
//
//        val client = OkHttpClient()
//
//        val mediaType = "application/json; charset=utf-8".toMediaType()
//        val requestBody = "{\"registrationNumber\":\"$registrationNumber\"}".toRequestBody(mediaType)
//
//        val request = Request.Builder()
//            .url("https://driver-vehicle-licensing.api.gov.uk/vehicle-enquiry/v1/vehicles")
//            .addHeader("x-api-key", "7zEbS3Ba4caUSrTQQswiF3cZmakZ9V2TaOVwA3Vb") // Replace with your actual API key
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("HTTP_ERROR", "Failed to send request: $e")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (!response.isSuccessful) {
//                    Log.e("HTTP_ERROR", "Request failed with code: ${response.code}")
//                    return
//                }
//                val responseBody = response.body?.string() ?: return // Handle null body
//                Log.d("HTTP_SUCCESS", "Response: $responseBody")
//                val jsonObject = JSONObject(responseBody)
//                val make = jsonObject.getString("make")
//                val colour = jsonObject.getString("colour")
//
//                runOnUiThread {
//                    binding.MakeInput.text = make
//                    binding.colourInput.text = colour
//                }
//            }
//        })
//    }


    // Other class content





//    private fun fetchNumberPlate() {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            val userId = currentUser.uid
//            database.reference.child("Vehicles")
//                .orderByChild("userID").equalTo(userId)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        var found = false
//                        for (vehicleSnapshot in dataSnapshot.children) {
//                            if (!found) {
//                                val numberPlate = vehicleSnapshot.child("number_plate").getValue(String::class.java)
//                                numberPlate?.let {
//                                    binding.colourInput.text = it
//                                    found = true
//                                }
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//                        // Handle possible errors.
//                    }
//                })
//        }
//    }








