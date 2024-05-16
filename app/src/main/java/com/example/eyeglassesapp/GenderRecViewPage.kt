package com.example.eyeglassesapp

import FrameViewModelFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.databinding.ActivityCategoryRecViewPageBinding
import com.example.eyeglassesapp.databinding.ActivityGenderRecViewPageBinding
import com.example.eyeglassesapp.repositories.FrameRepository

class GenderRecViewPage : AppCompatActivity() {
    private lateinit var binding: ActivityGenderRecViewPageBinding
    private lateinit var adapter: GenderAdapter
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGenderRecViewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = GenderAdapter(emptyList())
        val recyclerView = binding.recyclerViewFramesByGender
        val layoutManager = GridLayoutManager(this,2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val category = intent.getStringExtra("category")
        val gender = intent.getStringExtra("gender")
        if (gender != null && category != null) {
            frameViewModel.getFramesByGender(category, gender)
        } else {
            // Category is null
            showGenderNotFoundErrorDialog()
        }
        frameViewModel.framesByGender.observe(this){framesByGender->
            adapter.updateFrames(framesByGender)
        }

        binding.arrowBack.setOnClickListener{
            onBackPressed()
        }
    }

    private fun showGenderNotFoundErrorDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Gender Not Found")
        builder.setMessage("The gender information is missing or invalid.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val dialog = builder.create()
        dialog.show()
    }
}