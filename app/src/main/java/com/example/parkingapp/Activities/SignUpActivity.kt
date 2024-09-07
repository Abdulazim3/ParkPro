package com.example.parkingapp.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.DataModels.Users
import com.example.parkingapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("insert url here")

        setupListeners()
    }
    // function to set up listens for all the buttons
    private fun setupListeners() {
        binding.registerbutton.setOnClickListener {
            if (validateDetailsInputs() && validatePasswords()) {
                createAccountWithEmail()
            }
        }
        binding.AlreadyHaveAccountTextView1.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        binding.AlreadyHaveAccountTextView2.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.nextbutton.setOnClickListener {
            if (validateLoginInputs()) {
                switchToDetailsLayout()
            }
        }
        binding.backbutton.setOnClickListener {
            switchToDetails2Layout()
        }
    }
    private fun switchToDetailsLayout() {
        binding.detailsLayout.visibility = View.GONE
        binding.detailsLayout2.visibility = View.VISIBLE
    }
// switches between the 2 layouts making one visible and the other not visible
    private fun switchToDetails2Layout() {
        binding.detailsLayout.visibility = View.VISIBLE
        binding.detailsLayout2.visibility = View.GONE
    }
    //function to validate user inputs
    private fun validateLoginInputs(): Boolean {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val repeatPassword = binding.repeatpasswordEditText.text.toString().trim()
        //if email and password is empty display toast
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
    // is password dont match, display toast
        if (password != repeatPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
    //function to make sure all details and filled in
    private fun validateDetailsInputs(): Boolean {
        val firstName = binding.firstnameEditText.text.toString().trim()
        val lastName = binding.lastnameEditText.text.toString().trim()
        val phoneNumber = binding.phonenumberEditText.text.toString().trim()
        //is firstname last name or phone number is empty, display toast
        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    //ensures passwords are correct
    private fun validatePasswords(): Boolean {
        val password = binding.passwordEditText.text.toString()
        val repeatPassword = binding.repeatpasswordEditText.text.toString()

        if (password != repeatPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    // function to create account using firebase authenticaitons
    private fun createAccountWithEmail() {
        //retrieve email password from textfields
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        //using Firebase Auth to create new user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                //listen to handle the result of create user request
            .addOnCompleteListener { task ->
                //if successful, calls the function to save additional user details
                if (task.isSuccessful) {
                    saveUserToDatabase()
                } else { //task exception shows any error from auth. eg email already in use or will show Registration failed
                    Toast.makeText(this, task.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    //function to save all of users details expect password to database
    private fun saveUserToDatabase() {
        val userId = auth.currentUser?.uid ?: "" //retrieves the current userID
        // create a Users object with the user's details from the text fields
        val user = Users(
            firstname = binding.firstnameEditText.text.toString().trim(),
            lastname = binding.lastnameEditText.text.toString().trim(),
            email = binding.emailEditText.text.toString().trim(),
            phonenumber = binding.phonenumberEditText.text.toString().trim(),
            userID = userId
        )
        //stores user object under the "Users" node in the firebase
        database.reference.child("Users").child(userId).setValue(user)
            .addOnSuccessListener {
                //listen used to check if data is saved succesfully, message display is success in toast
                Toast.makeText(this, "Your account has been created successfully.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainTestActivity::class.java))
                finish() //finish current activity to remove it from the activity stack
            }
            .addOnFailureListener {
                //error handling
                Toast.makeText(this, "Failed to save user info.", Toast.LENGTH_SHORT).show()
            }
    }
}

