package com.example.eyeglassesapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.example.eyeglassesapp.databinding.ActivityTryOnBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.io.File
import java.io.IOException
import kotlin.math.sqrt


class TryOnActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTryOnBinding
    private lateinit var faceDetector: FaceDetector
    private lateinit var imageView: ImageView
    private lateinit var btnCapture: Button
    private lateinit var imagePath : String

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTryOnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        imageView = binding.imageView
        btnCapture = binding.btnCapture

        imagePath = intent.getStringExtra("imagePath").toString()
        if (imagePath != null) {
            Log.d("ImageSent","Image Path Received $imagePath")
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }

        val faceDetectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()

        faceDetector = FaceDetection.getClient(faceDetectorOptions)
        btnCapture.setOnClickListener {
            // Open image picker
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

    }
    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    val correctedBitmap = fixImageOrientation(uri, imageBitmap)
                    imageView.setImageBitmap(correctedBitmap)

                    // Process image for face detection
                    val inputImage = InputImage.fromBitmap(correctedBitmap, 0)

                    // Perform face detection
                    faceDetector.process(inputImage)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                Log.d("FaceDetection", "Face detected")
                                // Create a mutable copy of the original bitmap to draw on
                                val mutableBitmap = correctedBitmap.copy(Bitmap.Config.ARGB_8888, true)
                                val canvas = Canvas(mutableBitmap)
                                val paint = Paint().apply {
                                    color = Color.WHITE
                                    style = Paint.Style.STROKE
                                    strokeWidth = 4f
                                }
                                val landmarkPaint = Paint().apply {
                                    color = Color.GREEN
                                    style = Paint.Style.FILL
                                    strokeWidth = 4f
                                }
                                val eyePaint = Paint().apply {
                                    color = Color.BLACK
                                    style = Paint.Style.FILL
                                    strokeWidth = 4f
                                }
                                var leftEyePosition: FaceLandmark? = null
                                var rightEyePosition: FaceLandmark? = null
                                var leftEarPosition: FaceLandmark? = null
                                var rightEarPosition: FaceLandmark? = null

                                for (face in faces) {
                                    // Draw bounding box
                                    val boundingBox = face.boundingBox
                                    canvas.drawRect(boundingBox, paint)

                                    // Draw landmarks
                                    face.allLandmarks.forEach { landmark ->
                                        val point = landmark.position
                                        canvas.drawCircle(point.x, point.y, 1f, landmarkPaint)
                                        // Draw eyes with black color and log their coordinates
                                        if (landmark.landmarkType == FaceLandmark.LEFT_EYE) {
                                            canvas.drawCircle(point.x, point.y, 1f, eyePaint)
                                            Log.d(
                                                "FaceDetection",
                                                "Left Eye: (${point.x}, ${point.y})"
                                            )
                                            leftEyePosition = landmark
                                        } else if (landmark.landmarkType == FaceLandmark.RIGHT_EYE) {
                                            canvas.drawCircle(point.x, point.y, 1f, eyePaint)
                                            Log.d(
                                                "FaceDetection",
                                                "Right Eye: (${point.x}, ${point.y})"
                                            )
                                            rightEyePosition = landmark
                                        }else if (landmark.landmarkType == FaceLandmark.LEFT_EAR) {
                                            canvas.drawCircle(point.x, point.y, 1f, eyePaint)
                                            Log.d(
                                                "FaceDetection",
                                                "Left Ear: (${point.x}, ${point.y})"
                                            )
                                            leftEarPosition = landmark
                                        }else if (landmark.landmarkType == FaceLandmark.RIGHT_EAR) {
                                            canvas.drawCircle(point.x, point.y, 1f, eyePaint)
                                            Log.d(
                                                "FaceDetection",
                                                "Right Ear: (${point.x}, ${point.y})"
                                            )
                                            rightEarPosition = landmark
                                        }

                                    }
                                }

                                var distanceEars : Double = 0.0
                                // Calculate and log the distance between the ears
                                if (leftEarPosition != null && rightEarPosition != null) {
                                    val leftPoint = leftEarPosition!!.position
                                    val rightPoint = rightEarPosition!!.position
                                    distanceEars = calculateDistance(
                                        leftPoint.x,
                                        leftPoint.y,
                                        rightPoint.x,
                                        rightPoint.y
                                    )
                                    Log.d("FaceDetection", "Distance between ears: $distanceEars")
                                }

                                // Calculate and log the distance between the eyes
                                if (leftEyePosition != null && rightEyePosition != null) {
                                    val leftPoint = leftEyePosition!!.position
                                    val rightPoint = rightEyePosition!!.position
                                    val distance = calculateDistance(
                                        leftPoint.x,
                                        leftPoint.y,
                                        rightPoint.x,
                                        rightPoint.y
                                    )
                                    Log.d("FaceDetection", "Distance between eyes: $distance")
                                    val scalingFactor = distanceEars / distance
                                    Log.d("FaceDetection", "Scaling Factor: $scalingFactor")
                                    // Load the glasses image from assets
                                    val glassesBitmap = loadGlassesBitmap(imagePath)
                                    if (glassesBitmap != null) {
                                        // Calculate the position and size for the glasses
                                        val glassesWidth =
                                            distance * (scalingFactor + 0.3)
                                        //daca inmultesc cu scalingFactor, atunci ochelarii vor parea mai mici
                                        //deoarece este undeva la 2.2 si s-ar vedea ramele pe interior
                                        val glassesHeight =
                                            glassesBitmap.height * (glassesWidth / glassesBitmap.width)
                                        val centerX = (leftPoint.x + rightPoint.x) / 2
                                        val centerY = (leftPoint.y + rightPoint.y) / 2
                                        val left = centerX - glassesWidth / 2
                                        val top = centerY - glassesHeight / 2 + 25

                                        // Draw the glasses on the canvas
                                        canvas.drawBitmap(
                                            glassesBitmap,
                                            null,
                                            RectF(
                                                left.toFloat(),
                                                top.toFloat(),
                                                (left + glassesWidth).toFloat(),
                                                (top + glassesHeight).toFloat()
                                            ),
                                            null
                                        )
                                    }
                                }

                                // Update the ImageView with the new bitmap
                                imageView.setImageBitmap(mutableBitmap)
                            } else {
                                Log.d("FaceDetection", "No face detected")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FaceDetection", "Face detection failed", e)
                        }
                } catch (e: Exception) {
                    Log.e("ImagePicker", "Failed to get image", e)
                }
            }
        }
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble())
    }

    private fun loadGlassesBitmap(path: String?): Bitmap? {
        return if (path != null) {
            try {
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(path)
                } else {
                    Log.e("LoadGlasses", "Glasses image file does not exist")
                    null
                }
            } catch (e: IOException) {
                Log.e("LoadGlasses", "Failed to load glasses image", e)
                null
            }
        } else {
            Log.e("LoadGlasses", "Glasses image path is null")
            null
        }
    }

    private fun fixImageOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}







