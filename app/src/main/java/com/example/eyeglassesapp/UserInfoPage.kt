package com.example.eyeglassesapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eyeglassesapp.dao.UserDao
import com.example.eyeglassesapp.databinding.ActivityUserInfoPageBinding
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import kotlinx.coroutines.launch

class UserInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoPageBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository
    private lateinit var userFirebase : UserEntity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(applicationContext)

        // Initialize User DAO
        userDao = database.userDao()
        // Initialize User Repository
        userRepository = UserRepository(userDao)

        //get userId from the other page
        val userId = intent.extras?.getInt("userId") ?: -1
        if (userId != -1) {
            // Use Kotlin's coroutines to fetch the user data asynchronously
            lifecycleScope.launch {
                // Fetch the user data
                val userData = userRepository.getUserById(userId)
                // Now, use the userData to update the UI in the main thread
                userData?.let { user ->
                    val userIdTextView : TextView = findViewById(R.id.user_id)
                    val userUsernameTextView: TextView = findViewById(R.id.user_username)
                    val userEmailTextView: TextView = findViewById(R.id.user_email)
                    val userGenderTextView : TextView = findViewById(R.id.user_gender)


                    userIdTextView.text = "User No #${user.userId}"
                    userUsernameTextView.text = "Username: ${user.username}"
                    userEmailTextView.text = "Email: ${user.email}"
                    userGenderTextView.text = "Gender: ${user.gender}"

                    // TODO upload user picture
                }
            }
        }else{
            Toast.makeText(this, "Error: User information could not be loaded.", Toast.LENGTH_LONG).show()
            finish()
        }

        binding.backButton.setOnClickListener{
            onBackPressed()
        }
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(userId)
        }
    }

    private fun showDeleteConfirmationDialog(userId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this user?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteUser(userId)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteUser(userId: Int) {
        lifecycleScope.launch {

            //stergere din firestore
            //not null object
            userFirebase = userRepository.getUserById(userId)!!
            userRepository.deleteUserFromFirestore(userFirebase.firebaseUid)

            //delete user din room db
            userRepository.deleteUserById(userId)
            // Prepare the result to be sent back to previous activity
            val resultIntent = Intent()
            resultIntent.putExtra("deletedUserId", userId) // Send back the ID of the deleted user
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Close the activity after deletion
        }
    }

}