package com.example.eyeglassesapp

import FrameViewModelFactory
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.databinding.ActivityAdminDashboardBinding
import com.example.eyeglassesapp.databinding.ActivityMainBinding
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.OrderRepository
import com.example.eyeglassesapp.repositories.UserRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.concurrent.Executor




class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(
            OrderRepository(
                AppDatabase.getDatabase(applicationContext).orderDao()
            )
        )
    }
    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(
            FrameRepository(
                AppDatabase.getDatabase(applicationContext).frameDao()
            )
        )
    }
    private lateinit var userTotalTextView: TextView
    private lateinit var orderTotalTextView: TextView
    private lateinit var frameTotalTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // CHARTS
        // fetch frame counts
        frameViewModel.fetchFrameCounts()

        frameViewModel.eyeglassesCount.observe(this) { eyeglassesCount ->
            frameViewModel.sunglassesCount.observe(this) { sunglassesCount ->
                // Update pie chart
                populatePieChart(eyeglassesCount, sunglassesCount)
            }
        }
        // fetch nb utilizatori masculini și femei
        userViewModel.fetchUserCounts()

        userViewModel.maleUserCount.observe(this) { maleCount ->
            userViewModel.femaleUserCount.observe(this) { femaleCount ->
                // Actualizați diagrama cu informațiile despre genuri
                populateGenderPieChart(maleCount, femaleCount)
            }
        }


        // REDIRECT BUTTONS
        //redirect to frames list menu - recycler view
        binding.adminFramespage.setOnClickListener {
            val intent = Intent(this, Admin_FramesPage::class.java)
            startActivity(intent)
            //finish() // End this activity
            //daca folosesc finish, activitatea este stearsa din coada de activitati
            //rezultat: onBackPressed se intoarce la SplashScreen()
        }
        //redirect to list of users - extract from room
        binding.adminUserspage.setOnClickListener {
            val intent = Intent(this, Admin_UsersPage::class.java)
            startActivity(intent)
            //finish() // End this activity
        }
        //redirect from orders list page - extract from room
        binding.adminOrderspage.setOnClickListener {
            val intent = Intent(this, Admin_OrdersPage::class.java)
            startActivity(intent)
            //finish() // End this activity
        }



        //get total user count
        userTotalTextView = binding.totalUsers
        userViewModel.getTotalUserCount()
        userViewModel.totalUserCount.observe(this) { totalCount ->
            totalCount?.let {
                userTotalTextView.text = it.toString()
            }

        }

        //get total order count
        orderTotalTextView = binding.totalOrders
        orderViewModel.getTotalOrderCount()
        orderViewModel.totalOrderCount.observe(this) { totalOrderCount ->
            totalOrderCount?.let {
                orderTotalTextView.text = it.toString()
            }

        }

        //get total frame count
        frameTotalTextView = binding.totalFrames
        frameViewModel.getTotalFrameCount()
        frameViewModel.totalFrameCount.observe(this) { totalFrameCount ->
            totalFrameCount?.let {
                frameTotalTextView.text = it.toString()
            }

        }

        // Logout button
        binding.logoutImage.setOnClickListener {
            // Trigger the logout process
            userViewModel.logout()
        }
        // Observe the logout status to handle navigation
        userViewModel.logoutStatus.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                // Navigate to the login screen
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish() // Optional: Finish the current activity
            }
        }
    }

    private fun populatePieChart(eyeglassesCount: Int, sunglassesCount: Int) {
        // Use the counts to update your pie chart
        val pieChart = findViewById<PieChart>(R.id.pie_chart_1)
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(eyeglassesCount.toFloat(), "Eyeglasses"))
        entries.add(PieEntry(sunglassesCount.toFloat(), "Sunglasses"))

        // Define colors for the pie slices
        val colors = listOf(
            0xFF0000FF.toInt(),  // Blue
            0xFF00FF00.toInt()   // Green
        )

        // Set legend
        val dataSet = PieDataSet(entries, "Frame Types")
        dataSet.colors = colors

        val data = PieData(dataSet)
        pieChart.data = data
        //daca e setat to true, apare un text cu "Description Label:"
        //inainte de legenda
        pieChart.description.isEnabled = false
        //daca e false, in loc de procent, apare numarul de ochelari
        pieChart.setUsePercentValues(true)
        pieChart.invalidate() // Refresh the chart
    }


    private fun populateGenderPieChart(maleCount: Int, femaleCount: Int) {
        val pieChartGender = findViewById<PieChart>(R.id.pie_chart_2)
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(maleCount.toFloat(), "Male"))
        entries.add(PieEntry(femaleCount.toFloat(), "Female"))

        val colors = listOf(
            0xFFFFFF00.toInt(),  // Yellow
            0xFFFF00FF.toInt()   // Magenta
        )

        //setare legenda cu label
        val dataSet = PieDataSet(entries, "User Genders")
        dataSet.colors = colors

        val data = PieData(dataSet)
        pieChartGender.data = data
        pieChartGender.description.isEnabled = false
        pieChartGender.setUsePercentValues(false)
        pieChartGender.invalidate()
    }



}