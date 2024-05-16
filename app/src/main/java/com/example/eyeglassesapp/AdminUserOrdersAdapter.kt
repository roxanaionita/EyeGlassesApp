package com.example.eyeglassesapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.entities.OrderEntity


class AdminUserOrdersAdapter(
    var orders : MutableList<OrderEntity>,
    private val orderViewModel: OrderViewModel,
    private val context: Context
) : RecyclerView.Adapter<AdminUserOrdersAdapter.AdminUserOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUserOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_user_order,parent,false)
        return AdminUserOrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orders.size
    }
    fun updateOrders(newOrders: List<OrderEntity>) {
        orders.clear() // Clear existing data
        orders.addAll(newOrders) // Add new data
        notifyDataSetChanged() // Notify adapter of dataset change
    }

    override fun onBindViewHolder(holder: AdminUserOrderViewHolder, position: Int) {
        val order = orders[position]
        return holder.bind(order)
    }


    inner class AdminUserOrderViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val dateTextView: TextView = itemView.findViewById(R.id.date_value_admin)
        private val statusTextView: TextView = itemView.findViewById(R.id.status_value_admin)
        private val totalTextView: TextView = itemView.findViewById(R.id.total_value_admin)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_order_btn)
        fun bind(order : OrderEntity){
            dateTextView.text = order.orderDate.toString()
            statusTextView.text = "Confirmed"
            totalTextView.text = "$${order.totalPrice}"
            deleteButton.setOnClickListener{

                showDeleteConfirmationDialog(order)
            }
        }

        private fun showDeleteConfirmationDialog(order: OrderEntity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirm Delete")
            builder.setMessage("Are you sure you want to delete this order?")

            builder.setPositiveButton("Yes") { dialog, _ ->

                orderViewModel.deleteOrder(order)

                // Remove the item from the dataset
                // Notify adapter

                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    orders.removeAt(position)
                    notifyItemRemoved(position)
                }

                dialog.dismiss()
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    }

