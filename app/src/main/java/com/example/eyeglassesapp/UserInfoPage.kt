package com.example.eyeglassesapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.dao.OrderDao
import com.example.eyeglassesapp.dao.UserDao
import com.example.eyeglassesapp.databinding.ActivityUserInfoPageBinding
import com.example.eyeglassesapp.entities.OrderEntity
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.OrderRepository
import com.example.eyeglassesapp.repositories.UserRepository
import kotlinx.coroutines.launch
import java.io.File

//FOLOSIT PENTRU ADMIN - AFISAM FIECARE USER CU DETALIILE SI COMENZILE PERSONALE
class UserInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoPageBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository
    private lateinit var orderDao: OrderDao
    private lateinit var orderRepository: OrderRepository
    private lateinit var userFirebase : UserEntity
    private lateinit var orderAdapter: AdminUserOrdersAdapter
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(orderRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(applicationContext)

        // Initialize User DAO
        userDao = database.userDao()
        // Initialize User Repository
        userRepository = UserRepository(userDao)

        orderDao = database.orderDao()
        orderRepository = OrderRepository(orderDao)


        // Initialize RECYCLER VIEW
        val recyclerView: RecyclerView = findViewById(R.id.orders_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val orders: MutableList<OrderEntity> = mutableListOf()
        orderAdapter = AdminUserOrdersAdapter(orders, orderViewModel, this)
        recyclerView.adapter = orderAdapter

        val emptyView: View = findViewById(R.id.empty_view_orders)
        val emptyMessage: TextView = findViewById(R.id.empty_message)

        // Get userId from the other page
        val userId = intent.extras?.getInt("userId") ?: -1

        if (userId != -1) {

            //INCARCA INTAI ORDER-URILE

            orderViewModel.getOrdersByUserId(userId)
            orderViewModel.ordersLiveData.observe(this) { newOrders ->
                // Update adapter with new orders
                orders.clear()
                orders.addAll(newOrders)
                orderAdapter.notifyDataSetChanged()
                if (orderAdapter.itemCount == 0) {
                    emptyView.visibility = View.VISIBLE
                    emptyMessage.text = "No items to display."
                } else {
                    emptyView.visibility = View.GONE
                }
            }


            lifecycleScope.launch {
                // Fetch the user data
                val userData = userRepository.getUserById(userId)
                userData?.let { user ->
                    val userIdTextView : TextView = findViewById(R.id.user_id)
                    val userUsernameTextView: TextView = findViewById(R.id.user_username)
                    val userEmailTextView: TextView = findViewById(R.id.user_email)
                    val userGenderTextView : TextView = findViewById(R.id.user_gender)
                    val userImage: ImageView = findViewById(R.id.user_icon)


                    userIdTextView.text = "User No #${user.userId}"
                    userUsernameTextView.text = "Username: ${user.username}"
                    userEmailTextView.text = "Email: ${user.email}"
                    userGenderTextView.text = "Gender: ${user.gender}"

                    //load user pic
                    val userPhoto = user.profilePictureUrl
                    if (userPhoto != null) {
                        loadUserImage(userImage,userPhoto)
                    }else{
                        //se incarca poze default in functie de gen
                        //in cazul in care utiizatorul nu si-a setat o imagine de profil
                        if (user.gender == "male")
                            userImage.setImageResource(R.drawable.boss)
                        if(user.gender == "female")
                            userImage.setImageResource(R.drawable.businesswoman)
                    }


                }
            }

        }else{
            Toast.makeText(this, "Error: User information could not be loaded.", Toast.LENGTH_LONG).show()
            finish()
        }

        binding.backButton.setOnClickListener{
            onBackPressed()
        }
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(userId)
        }
    }

    private fun showDeleteConfirmationDialog(userId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this user?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteUser(userId)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteUser(userId: Int) {
        lifecycleScope.launch {

            //stergere din firestore
            //not null object
            userFirebase = userRepository.getUserById(userId)!!
            userRepository.deleteUserFromFirestore(userFirebase.firebaseUid)

            //delete user din room db
            userRepository.deleteUserById(userId)
            // Prepare the result to be sent back to previous activity
            val resultIntent = Intent()
            resultIntent.putExtra("deletedUserId", userId) // Send back the ID of the deleted user to update adapter
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
    fun loadUserImage(imageView: ImageView, imagePath: String) {
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imageView.setImageBitmap(myBitmap)
        } else {
            // Placeholder
            imageView.setImageResource(R.drawable.default_image_placeholder)
        }
    }

}