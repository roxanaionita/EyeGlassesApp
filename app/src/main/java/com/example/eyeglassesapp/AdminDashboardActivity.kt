package com.example.eyeglassesapp

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eyeglassesapp.databinding.ActivityAdminDashboardBinding
import com.example.eyeglassesapp.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //redirect to frames list menu - recycler view
        binding.adminFramespage.setOnClickListener{
            val intent = Intent(this, Admin_FramesPage::class.java)
            startActivity(intent)
            finish() // End this activity
        }
        //redirect to list of users - extract from room
        binding.adminUserspage.setOnClickListener{
            val intent = Intent(this, Admin_UsersPage::class.java)
            startActivity(intent)
            finish() // End this activity
        }
        //redirect from orders list page - extract from room
        binding.adminOrderspage.setOnClickListener{
            val intent = Intent(this, Admin_OrdersPage::class.java)
            startActivity(intent)
            finish() // End this activity
        }
    }
}