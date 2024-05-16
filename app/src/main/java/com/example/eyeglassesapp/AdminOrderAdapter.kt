package com.example.eyeglassesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.entities.OrderEntity
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminOrderAdapter(
    private var orders: List<OrderEntity>,
    private val userRepository: UserRepository
) : RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_order, parent, false)
        return AdminOrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    fun updateOrders(newOrders: List<OrderEntity>) {
        orders = newOrders
        notifyDataSetChanged()
    }


    inner class AdminOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.order_username)
        private val emailTextView: TextView = itemView.findViewById(R.id.order_email)
        private val dateTextView: TextView = itemView.findViewById(R.id.date_order)
        private val statusTextView: TextView = itemView.findViewById(R.id.status)
        private val totalTextView: TextView = itemView.findViewById(R.id.total_order)

        fun bind(order: OrderEntity) {
            // Set order details
            dateTextView.text = order.orderDate.toString()
            statusTextView.text = "Confirmed"
            totalTextView.text = "$${order.totalPrice}"

            // Coroutine to fetch user details
            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserById(order.userId)
                user?.let {
                    usernameTextView.text = it.username
                    emailTextView.text = it.email
                }
            }
        }
    }
}
