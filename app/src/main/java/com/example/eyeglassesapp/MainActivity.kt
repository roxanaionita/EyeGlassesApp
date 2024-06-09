package com.example.eyeglassesapp

import FrameViewModelFactory
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.dao.LensDao
import com.example.eyeglassesapp.databinding.ActivityMainBinding
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var user : FirebaseAuth

    private lateinit var adapter: MainActFrameAdapter
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }

    private lateinit var lensDao: LensDao
    private lateinit var lensInitializer: LensInitializer


    private var userId : Int = 0
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // INIT LENSES USING LENSINITIALIZER
        // Inițializează lens DAO și LensInitializer
        lensDao = AppDatabase.getDatabase(this).lensDao()
        lensInitializer = LensInitializer(lensDao)
//        lifecycleScope.launch(Dispatchers.IO) {
//            lensDao.deleteAllLenses()
//        }


        // Verifică dacă lentilele au fost deja inițializate în baza de date
        lifecycleScope.launch(Dispatchers.IO) {
            if (lensDao.getAllLenses().isEmpty()) {
                // Lentilele nu au fost încă inițializate, așa că le inițializăm acum
                lensInitializer.initializeLens()
            }
        }


        //GESTIONARE USER
        user = FirebaseAuth.getInstance()
        if(user.currentUser != null){
            user.currentUser?.let {
                binding.username.text = it.email
            }
        }

        userViewModel.getUserIdFromFirebaseEmail()
        userViewModel.userId.observe(this) { fetchedUserId ->
            if (fetchedUserId != null) {
                // User ID a fost obținut cu succes
                userId = fetchedUserId
                // Acum poți folosi userId în alte părți ale codului
                Log.d("UserIdDebug", "User id: $userId")

            } else {
                // User ID nu a putut fi obținut
                Log.d("UserIdDebug", "user id failed to be fetched")
            }
        }

        //REDIRECT TO CART
        binding.cartIcon.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        binding.cartText.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        //REDIRECT TO HOME
        binding.homeImg.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.homeText.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //REDIRECT TO USER MENU/PAGE
        binding.accountImage.setOnClickListener{
            val intent = Intent(this, UserAccountActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        binding.accountText.setOnClickListener{
            val intent = Intent(this, UserAccountActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // REDIRECT TO PAGE WITH ALL FRAMES
        binding.seeAll.setOnClickListener {
            val intent = Intent(this, AllFramesActivity::class.java)
            startActivity(intent)
        }


        //GESTIONARE RECYCLER VIEW PENTRU POPULAR FRAMES = UNDER A PRICE
        adapter = MainActFrameAdapter(emptyList(), 10)
        val recyclerView = binding.recyclerviewMenu

        //se seteaza layout-ul recycler view ului
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter

        frameViewModel.getFramesWithImageAndPriceLess()
        frameViewModel.framesWithImageAndPriceLess.observe(this){frames->
            adapter.updateFrames(frames)
        }

        //GESTIONARE BUTOANE PENTRU RECYCLER VIEW PENTRU CATEGORIE
        binding.eyeglassesCategory.setOnClickListener {
            val category = binding.eyeglasses.text.toString()
            launchCategoryPageActivity(category)
        }
        binding.sunglassesCategory.setOnClickListener{
            val category = binding.sunglasses.text.toString()
            launchCategoryPageActivity(category)
        }



    }
    private fun launchCategoryPageActivity(category : String){
        val intent = Intent(this, CategoryRecViewPage :: class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }
}