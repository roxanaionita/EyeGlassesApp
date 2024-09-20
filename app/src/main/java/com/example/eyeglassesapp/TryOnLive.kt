package com.example.eyeglassesapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mediapipe.examples.facelandmarker.FaceLandmarkerHelper
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.core.RunningMode

class TryOnLive : AppCompatActivity(), FaceLandmarkerHelper.LandmarkerListener {

    private lateinit var previewView: PreviewView
    private lateinit var liveOverlay: LiveOverlay
    private lateinit var permissionButton: Button
    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private var imagePath: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionButton.visibility = View.GONE
            startCamera()
        } else {
            permissionButton.visibility = View.VISIBLE
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_on_live)

        previewView = findViewById(R.id.viewFinder)
        liveOverlay = findViewById(R.id.graphicOverlay)
        permissionButton = findViewById(R.id.permission_button)

        imagePath = intent.getStringExtra("imagePath")
        imagePath?.let {
            val glassesBitmap = BitmapFactory.decodeFile(it)
            liveOverlay.setGlassesBitmap(glassesBitmap)
        }

        faceLandmarkerHelper = FaceLandmarkerHelper(
            context = this,
            runningMode = RunningMode.LIVE_STREAM,
            faceLandmarkerHelperListener = this
        )

        // Initially hide the camera preview
        previewView.visibility = View.GONE
        liveOverlay.visibility = View.GONE

        permissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        // Show the camera preview
        previewView.visibility = View.VISIBLE
        liveOverlay.visibility = View.VISIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), ::processImageProxy)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun processImageProxy(imageProxy: ImageProxy) {
        try {
            val bitmap = YuvToRgbConverter.imageToBitmap(this, imageProxy)
            val mpImage = BitmapImageBuilder(bitmap).build()
            val frameTime = System.currentTimeMillis()

            faceLandmarkerHelper.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        } finally {
            imageProxy.close()
        }
    }

    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
        runOnUiThread {
            liveOverlay.setResults(
                resultBundle.result,
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth,
                270,
                RunningMode.LIVE_STREAM
            )
        }
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onEmpty() {
        runOnUiThread {
            liveOverlay.clear()
        }
    }

    companion object {
        private const val TAG = "TryOnLive"
        }
}
