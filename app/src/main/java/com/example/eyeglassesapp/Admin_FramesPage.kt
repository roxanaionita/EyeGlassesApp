package com.example.eyeglassesapp

import FrameViewModelFactory
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.databinding.ActivityAdminFramesPageBinding
import com.example.eyeglassesapp.repositories.FrameRepository

class Admin_FramesPage : AppCompatActivity() {
    private lateinit var binding : ActivityAdminFramesPageBinding
    private val DELETE_FRAME_REQUEST_CODE = 100
    private lateinit var adapter: FrameAdapter
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdminFramesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addFrame.setOnClickListener{
            val intent = Intent(this, NewFrameForm::class.java)
            startActivity(intent)
            //finish()
        }
        binding.backButton.setOnClickListener{
            finish()
        }

        adapter = FrameAdapter(emptyList())
        binding.framesRecyclerView.adapter = adapter

        frameViewModel.allFramesWithImages.observe(this) { frames ->
            adapter.updateFrames(frames)
        }

    }
    //if i deleted a frame in the See Frame page
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DELETE_FRAME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val deletedFrameId = data?.getIntExtra("deletedFrameId", -1)
            if (deletedFrameId != null && deletedFrameId != -1) {
                // Ștergeți frame-ul din RecyclerView
                adapter.deleteFrame(deletedFrameId)
            }
        }
    }



}