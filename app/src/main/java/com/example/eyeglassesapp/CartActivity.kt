package com.example.eyeglassesapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.CartElementViewModel
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.databinding.ActivityCartBinding
import com.example.eyeglassesapp.databinding.ActivityCreatePairBinding
import com.example.eyeglassesapp.entities.CartElementEntity
import com.example.eyeglassesapp.entities.OrderEntity
import com.example.eyeglassesapp.repositories.CartElementRepository
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import com.example.eyeglassesapp.repositories.OrderRepository
import com.example.eyeglassesapp.repositories.PairRepository
import com.example.eyeglassesapp.repositories.UserRepository
import kotlinx.coroutines.launch
import java.util.Date

class CartActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }
    private val cartElementViewModel: CartElementViewModel by viewModels {
        CartElementViewModelFactory(CartElementRepository(AppDatabase.getDatabase(applicationContext).cartElementDao()))
    }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(AppDatabase.getDatabase(applicationContext).orderDao()))
    }
    private lateinit var binding : ActivityCartBinding
    private var userId : Int = 0

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartElementAdapter
    private lateinit var pairRepository: PairRepository
    private lateinit var frameRepository: FrameRepository
    private lateinit var imageRepository: ImageRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pairRepository = PairRepository(AppDatabase.getDatabase(applicationContext).pairDao())
        frameRepository = FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao())
        imageRepository = ImageRepository(AppDatabase.getDatabase(applicationContext).imageDao())


        //se incearca extragerea user id daca a fost trimis
        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            // daca nu a fost trimis, il gasim iar
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

        }


        // Initialize RecyclerView
        cartRecyclerView = findViewById(R.id.cart_elements_view)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter
        cartAdapter = CartElementAdapter(emptyList(), pairRepository,frameRepository,imageRepository, cartElementViewModel)
        cartRecyclerView.adapter = cartAdapter

        val emptyView: View = findViewById(R.id.empty_view)
        val emptyMessage: TextView = findViewById(R.id.empty_message)


        //GET CART ELEMENTS BU USER ID
        // Observe cart elements and update RecyclerView
        cartElementViewModel.getCartElementsByUserId(userId).observe(this) { cartElements ->
            cartAdapter.updateCartElements(cartElements)
            // Check the adapter's item count to determine visibility of empty view
            if (cartAdapter.itemCount == 0) {
                emptyView.visibility = View.VISIBLE
                emptyMessage.text = "No items to display."
            } else {
                emptyView.visibility = View.GONE
            }

            // Calculate subtotal, delivery charge, and total price
            val subtotal = calculateSubtotal(cartElements)
            val deliveryCharge = 5.0 // Fixed delivery charge
            val totalPrice = subtotal + deliveryCharge

            // Update TextViews
            binding.subtotal.text = String.format("$%.2f", subtotal)
            binding.delivery.text = String.format("$%.2f", deliveryCharge)
            binding.totalOrderPrice.text = String.format("$%.2f", totalPrice)
        }

        binding.submitBtn.setOnClickListener{
            onSubmitButtonClicked()
        }
        binding.arrowBack.setOnClickListener{
            finish()
        }


    }

    private fun onSubmitButtonClicked() {
        if (cartAdapter.itemCount == 0) {
            Toast.makeText(this, "Can't send empty order", Toast.LENGTH_SHORT).show()
        } else {
            val cartElements = cartAdapter.getCartElements()
            val totalNumberProducts = cartElements.size
            val subtotal = calculateSubtotal(cartElements)
            val totalPrice = subtotal + 5.0 // Subtotal + delivery charge

            // Create the order using the OrderViewModel
            orderViewModel.createOrder(userId, totalNumberProducts, totalPrice).observe(this
            ) { orderId ->
                if (orderId != null) {
                    // Order created successfully, update pairs and delete cart elements
                    updatePairsWithOrder(orderId, cartElements)
                    deleteCartElements()
                    showOrderSubmittedDialog()
                } else {
                    // Order creation failed
                    Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun showOrderSubmittedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Order Submitted")
            .setMessage("Your order has been submitted successfully.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun updatePairsWithOrder(orderId: Long, cartElements: List<CartElementEntity>) {
        for (cartElement in cartElements) {
            val pairId = cartElement.pairId
            val quantity = cartElement.quantity

            // Update pair with order ID and quantity
            cartElementViewModel.viewModelScope.launch {
                pairRepository.updatePairWithOrder(pairId, orderId, quantity)
            }
        }
    }

    private fun deleteCartElements() {
        cartElementViewModel.deleteAllCartElements()
    }

    private fun calculateSubtotal(cartElements: List<CartElementEntity>): Double {
        var subtotal = 0.0
        for (cartElement in cartElements) {
            subtotal += cartElement.quantity * cartElement.price
        }
        return subtotal
    }

}