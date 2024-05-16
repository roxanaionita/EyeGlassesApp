package com.example.eyeglassesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.entities.OrderEntity


//PENTRU LISTA DE COMENZI A USERULUI
//SE FACE REDIRECT DINTR-UN ORDER LIST ELEMENT CATRE O PAGINA
//CE CONTINE DETALIILE (PERECHILE) COMENZII
class OrderAdapter(
    private var orders: List<OrderEntity>,
    private val onSeePairsClickListener: OnSeePairsClickListener // Listener-ul pentru evenimentele de click
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_order_element, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        return orders.size
    }
    fun updateOrders(newOrders: List<OrderEntity>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.date_value)
        private val statusTextView: TextView = itemView.findViewById(R.id.status_value)
        private val totalTextView: TextView = itemView.findViewById(R.id.total_value)
        private val seePairsButton: Button = itemView.findViewById(R.id.order_contains_button)

        init {
            // Setează onClickListener pentru button
            seePairsButton.setOnClickListener {
                val order = orders[adapterPosition] // Obține comanda din listă
                onSeePairsClickListener.onSeePairsClick(order) // Apelează metoda din listener
            }
        }

        fun bind(order: OrderEntity) {
            dateTextView.text = order.orderDate.toString()
            statusTextView.text = "Confirmed" // Setează statusul comenzii
            totalTextView.text = "$${order.totalPrice}" // Setează suma totală plătită
        }
    }

    // Interfața pentru listener-ul de evenimente de clic
    interface OnSeePairsClickListener {
        fun onSeePairsClick(order: OrderEntity)
    }
}
