package com.example.parkingapp.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // If the user is already logged in, go to the main activity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainTestActivity::class.java))
            finish()
            return
        }

        setupListeners()
    }
    //function to set up listeners used in on create
    private fun setupListeners() {
        binding.nextbutton.setOnClickListener {
            performLogin()
        }

        binding.DontHaveAccountTextView.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }


    }


    private fun performLogin() {
        // retrieve email and password from the EditText fields
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Check if email or password is empty
        if (email.isEmpty() || password.isEmpty()) {
            // Show a toast message if either email or password is empty
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // attempt to sign in with the provided email and password
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) { // If sign-in is successful, navigate to the main activity
                startActivity(Intent(this, MainTestActivity::class.java))
                finish()
            } else { // If sign in fails, show an error message in toast
                Toast.makeText(
                    this,
                    "Authentication failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}





