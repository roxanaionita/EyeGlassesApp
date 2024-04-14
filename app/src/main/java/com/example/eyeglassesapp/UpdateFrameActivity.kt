package com.example.eyeglassesapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.eyeglassesapp.dao.FrameDao
import com.example.eyeglassesapp.dao.ImageDao
import com.example.eyeglassesapp.databinding.ActivityAdminFramesPageBinding
import com.example.eyeglassesapp.databinding.ActivityUpdateFrameBinding
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.ImageEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.UUID

class UpdateFrameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateFrameBinding
    private lateinit var database: AppDatabase
    private lateinit var frameDao: FrameDao
    private lateinit var frameRepository: FrameRepository
    private lateinit var imageDao: ImageDao
    private lateinit var imageRepository: ImageRepository

    private var images: List<ImageEntity> = emptyList()
    private var frameId: Int = -1
    private var IMAGE_PICKER_REQUEST_CODE = 1001
    private lateinit var existingImages: List<ImageEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUpdateFrameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(applicationContext)
        // Initialize DAO
        frameDao = database.frameDao()
        // Initialize Repository
        frameRepository = FrameRepository(frameDao)
        // Initialize DAO
        imageDao = database.imageDao()
        // Initialize Repository
        imageRepository = ImageRepository(imageDao)


        //se umple formularul cu datele din frame-ul curent, al carui id a fost parsat din product page
        frameId = intent.getIntExtra("frameId", -1)
        if (frameId != -1) {
            lifecycleScope.launch {
                val frame = frameRepository.getFrameById(frameId)
                existingImages = imageRepository.findImagesByFrameId(frameId)
                frame?.let {
                    // Update UI with frame details
                    findViewById<TextInputEditText>(R.id.form_brand).setText(frame.brand)
                    findViewById<TextInputEditText>(R.id.form_model).setText(frame.model)
                    findViewById<TextInputEditText>(R.id.form_color).setText(frame.colour)
                    findViewById<TextInputEditText>(R.id.form_description).setText(frame.description)
                    findViewById<TextInputEditText>(R.id.form_price).setText(frame.price.toString())

                    // Update gender radio button
                    findViewById<RadioButton>(R.id.maleRadioButton).isChecked = frame.gender == "Male"
                    findViewById<RadioButton>(R.id.femaleRadioButton).isChecked = frame.gender == "Female"

                    // Update frame type radio button
                    findViewById<RadioButton>(R.id.fullRadioButton).isChecked = frame.frameType == "Full"
                    findViewById<RadioButton>(R.id.semiRadioButton).isChecked = frame.frameType == "Semi"

                    // Update category radio button
                    findViewById<RadioButton>(R.id.EyeGlassesRadioButton).isChecked = frame.category == "Eyeglasses"
                    findViewById<RadioButton>(R.id.SunGlassesRadioButton).isChecked = frame.category == "Sunglasses"

                    // Update material radio button
                    findViewById<RadioButton>(R.id.PlasticRadioButton).isChecked = frame.material == "Plastic"
                    findViewById<RadioButton>(R.id.MetalRadioButton).isChecked = frame.material == "Metal"

                } ?: run {
                    // Handle the case where no frame is found
                    Toast.makeText(applicationContext, "Frame not found", Toast.LENGTH_SHORT).show()
                }

                //FILL IMAGE VIEWS WITH PHOTOS FROM DATABASE
                val images = imageRepository.findImagesByFrameId(frameId)
                if (images.isNotEmpty()) {
                    images.getOrNull(0)?.let { image ->
                        loadImage(findViewById(R.id.image1), image.imageUri)
                    }
                    images.getOrNull(1)?.let { image ->
                        loadImage(findViewById(R.id.image2), image.imageUri)
                    }
                    images.getOrNull(2)?.let { image ->
                        loadImage(findViewById(R.id.image3), image.imageUri)
                    }
                }
            }
        }
        //Update frame object
        binding.submitButton.setOnClickListener{
            // Get references to the form fields
            val brand = binding.formBrand.text.toString()
            val model = binding.formModel.text.toString()
            val color = binding.formColor.text.toString()
            val description = binding.formDescription.text.toString()
            val price = binding.formPrice.text.toString().toDoubleOrNull() ?: 0.0
            val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
                R.id.maleRadioButton -> "Male"
                R.id.femaleRadioButton -> "Female"
                else -> ""
            }
            val frameType = when (binding.frameTypeRadioGroup.checkedRadioButtonId) {
                R.id.fullRadioButton -> "Full"
                R.id.semiRadioButton -> "Semi"
                else -> ""
            }
            val category = when (binding.categoryRadioGroup.checkedRadioButtonId) {
                R.id.EyeGlassesRadioButton -> "Eyeglasses"
                R.id.SunGlassesRadioButton -> "Sunglasses"
                else -> ""
            }
            val material = when (binding.materialRadioGroup.checkedRadioButtonId) {
                R.id.PlasticRadioButton -> "Plastic"
                R.id.MetalRadioButton -> "Metal"
                else -> ""
            }

            val newFrame = FrameEntity(
                frameId = frameId,
                brand = brand,
                model = model,
                colour = color,
                gender = gender,
                description = description,
                price = price ?: 0.0,
                category = category,
                material = material,
                frameType = frameType
            )
            // Pass the updated frame object to a function to save it to the database
            newFrame?.let { updateFrame(it) }

            //redirect to recycler view - Admin Frames page
            val intent = Intent(this, Admin_FramesPage::class.java)
            startActivity(intent)
            finish()

        }
        //update photos
        binding.changePicturesButton.setOnClickListener {
            // Start an image picker activity
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Handle the selected images
            val selectedImages = mutableListOf<Uri>()
            if (data != null && data.data != null) {
                // Single image selected
                selectedImages.add(data.data!!)
            } else if (data != null && data.clipData != null) {
                // Multiple images selected
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    selectedImages.add(clipData.getItemAt(i).uri)
                }
            }
            // Update UI with the selected images
            updateImages(selectedImages)
        }
    }


    fun loadImage(imageView: ImageView, imagePath: String) {
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imageView.setImageBitmap(myBitmap)
        } else {
            // Handle the case where the image file doesn't exist
            imageView.setImageResource(R.drawable.default_image_placeholder)
        }
    }

    private fun updateImages(selectedImages: List<Uri>) {
        // Assuming you have references to ImageViews in your activity
        val imageViews = listOf(findViewById<ImageView>(R.id.image1), findViewById<ImageView>(R.id.image2), findViewById<ImageView>(R.id.image3))

        selectedImages.forEachIndexed { index, uri ->
            if (index < imageViews.size) {
                val imagePath = saveImageToFile(applicationContext, uri, frameId)
                if (imagePath != null) {
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    imageViews[index].setImageBitmap(bitmap)

                    // Prepare to update or insert the ImageEntity
                    val imageEntity = if (index < existingImages.size) {
                        existingImages[index].copy(imageUri = imagePath)
                    } else {
                        ImageEntity(frameId = frameId, imageUri = imagePath)
                    }

                    // Perform database operation
                    lifecycleScope.launch {
                        if (index < existingImages.size) {
                            imageRepository.updateImage(imageEntity)
                        } else {
                            imageRepository.insertImage(imageEntity)
                        }
                    }
                } else {
                    imageViews[index].setImageResource(R.drawable.default_image_placeholder)
                }
            }
        }

        // If fewer images are selected than exist, delete the old ones
        if (selectedImages.size < existingImages.size) {
            for (i in selectedImages.size until existingImages.size) {
                val imageToDelete = existingImages[i]
                lifecycleScope.launch {
                    imageRepository.deleteImageById(imageToDelete.image_id)
                }
            }
        }
    }


    private fun updateFrame(updatedFrame: FrameEntity) {
        // Call a function in your repository to update the frame in the database
        lifecycleScope.launch {
            frameRepository.updateFrame(updatedFrame)
        }
    }

    fun saveImageToFile(context: Context, uri: Uri, frameId: Int): String? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val directory = File(context.filesDir, "frame_images/$frameId").apply { mkdirs() }
        val file = File(directory, "${UUID.randomUUID()}.jpg")

        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        return file.absolutePath
    }

}