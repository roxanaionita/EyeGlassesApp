package com.example.eyeglassesapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eyeglassesapp.databinding.ActivitySignUpBinding
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        firebaseAuth = FirebaseAuth.getInstance()
        //pentru a stoca userul in CloudFirestore
        firestore = FirebaseFirestore.getInstance()

        if (applicationContext == null) {
            throw IllegalStateException("Context should not be null when accessing the database")
        }else{
            Log.d("SignUpActivity","not null context")
        }

        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(appDatabase.userDao())


        //logica pentru butonul de sign in si text field-uri
        binding.signupButton.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupPasswordConfirm.text.toString()
            val username = binding.signupUsername.text.toString()
            //checkedRadioBtnId is a function to see the selected radio btn
            val selectedGenderId = binding.genderRadioGroup.checkedRadioButtonId
            val gender = when(selectedGenderId) {
                R.id.maleRadioButton -> "male"
                R.id.femaleRadioButton -> "female"
                else -> "" // No selection or handling other possible options
            }
            val profilePictureUrl = null
            if (validateUsername(username) && isValidEmail(email) && isValidPassword(password, confirmPassword)) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = hashMapOf(
                                "username" to username,
                                "email" to email,
                                "gender" to gender,
                                "role" to "user", // Default role
                                "profilePic" to profilePictureUrl
                            )
                            //documentul se creeaza automat daca nu exista
                            firestore.collection("users").document(task.result?.user?.uid ?: "")
                                .set(user)
                                .addOnSuccessListener {
                                    // create new user entity object and inject into room db
                                    val newUser = UserEntity(
                                        //rolul este by default de user, astfel adminul nu este stocat
                                        //in Room Db
                                        firebaseUid = task.result?.user?.uid ?: "",
                                        username = username,
                                        email = email,
                                        gender = gender,
                                        faceShapeId = null,
                                        profilePictureUrl = null //null for now, user page for update
                                    )
                                    CoroutineScope(Dispatchers.IO).launch {
                                        userRepository.insertUser(newUser)
                                    }


                                    val intent = Intent(this, LogInActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to save user details: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Display Toast messages for invalid input fields
                if (!validateUsername(username)) {
                    Toast.makeText(this, "Invalid username! Username must be at least 5 characters long and contain letters.", Toast.LENGTH_SHORT).show()
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(this, "Invalid email address! Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
                if (!isValidPassword(password, confirmPassword)) {
                    Toast.makeText(this, "Invalid password! Password must be at least 8 characters long and contain both letters and numbers.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // redirect text
        binding.loginRedirect.setOnClickListener {
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }
        binding.backButton.setOnClickListener{
            finish()
        }

    }
    // Function to validate username
    private fun validateUsername(username: String): Boolean {
        return username.length >= 5 && username.matches(Regex(".*[a-zA-Z].*"))
    }

    // Function to validate email address
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate password
    private fun isValidPassword(password: String, confirmPassword: String): Boolean {
        return password.length >= 8 && password.matches(Regex(".*[a-zA-Z].*")) && password.matches(
            Regex(".*\\d.*")
        ) && password == confirmPassword
    }

}
