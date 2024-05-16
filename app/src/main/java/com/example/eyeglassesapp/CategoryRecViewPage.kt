package com.example.eyeglassesapp

import FrameViewModelFactory
import android.content.Intent
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
import com.example.eyeglassesapp.repositories.FrameRepository

class CategoryRecViewPage : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryRecViewPageBinding
    private lateinit var adapter: CategoryAdapter
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityCategoryRecViewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CategoryAdapter(emptyList())
        val recyclerView = binding.recyclerViewFramesByCategory
        val layoutManager = GridLayoutManager(this,2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter


        val category = intent.getStringExtra("category")
        if (category != null) {
            frameViewModel.getFramesByCategory(category)
        } else {
            // Handle case where category is null
            showCategoryNotFoundErrorDialog()
        }
        frameViewModel.framesByCategory.observe(this){framesByCategory->
            adapter.updateFrames(framesByCategory)
        }

        binding.arrowBack.setOnClickListener{
            onBackPressed()
        }


        binding.womanCategory.setOnClickListener {
            val gender = "Female"
            if (category != null) {
                launchGenderPageActivity(category,gender)
            }
        }
        binding.manCategory.setOnClickListener {
            val gender = "Male"
            if (category != null) {
                launchGenderPageActivity(category,gender)
            }
        }


    }

    //send gender and launch activity
    private fun launchGenderPageActivity(category : String, gender : String) {
        val intent = Intent(this, GenderRecViewPage::class.java)
        intent.putExtra("category",category)
        intent.putExtra("gender",gender)
        startActivity(intent)
    }

    private fun showCategoryNotFoundErrorDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Category Not Found")
        builder.setMessage("The category information is missing or invalid.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val dialog = builder.create()
        dialog.show()
    }
}