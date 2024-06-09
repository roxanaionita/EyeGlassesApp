package com.example.eyeglassesapp


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.FaceShapeViewModel
import com.example.eyeglassesapp.ViewModels.FrameFaceShapeCrossViewModel
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.databinding.ActivityRecommendationBinding
import com.example.eyeglassesapp.databinding.ActivityUserReportBinding
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mlkit.vision.face.FaceLandmark
import java.util.Locale

class UserReport : AppCompatActivity() {
    private lateinit var binding: ActivityUserReportBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }

    private var userId: Int = 0
    private lateinit var gender : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserReportBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userId = intent.getIntExtra("userId", -1)
        Log.d("UserId", "$userId")

        val frameFaceShapeCrossViewModel: FrameFaceShapeCrossViewModel by viewModels()
        val faceShapeViewModel: FaceShapeViewModel by viewModels()

        val faceLengthTextView: TextView = findViewById(R.id.face_length_value)
        val faceWidthTextView: TextView = findViewById(R.id.face_width_value)
        val foreheadWidthTextView: TextView = findViewById(R.id.forehead_width_value)
        val cheekbonesWidthTextView: TextView = findViewById(R.id.cheekbones_width_value)
        val cheekbonesAngleTextView: TextView = findViewById(R.id.cheekbones_angle_value)
        val faceShapesTextView: TextView = findViewById(R.id.face_shapes_value)

        val eyeglassesRecyclerView1: RecyclerView = findViewById(R.id.eyeglassesRecyclerView)
        val eyeglassesRecyclerView2: RecyclerView = findViewById(R.id.eyeglasses2RecyclerView)
        val sunglassesRecyclerView1: RecyclerView = findViewById(R.id.sunglassesRecyclerView)
        val sunglassesRecyclerView2: RecyclerView = findViewById(R.id.sunglasses2RecyclerView)


        setupRecyclerView(eyeglassesRecyclerView1)
        setupRecyclerView(eyeglassesRecyclerView2)
        setupRecyclerView(sunglassesRecyclerView1)
        setupRecyclerView(sunglassesRecyclerView2)

        // Fetch user data and populate views
        userViewModel.getUserById(userId)
        userViewModel.userLiveData.observe(this) { user ->
            user?.let {
                gender = it.gender
                faceLengthTextView.text = it.faceLength?.toString() ?: "N/A"
                faceWidthTextView.text = it.faceWidth?.toString() ?: "N/A"
                foreheadWidthTextView.text = it.foreheadWidth?.toString() ?: "N/A"
                cheekbonesWidthTextView.text = it.cheekbonesWidth?.toString() ?: "N/A"
                cheekbonesAngleTextView.text = it.cheekbonesAngle?.toString() ?: "N/A"
                faceShapesTextView.text = listOfNotNull(it.firstPredictedShape, it.secondPredictedShape).joinToString(", ")

                loadFramesForUser(it, frameFaceShapeCrossViewModel)
            }
        }

        binding.backButton.setOnClickListener{
            val intent = Intent(this, UserAccountActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
        }
        binding.accountRedirect.setOnClickListener{
            val intent = Intent(this, UserAccountActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MainActFrameAdapter(emptyList())
    }

    private fun loadFramesForUser(user: UserEntity, frameFaceShapeCrossViewModel: FrameFaceShapeCrossViewModel) {
        user.firstPredictedShape?.let { firstShape ->
            frameFaceShapeCrossViewModel.getFramesForFaceShape(firstShape).observe(this) { frames ->
                val eyeglasses = frames.filter { it.frame.category == "Eyeglasses" && it.frame.gender.toLowerCase(
                    Locale.ROOT) == gender }
                val sunglasses = frames.filter { it.frame.category == "Sunglasses" && it.frame.gender.toLowerCase(
                    Locale.ROOT) == gender }

                (binding.eyeglassesRecyclerView.adapter as MainActFrameAdapter).updateFrames(eyeglasses)
                (binding.sunglassesRecyclerView.adapter as MainActFrameAdapter).updateFrames(sunglasses)
            }
        }

        user.secondPredictedShape?.let { secondShape ->
            frameFaceShapeCrossViewModel.getFramesForFaceShape(secondShape).observe(this) { frames ->
                val eyeglasses = frames.filter { it.frame.category == "Eyeglasses" && it.frame.gender.toLowerCase(
                    Locale.ROOT) == gender }
                val sunglasses = frames.filter { it.frame.category == "Sunglasses" && it.frame.gender.toLowerCase(
                    Locale.ROOT) == gender  }

                (binding.eyeglasses2RecyclerView.adapter as MainActFrameAdapter).updateFrames(eyeglasses)
                (binding.sunglasses2RecyclerView.adapter as MainActFrameAdapter).updateFrames(sunglasses)
            }
        }
    }
}



