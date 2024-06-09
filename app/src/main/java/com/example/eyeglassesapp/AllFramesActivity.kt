package com.example.eyeglassesapp

import FrameViewModelFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.databinding.ActivityAllFramesBinding
import com.example.eyeglassesapp.databinding.ActivityMainBinding
import com.example.eyeglassesapp.repositories.FrameRepository

class AllFramesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAllFramesBinding
    private lateinit var adapter: MainActFrameAdapter
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllFramesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        //GESTIONARE RECYCLER VIEW PENTRU POPULAR FRAMES = UNDER A PRICE
        adapter = MainActFrameAdapter(emptyList())
        val recyclerView = binding.recviewAllFrames

        //se seteaza layout-ul recycler view ului
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter

//        frameViewModel.getFramesWithImageAndPriceLess()
        frameViewModel.allFramesWithImages.observe(this){frames->
            adapter.updateFrames(frames)
        }
    }
}