package com.example.eyeglassesapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import kotlinx.coroutines.launch

class UserViewModel (private val userRepository: UserRepository) : ViewModel() {
    val allUsers: LiveData<List<UserEntity>> = userRepository.getAllUsers()

//    fun deleteUser(userId: Int) {
//        viewModelScope.launch {
//            userRepository.deleteUserById(userId)
//            // No need to manually update the LiveData; Room does this automatically
//        }
//    }


}