package com.example.parkingapp.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.parkingapp.DataModels.CarParks
import com.example.parkingapp.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : ComponentActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //inflates the admin_activity xml layout for data binding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //initialise firebase auth and database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("insert url here")
        //button click listener
        binding.submitButton.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) { //
                addCarPark()
            } else {
                Toast.makeText(this, "You must be logged in to add car parks.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    private fun addCarPark() {
        try { //retrieves all textfield inputs
            val carparkName = binding.carparkNameInput.text.toString().trim()
            val standardBaysInput = binding.standardBaysInput.text.toString().trim()
            val disabledBaysInput = binding.disabledBaysInput.text.toString().trim()
            val carparkAddress = binding.carparkAddressInput.text.toString().trim()
            val entranceImageUrl = binding.entranceImageUrlInput.text.toString().trim()
            val entranceDescription = binding.entranceDescriptionInput.text.toString().trim()
            val restrictions = binding.restrictionsInput.text.toString().trim()
            val paymentMethods = binding.paymentMethodsInput.text.toString().trim()

            //convert bays to int and handles empty values
            val standardBays = if (standardBaysInput.isNotEmpty()) standardBaysInput.toInt() else null
            val disabledBays = if (disabledBaysInput.isNotEmpty()) disabledBaysInput.toInt() else null
            //parses opening times and prices
            val carparkPrices = parsePrices(binding.carparkPricesInput.text.toString().trim())
            val carparkOpeningTimes = parseOpeningTimes(binding.carparkOpeningTimesInput.text.toString().trim())

            //creates new carpark reference,(tells you which node to check) in database
            val carParkRef = database.reference.child("CarParks").push()
            val carPark = CarParks(
                carparkID = carParkRef.key ?: "",
                carparkName = carparkName,
                standardBays = standardBays ,
                disabledBays = disabledBays ,
                carparkAddress = carparkAddress,
                carparkPrices = carparkPrices,
                entranceImageUrl = entranceImageUrl,
                entranceDescription = entranceDescription,
                restrictions = restrictions,
                carparkOpeningTimes = carparkOpeningTimes,
                paymentMethods = paymentMethods
            )
            // saves carpark to database
            carParkRef.setValue(carPark)
                .addOnSuccessListener {
                    Toast.makeText(this, "Car park added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add car park: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing input: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    //function to parse opening times, easier than configuring xml layyout to handle multiple values for opening times
    private fun parseOpeningTimes(openingTimesString: String): HashMap<String, String> {
        val openingTimesMap = HashMap<String, String>()
        //  the format is Monday 5-7pm, Tuesday 5-7pm
        openingTimesString.split(", ").forEach {
            val parts =
                it.split(" ", limit = 2) // split by the first space to separate the day from times
            if (parts.size == 2) {
                openingTimesMap[parts[0].trim()] = parts[1].trim()
            }
        }
        return openingTimesMap
    }

    // function to parse prices from a string input to make a hashmap
    private fun parsePrices(pricesString: String): HashMap<String, String> {
        val pricesMap = HashMap<String, String>()
        //  format is 1hr=£1, 3hrs=£3
        pricesString.split(", ").forEach {
            val parts = it.split("=")
            if (parts.size == 2) {
                pricesMap[parts[0].trim()] = parts[1].trim()
            }
        }
        return pricesMap
    }

}

