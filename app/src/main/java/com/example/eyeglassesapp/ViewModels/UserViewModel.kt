package com.example.eyeglassesapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel (private val userRepository: UserRepository) : ViewModel() {
    val allUsers: LiveData<List<UserEntity>> = userRepository.getAllUsers()


    private val _userId = MutableLiveData<Int?>()
    val userId: LiveData<Int?> = _userId

    // Function to retrieve user ID based on email
    fun getUserIdFromFirebaseEmail() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userEmail = firebaseUser.email
            if (userEmail != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val userId = userRepository.getUserIdByEmail(userEmail)
                        _userId.postValue(userId)
                    } catch (e: Exception) {
                        _userId.postValue(null)
                    }
                }
            }
        }
    }

    //get user by user_id attribute
    private val _userLiveData = MutableLiveData<UserEntity?>()
    val userLiveData: LiveData<UserEntity?>
        get() = _userLiveData

    fun getUserById(userId: Int) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            _userLiveData.value = user
        }
    }



    //update profile pic
    fun updateUserProfilePicture(userId: Int, imagePath: String) {
        viewModelScope.launch {
            userRepository.updateUserProfilePicture(userId, imagePath)
        }
    }

    //get total user count for admin dashboard activity
    private val _totalUserCount = MutableLiveData<Int>()
    val totalUserCount : LiveData<Int> = _totalUserCount

    fun getTotalUserCount(){
        viewModelScope.launch {
            try {
                val count = userRepository.getTotalUserCount()
                _totalUserCount.postValue(count)
            }catch (e:Exception){
                _totalUserCount.postValue(0)
            }
        }
    }


    private val _maleUserCount = MutableLiveData<Int>()
    val maleUserCount: LiveData<Int> = _maleUserCount

    private val _femaleUserCount = MutableLiveData<Int>()
    val femaleUserCount: LiveData<Int> = _femaleUserCount

    fun fetchUserCounts() {
        viewModelScope.launch {
            try {
                val maleCount = userRepository.getMaleUserCount()
                _maleUserCount.postValue(maleCount)

                val femaleCount = userRepository.getFemaleUserCount()
                _femaleUserCount.postValue(femaleCount)
            } catch (e: Exception) {
                _maleUserCount.postValue(0)
                _femaleUserCount.postValue(0)
            }
        }
    }

    //logout functionality
    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> = _logoutStatus

    fun logout() {
        // Clear Firebase authentication state
        FirebaseAuth.getInstance().signOut()

        // Notify observers that logout was successful
        _logoutStatus.postValue(true)
    }
}