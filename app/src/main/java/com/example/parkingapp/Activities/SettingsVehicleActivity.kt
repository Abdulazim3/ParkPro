package com.example.parkingapp.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.parkingapp.databinding.ActivitySettingsvehicleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingsVehicleActivity : ComponentActivity() {
    private lateinit var binding: ActivitySettingsvehicleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsvehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("insert url here")

        binding.numberPlateEntryButton.setOnClickListener {
            val newNumberPlate = binding.numberPlateEntry.text.toString().trim()
            if (newNumberPlate.isNotEmpty()) {
                fetchAndUpdateVehicle(newNumberPlate)
            } else {
                Toast.makeText(this, "Number plate cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }
    //fetches and updates the number plate in the database
    private fun fetchAndUpdateVehicle(newNumberPlate: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.reference.child("Vehicles")
                .orderByChild("userID").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (vehicleSnapshot in dataSnapshot.children) {
                            val vehicleId = vehicleSnapshot.key
                            if (vehicleId != null) {
                                updateNumberPlate(vehicleId, newNumberPlate)
                            }
                            break
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@SettingsVehicleActivity, "Failed to fetch vehicle data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    // this updates a given number plate
    private fun updateNumberPlate(vehicleId: String, newNumberPlate: String) {
        database.reference.child("Vehicles").child(vehicleId).child("number_plate").setValue(newNumberPlate)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Number plate updated successfully", Toast.LENGTH_SHORT).show()
                    // set a delay before navigating to the VehicleActivity
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@SettingsVehicleActivity, VehicleActivity::class.java)
                        startActivity(intent)
                        finish()  //
                    }, 500)  //0.5 seconds
                } else {
                    Toast.makeText(this, "Failed to update number plate", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
