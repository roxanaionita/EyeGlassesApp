package com.example.eyeglassesapp.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.AppDatabase
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity
import com.example.eyeglassesapp.repositories.FaceShapeRepository
import kotlinx.coroutines.launch

class FaceShapeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FaceShapeRepository

    init {
        val faceShapeDao = AppDatabase.getDatabase(application).faceShapeDao()
        repository = FaceShapeRepository(faceShapeDao)
    }

    fun insertAllFaceShapes(faceShapes: List<FaceShapeEntity>) {
        viewModelScope.launch {
            repository.insertAll(faceShapes)
        }
    }

    fun getAllFaceShapes() = viewModelScope.launch {
        repository.getAllFaceShapes()
    }

    fun getFaceShapeIdByName(faceShape: String): LiveData<Int> {
        val faceShapeIdLiveData = MutableLiveData<Int>()
        viewModelScope.launch {
            val faceShapeId = repository.getFaceShapeIdByName(faceShape)
            faceShapeIdLiveData.postValue(faceShapeId)
        }
        return faceShapeIdLiveData
    }


}