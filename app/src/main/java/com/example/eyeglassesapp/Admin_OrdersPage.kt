package com.example.eyeglassesapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.dao.OrderDao
import com.example.eyeglassesapp.dao.UserDao
import com.example.eyeglassesapp.databinding.ActivityAdminOrdersPageBinding
import com.example.eyeglassesapp.databinding.ActivityUserInfoPageBinding
import com.example.eyeglassesapp.repositories.OrderRepository
import com.example.eyeglassesapp.repositories.UserRepository

class Admin_OrdersPage : AppCompatActivity() {
    private lateinit var binding: ActivityAdminOrdersPageBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var orderDao: OrderDao
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var adminOrderAdapter: AdminOrderAdapter
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(orderRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminOrdersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(applicationContext)

        // Initialize User DAO
        userDao = database.userDao()
        orderDao = database.orderDao()
        // Initialize User Repository
        userRepository = UserRepository(userDao)
        orderRepository = OrderRepository(orderDao)


        adminOrderAdapter = AdminOrderAdapter(emptyList(), userRepository)

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recview_orders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adminOrderAdapter

        // Fetch all orders from ViewModel
        orderViewModel.getAllOrders()
        // Observe allOrders LiveData
        orderViewModel.allOrders.observe(this) { orders ->
            // Update the adapter with the new list of orders
            adminOrderAdapter.updateOrders(orders)
        }


        binding.backButton.setOnClickListener{
            finish()
        }


    }
}