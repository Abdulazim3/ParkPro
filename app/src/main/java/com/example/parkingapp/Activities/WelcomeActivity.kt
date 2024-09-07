package com.example.parkingapp.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.parkingapp.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : ComponentActivity() {

    private lateinit var binding: ActivityWelcomeBinding // Declaring a variable to hold the binding object for
                                                            // Welcome activity layout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        checkUserLoggedIn()
// Inflating the layout for the Welcome activity using the binding object and setting content view
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
// on click listeners for 2 buttons that start activity for sign in and sign up page
        binding.signInButton.setOnClickListener {
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent)
        }

        binding.signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
    }
// checks is the user is already logged in and direct to the main page
    private fun checkUserLoggedIn() {
        if (auth.currentUser != null) {
            val mainActivityIntent = Intent(this, MainTestActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        }
    }
}
