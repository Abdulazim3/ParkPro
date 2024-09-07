package com.example.parkingapp.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.parkingapp.R
import com.example.parkingapp.databinding.ActivitySettingsBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("insert url here")

        // Binding setup
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // set up bindings for buttons
        binding.settingsvehiclebutton.setOnClickListener {
            startActivity(Intent(this, SettingsVehicleActivity::class.java))
        }
        binding.settingsadminbutton.setOnClickListener {
            showAdminPasswordDialog()
        }


        fetchAndSetName()
    }

    // function to shwo a passwrod dialog for admin acces
    private fun showAdminPasswordDialog() {
        //create alert dialog builder object to build dialog
        val passwordDialog = AlertDialog.Builder(this)
        //title
        passwordDialog.setTitle("Admin Access")
        //edit text to let admin enter password
        val passwordEditText = EditText(this)
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        //set edit text to the dialog
        passwordDialog.setView(passwordEditText)
        passwordDialog.setPositiveButton("Enter") { dialog, which ->
            val password = passwordEditText.text.toString()
            if (password == "123456") { // password
                // Correct password, navigate to AdminActivity
                val adminIntent = Intent(this, AdminActivity::class.java)
                startActivity(adminIntent)
            } else {
                // error handling
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
        passwordDialog.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        passwordDialog.show()
    }
    //fetches are sets username in the header.
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

