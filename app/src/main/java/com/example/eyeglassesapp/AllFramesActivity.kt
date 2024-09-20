package com.example.eyeglassesapp

import FrameViewModelFactory
import android.os.Bundle
import android.util.Log
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
    private lateinit var binding: ActivityAllFramesBinding
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

        // Setup RecyclerView
        adapter = MainActFrameAdapter(emptyList())
        val recyclerView = binding.recviewAllFrames
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        // Observe all frames initially
        frameViewModel.allFramesWithImages.observe(this) { frames ->
            adapter.updateFrames(frames)
        }

        // Setup Search
        binding.searchButton.setOnClickListener {
            val query = binding.searchBar.text.toString()
            val minPrice = binding.minPrice.text.toString().toDoubleOrNull() ?: 0.0
            val maxPrice = binding.maxPrice.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            performSearch(query, minPrice, maxPrice)
        }

        // Setup Reset
        binding.resetButton.setOnClickListener {
            resetSearch()
        }
    }

    private fun performSearch(query: String, minPrice: Double, maxPrice: Double) {
        try {
            frameViewModel.allFramesWithImages.observe(this) { frames ->
                val filteredFrames = frames.filter {
                    (it.frame.brand.contains(query, true) || it.frame.model.contains(query, true)) &&
                            it.frame.price in minPrice..maxPrice
                }
                adapter.updateFrames(filteredFrames)
            }
        } catch (e: Exception) {
            Log.e("AllFramesActivity", "Error performing search: ${e.message}", e)
        }
    }

    private fun resetSearch() {
        binding.searchBar.text.clear()
        binding.minPrice.text.clear()
        binding.maxPrice.text.clear()

        frameViewModel.allFramesWithImages.observe(this) { frames ->
            adapter.updateFrames(frames)
        }
    }
}
