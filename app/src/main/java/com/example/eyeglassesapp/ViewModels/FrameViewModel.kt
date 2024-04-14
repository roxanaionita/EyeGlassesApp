package com.example.eyeglassesapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrameViewModel(private val repository: FrameRepository) : ViewModel() {

    val allFramesWithImages: LiveData<List<FrameWithImages>> = repository.getAllFramesWithImages()


    private val _insertedFrameId = MutableLiveData<Long>()
    val insertedFrameId: LiveData<Long> = _insertedFrameId

    fun insertFrame(frame: FrameEntity) = viewModelScope.launch(Dispatchers.IO) {
        val newFrameId = repository.insertFrame(frame)
        _insertedFrameId.postValue(newFrameId)
    }

    // Funcție pentru actualizarea unui frame
    fun updateFrame(frame: FrameEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateFrame(frame)
    }

    // Funcție pentru ștergerea unui frame
    fun deleteFrame(frame: FrameEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteFrame(frame)
    }

    // Funcție pentru a obține un frame după ID
    // Rezultatul poate fi returnat folosind LiveData sau o altă metodă de comunicare asincronă cu UI-ul
    fun getFrameById(frameId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val frame = repository.getFrameById(frameId)
        withContext(Dispatchers.Main) {
            // Actualizează UI-ul aici sau folosește LiveData pentru a expune datele către UI
        }
    }

    // Funcție pentru a obține toate frame-urile
    // o metodă asincronă pentru a expune datele
    fun getAllFrames() = viewModelScope.launch(Dispatchers.IO) {
        val frames = repository.getAllFrames()
        withContext(Dispatchers.Main) {
            // Actualizează UI-ul aici sau folosește LiveData pentru a expune datele către UI
        }
    }
}