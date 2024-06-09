package com.example.eyeglassesapp

import FrameViewModelFactory
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.FaceShapeViewModel
import com.example.eyeglassesapp.ViewModels.FrameFaceShapeCrossViewModel
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.dao.ImageDao

import com.example.eyeglassesapp.databinding.ActivityNewFrameFormBinding
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity
import com.example.eyeglassesapp.entities.ImageEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.UUID


//logica pentru adaugare frame nou, cu poze
//se creeaza intai obiectul de tip FrameEntity
//cand sunt alese, pozele sunt inserate in Room, cu id-ul obiectului frame curent
//frame curent este urmarit in FrameViewModel
class NewFrameForm : AppCompatActivity() {
    private lateinit var binding: ActivityNewFrameFormBinding
    private var currentFrameId: Long = 0
    private lateinit var database: AppDatabase
    private lateinit var imageDao: ImageDao
    private lateinit var imageRepository: ImageRepository
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewFrameFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(applicationContext)

        // Initialize DAO
        imageDao = database.imageDao()
        // Initialize Repository
        imageRepository = ImageRepository(imageDao)


        // Obtain the ViewModel
        val frameFaceShapeCrossViewModel = ViewModelProvider(this).get(FrameFaceShapeCrossViewModel::class.java)
        val faceShapeViewModel = ViewModelProvider(this).get(FaceShapeViewModel::class.java)

        binding.frameCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
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
                // Collect selected face shapes
                val selectedFaceShapes = mutableListOf<String>()
                if (binding.roundFaceCheckBox.isChecked) {
                    selectedFaceShapes.add("Round")
                }
                if (binding.ovalFaceCheckBox.isChecked) {
                    selectedFaceShapes.add("Oval")
                }
                if (binding.oblongFaceCheckBox.isChecked) {
                    selectedFaceShapes.add("Oblong")
                }
                if (binding.heartFaceCheckBox.isChecked) {
                    selectedFaceShapes.add("Heart")
                }
                if (binding.squareFaceCheckBox.isChecked) {
                    selectedFaceShapes.add("Square")
                }


                val newFrame = FrameEntity(
                    brand = brand,
                    model = model,
                    colour = color,
                    gender = gender,
                    description = description,
                    price = price,
                    category = category,
                    material = material,
                    frameType = frameType
                )
                // Insert the frame along with its associated face shapes
                frameViewModel.insertFrame(newFrame)
                // Observe the inserted frame ID
                frameViewModel.insertedFrameId.observe(this) { frameId ->
                    if (frameId != null && frameId != 0L) {
                        currentFrameId = frameId

                        // Insert the frame face shape cross-references
                        selectedFaceShapes.forEach { faceShapeName ->
                            faceShapeViewModel.getFaceShapeIdByName(faceShapeName).observe(this) { faceShapeId ->
                                if (faceShapeId != null) {
                                    val crossEntity = FrameFaceShape_CrossEntity(
                                        frame_id = frameId.toInt(),
                                        face_shape_id = faceShapeId
                                    )
                                    frameFaceShapeCrossViewModel.insert(crossEntity)
                                }
                            }
                        }
                        // Reset the inserted frame ID to prevent multiple insertions
                        frameViewModel.resetInsertedFrameId()
                    }
                }
            }

        }

        binding.addPicturesButton.setOnClickListener {
            pickMultipleImagesLauncher.launch("image/*")
        }
        binding.submitButton.setOnClickListener {
            val intent = Intent(this, Admin_FramesPage::class.java)
            startActivity(intent)
            finish()
        }
        binding.backButton.setOnClickListener{
            val intent = Intent(this, Admin_FramesPage::class.java)
            startActivity(intent)
            finish()
        }


    }
    private val pickMultipleImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        // For selecting multiple pictures
        uris.forEach { uri ->
            saveImageUri(uri)
        }
    }
    private fun saveImageUri(uri: Uri) {
        // current frame id
        val frameId = currentFrameId

        val imagePath = saveImageToFile(applicationContext, uri, frameId.toInt())
        if (imagePath != null) {
            val imageEntity = ImageEntity(frameId = frameId.toInt(), imageUri = imagePath)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    imageRepository.insertImage(imageEntity)
                } catch (e: Exception) {
                    Log.e("SaveImageUri", "Failed to insert image", e)
                }
            }
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



