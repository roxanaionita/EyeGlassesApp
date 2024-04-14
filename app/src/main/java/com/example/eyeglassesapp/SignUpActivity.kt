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
        // Apply window insets for system bars

        //bind with the xml file and start Firebase instance
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

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
            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
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
                                        firebaseUid = task.result?.user?.uid ?: "",
                                        username = username,
                                        email = email,
                                        gender = gender,
                                        faceShapeId = null, // This can be set later or obtained from another part of your app if necessary
                                        profilePictureUrl = null // This can be updated later when the user adds a profile picture
                                    )
                                    CoroutineScope(Dispatchers.IO).launch {
                                        userRepository.insertUser(newUser)
                                    }

                                    //navigate to LogInActivity
                                    val intent = Intent(this, LogInActivity::class.java)
                                    startActivity(intent)
                                    finish() // End this activity
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
                Toast.makeText(this, "Can't have empty fields", Toast.LENGTH_SHORT).show()
            }
        }

        //daca se apasa pe textul de redirect
        binding.loginRedirect.setOnClickListener {
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }
        binding.backButton.setOnClickListener{
            finish()
        }

    }

}
