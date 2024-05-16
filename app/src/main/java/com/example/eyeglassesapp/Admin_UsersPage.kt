package com.example.eyeglassesapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.databinding.ActivityAdminUsersPageBinding
import com.example.eyeglassesapp.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Admin_UsersPage : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUsersPageBinding
    private lateinit var userAdapter: UserAdapter
    private val DELETE_USER_REQUEST_CODE = 100
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdminUsersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userAdapter = UserAdapter(emptyList()){user->
            val intent = Intent(this, UserInfoPage::class.java)
            intent.putExtra("userId", user.userId)
            startActivity(intent)

        }
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = userAdapter

        userViewModel.allUsers.observe(this){userList ->
            userAdapter.updateUsers(userList)

        }

        binding.backButton.setOnClickListener{
            finish()
        }

    }

    //if i deleted a user in the See Users's Detais page
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == DELETE_USER_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val deletedUserId = data?.getIntExtra("deletedUserId",-1)
            if(deletedUserId != null && deletedUserId != -1){
                userAdapter.deleteUser(deletedUserId)
            }
        }
    }
}