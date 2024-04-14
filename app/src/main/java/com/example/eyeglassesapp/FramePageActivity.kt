package com.example.eyeglassesapp

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Update
import com.example.eyeglassesapp.dao.FrameDao
import com.example.eyeglassesapp.dao.ImageDao
import com.example.eyeglassesapp.databinding.ActivityFramePageBinding
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.ImageEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FramePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFramePageBinding
    private lateinit var database: AppDatabase
    private lateinit var frameDao: FrameDao
    private lateinit var imageDao : ImageDao
    private lateinit var imageRepository: ImageRepository
    private lateinit var frameRepository: FrameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityFramePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(applicationContext)

        // Initialize Frame DAO
        frameDao = database.frameDao()
        // Initialize Frame Repository
        frameRepository = FrameRepository(frameDao)
        // Initialize Frame DAO
        imageDao = database.imageDao()
        // Initialize Frame Repository
        imageRepository = ImageRepository(imageDao)

        // Receive frame ID from intent
        val frameId = intent.getIntExtra("frameId", -1)

        // Fetch frame details asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            val frameDetails = frameRepository.getFrameById(frameId)
            val images = imageRepository.findImagesByFrameId(frameId)
            if (frameDetails != null) {
                updateViews(frameDetails, images)
            } else {
                // Handle the case when frame details are not available
            }
        }

        //update frame and photos functionality
        binding.updateButton.setOnClickListener {
            // Start the FormActivity to update frame details
            val updateIntent = Intent(this, UpdateFrameActivity::class.java).apply {
                putExtra("frameId", frameId)
            }
            startActivity(updateIntent)

        }

        //delete button for frame - images are deleted because of ON DELETE CASCADE
        binding.deleteButton.setOnClickListener{
            lifecycleScope.launch {
                frameRepository.deleteFrameById(frameId)
            }
            val resultIntent = Intent()
            resultIntent.putExtra("deletedFrameId", frameId) // trimiteți înapoi ID-ul frame-ului șters
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        }

    }

    private fun updateViews(frameDetails: FrameEntity, images: List<ImageEntity>) {

        // Update views with images

        // Find the first image URI to be used as the product image ( main )
        val productImage = findViewById<ImageView>(R.id.product_image)
        var productImageUri: String=""
        if (images.isNotEmpty()){
            productImageUri = images[0].imageUri
        }else{
            productImage.setImageResource(R.drawable.default_image_placeholder)
        }
        // Update ImageView with product image
        val productImageBitmap = getImageBitmapFromPath(productImageUri)
        if (productImageBitmap != null) {
            productImage.setImageBitmap(productImageBitmap)
        } else {
            // If bitmap loading fails, use placeholder image
            productImage.setImageResource(R.drawable.default_image_placeholder)
        }

        // Set mini images
        val miniImagesContainer = findViewById<LinearLayout>(R.id.mini_images_container)
        miniImagesContainer.removeAllViews() // Clear previous images if any

        // Start from index 1 to skip the product image
        for (i in 1 until images.size) {
            if (i < 3) { // Ensure we only add two mini images (images 2 and 3)
                val imageView = ImageView(this)
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    width = 200 // Adjust width as needed
                    height = 150 // Adjust height as needed
                    setMargins(4, 0, 4, 0) // Add margins as needed
                }
                val imageResource = getImageBitmapFromPath(images[i].imageUri)
                if (imageResource != null) {
                    imageView.setImageBitmap(imageResource)
                } else {
                    // If bitmap loading fails, use placeholder image
                    imageView.setImageResource(R.drawable.default_image_placeholder)
                }
                miniImagesContainer.addView(imageView)
            }
        }

        // Update other TextViews with frame details
        findViewById<TextView>(R.id.product_description).text = frameDetails.description
        findViewById<TextView>(R.id.product_price).text = frameDetails.price.toString()
        findViewById<TextView>(R.id.brand).text = frameDetails.brand
        findViewById<TextView>(R.id.model).text = frameDetails.model
        findViewById<TextView>(R.id.colour).text = frameDetails.colour
        findViewById<TextView>(R.id.gender).text = frameDetails.gender
        findViewById<TextView>(R.id.category).text = frameDetails.category
        findViewById<TextView>(R.id.material).text = frameDetails.material
        findViewById<TextView>(R.id.frame_type).text = frameDetails.frameType
    }

    private fun getImageBitmapFromPath(imagePath: String): Bitmap? {
        val file = File(imagePath)
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.absolutePath)
        }
        return null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle arrow click here
        if (item.itemId == android.R.id.home) {
            // This is the back button in the toolbar
            finish() // Close the current activity
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}