import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.repositories.FrameRepository

class FrameViewModelFactory(private val repository: FrameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FrameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FrameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
