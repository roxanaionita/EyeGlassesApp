package com.example.eyeglassesapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.databinding.ActivityUserOrdersBinding
import com.example.eyeglassesapp.databinding.EmptyRecyclerBinding
import com.example.eyeglassesapp.entities.OrderEntity
import com.example.eyeglassesapp.repositories.OrderRepository

class UserOrdersActivity : AppCompatActivity(), OrderAdapter.OnSeePairsClickListener {

    private lateinit var binding: ActivityUserOrdersBinding
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(AppDatabase.getDatabase(applicationContext).orderDao()))
    }
    private lateinit var userOrdersRecyclerView: RecyclerView
    private lateinit var userOrdersAdapter: OrderAdapter
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("userId", -1)

        // Initialize RecyclerView
        userOrdersRecyclerView = binding.orderElementsRecview
        userOrdersRecyclerView.layoutManager = LinearLayoutManager(this)

        val emptyView: View = findViewById(R.id.empty_list)
        val emptyMessage: TextView = findViewById(R.id.empty_message)

        // Create and set up OrderAdapter
        userOrdersAdapter = OrderAdapter(emptyList(), this)
        userOrdersRecyclerView.adapter = userOrdersAdapter

        // Observe ordersLiveData from ViewModel and update the adapter
        orderViewModel.getOrdersByUserId(userId)
        orderViewModel.ordersLiveData.observe(this) { orders ->
            userOrdersAdapter.updateOrders(orders)
            if (userOrdersAdapter.itemCount == 0) {
                emptyView.visibility = View.VISIBLE
                emptyMessage.text = "No items to display."
            } else {
                emptyView.visibility = View.GONE
            }
        }

        binding.backButton.setOnClickListener{
            super.onBackPressed()
            finish()
        }
    }

    // Implementează acțiunea de navigare către detaliile comenzii când butonul este apăsat
    override fun onSeePairsClick(order: OrderEntity) {
        // Navigare către o altă activitate cu detaliile comenzii
        val intent = Intent(this, UserOrderDetailsActivity::class.java)
        intent.putExtra("orderId", order.orderId)
        startActivity(intent)
    }
}
