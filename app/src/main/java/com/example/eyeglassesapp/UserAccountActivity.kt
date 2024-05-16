package com.example.eyeglassesapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.databinding.ActivityMainBinding
import com.example.eyeglassesapp.databinding.ActivityUserAccountBinding
import com.example.eyeglassesapp.repositories.UserRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class UserAccountActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserAccountBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }
    private var userId : Int = 0
    private var userGender : String = ""
    private var PICK_IMAGE_REQUEST = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("userId", -1)
        if (userId != -1) {
            userViewModel.getUserById(userId)
            userViewModel.userLiveData.observe(this) { user ->
                if (user != null) {
                    userGender = user.gender
                    binding.usernameValue.text = user.username
                    binding.emailValue.text = user.email

                    //OBSERVE AGAIN TO SEE IMAGE CHANGES IN THE MEANTIME
                    userViewModel.userLiveData.observe(this) { user ->
                        if (user != null) {
                            // Update UI with the updated user data
                            binding.usernameValue.text = user.username
                            binding.emailValue.text = user.email

                            // Load image from room
                            loadImage(binding.userImage, user.profilePictureUrl ?: "")
                        }
                    }

                    binding.userImage.setOnClickListener {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.apply {
                            setTitle("Change Profile Picture")
                            setMessage("Do you want to change your profile picture?")
                            setPositiveButton("Change Picture") { dialog, _ ->
                                // Launch an image picker to select a new image
                                val intent = Intent(Intent.ACTION_GET_CONTENT)
                                intent.type = "image/*"
                                startActivityForResult(intent, PICK_IMAGE_REQUEST)
                                dialog.dismiss()
                            }
                            setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                        }.create().show()
                    }

                } else {
                    // User not found or error occurred
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
        }


        binding.homeRedirect.setOnClickListener{
            val intent = Intent(this, MainActivity :: class.java)
            startActivity(intent)
        }
        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }
        //redirect catre lista de comenzi efectuate
        binding.orderListButton.setOnClickListener{
            val intent = Intent(this, UserOrdersActivity :: class.java)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }

        //logout button
        binding.logoutButton.setOnClickListener {
            // Trigger the logout process
            userViewModel.logout()
        }
        // Observe the logout status to handle navigation
        userViewModel.logoutStatus.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                // Navigate to the login screen
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { selectedImageUri ->
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                val imagePath = saveBitmapToFile(bitmap)

                // Update profile picture in the database
                updateUserProfilePicture(imagePath)

                // Load updated profile picture into ImageView
                loadImage(binding.userImage, imagePath)
            }
        }
    }
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        val file = wrapper.getDir("images", Context.MODE_PRIVATE)
        val fileName = "profile_image${UUID.randomUUID()}.jpg"
        val filePath = File(file, fileName)

        try {
            val outputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return filePath.absolutePath
    }
    private fun updateUserProfilePicture(imagePath: String) {
        // Get the current user ID
        val userId = intent.getIntExtra("userId", -1)

        if (userId != -1) {
            // Update the profile picture path in the ViewModel
            userViewModel.updateUserProfilePicture(userId, imagePath)
        }
        userViewModel.userLiveData.observe(this) { user ->
            if (user != null) {
                // Update UI with the updated user data
                binding.usernameValue.text = user.username
                binding.emailValue.text = user.email
                loadImage(binding.userImage, user.profilePictureUrl ?: "")
            }
        }
    }
    private fun loadImage(imageView: ImageView, imagePath: String) {
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imageView.setImageBitmap(myBitmap)
        } else {
            // Handle the case where the image file doesn't exist with default images
            if (userGender == "male")
                imageView.setImageResource(R.drawable.boss)
            if(userGender == "female")
                imageView.setImageResource(R.drawable.businesswoman)
        }
    }

}