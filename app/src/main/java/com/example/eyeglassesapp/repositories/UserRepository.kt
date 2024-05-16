package com.example.eyeglassesapp.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.eyeglassesapp.dao.UserDao
import com.example.eyeglassesapp.entities.UserEntity
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val userDao: UserDao) {

    private val firestoreDatabase = FirebaseFirestore.getInstance()

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        return userDao.deleteUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    //get user by user uid from firebase
    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserByUid(userId)
    }

    fun getAllUsers() : LiveData<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    suspend fun deleteUserById(userId: Int){
        userDao.deleteUserById(userId)

    }
     fun deleteUserFromFirestore(firebaseUid: String) {
        firestoreDatabase.collection("users").document(firebaseUid).delete()
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting document", e)
            }
    }
    suspend fun getUserIdByEmail(userEmail: String): Int? {
        return userDao.getUserIdByEmail(userEmail)
    }
    //get user bu user_id attribute
    suspend fun getUserByUserId(userId: Int): UserEntity? {
        return userDao.getUserByUserId(userId)
    }

    suspend fun updateUserProfilePicture(userId: Int, imagePath: String) {
        userDao.updateUserProfilePicture(userId, imagePath)
    }

    suspend fun getTotalUserCount(): Int{
        return userDao.getTotalUserCount()
    }

    suspend fun getMaleUserCount(): Int {
        return userDao.getMaleUserCount()
    }

    suspend fun getFemaleUserCount(): Int {
        return userDao.getFemaleUserCount()
    }
}