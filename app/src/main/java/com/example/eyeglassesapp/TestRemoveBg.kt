package com.example.eyeglassesapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.ContentValues
import android.provider.MediaStore


class TestRemoveBg : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_remove_bg)
        enableEdgeToEdge()

        imageView = findViewById(R.id.imageView_test)
        val button = findViewById<Button>(R.id.buttonSelectImage)
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            val inputStream = contentResolver.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            removeBackgroundFromImage(bitmap) { resultBitmap ->
                runOnUiThread {
                    if (resultBitmap != null) {
                        imageView.setImageBitmap(resultBitmap)
                        val savedImagePath = saveImageToMediaStore(resultBitmap)
                        if (savedImagePath != null) {
                            Toast.makeText(
                                this,
                                "Image saved to $savedImagePath",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Failed to remove background", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }


    // Will create a forlder named "no_bg" in the Images Folder of the phone (simulator)
    private fun saveImageToMediaStore(finalBitmap: Bitmap): String? {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Image-${System.currentTimeMillis()}.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/no_bg"
                )
            }
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    return uri.toString()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}
