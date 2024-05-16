package com.example.eyeglassesapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.OrderViewModel
import com.example.eyeglassesapp.ViewModels.PairViewModel
import com.example.eyeglassesapp.dao.CartElementDao
import com.example.eyeglassesapp.dao.FrameDao
import com.example.eyeglassesapp.dao.ImageDao
import com.example.eyeglassesapp.dao.LensDao
import com.example.eyeglassesapp.dao.PairDao
import com.example.eyeglassesapp.databinding.ActivityUserOrderDetailsBinding
import com.example.eyeglassesapp.databinding.ActivityUserOrdersBinding
import com.example.eyeglassesapp.repositories.CartElementRepository
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import com.example.eyeglassesapp.repositories.LensRepository
import com.example.eyeglassesapp.repositories.OrderRepository
import com.example.eyeglassesapp.repositories.PairRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserOrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserOrderDetailsBinding

    private lateinit var database: AppDatabase

    private lateinit var frameDao: FrameDao
    private lateinit var imageDao : ImageDao
    private lateinit var pairDao: PairDao
    private lateinit var lensDao: LensDao
    private lateinit var cartElementDao: CartElementDao

    private lateinit var pairAdapter: OrderPairAdapter
    private lateinit var pairViewModel: PairViewModel

    private lateinit var pairRepository: PairRepository
    private lateinit var cartElementRepository: CartElementRepository
    private lateinit var frameRepository: FrameRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var lensRepository: LensRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(applicationContext)

        // Initialize DAOs
        frameDao = database.frameDao()
        imageDao = database.imageDao()
        pairDao = database.pairDao()
        lensDao = database.lensDao()
        cartElementDao = database.cartElementDao()

        // Initialize Repositories
        frameRepository = FrameRepository(frameDao)
        pairRepository = PairRepository(pairDao)
        imageRepository = ImageRepository(imageDao)
        lensRepository = LensRepository(lensDao)
        cartElementRepository = CartElementRepository(cartElementDao)

        // Receive orderId from intent extras
        val orderId = intent.getIntExtra("orderId",-1)
        Log.d("OrderIdDebug","order id : ${orderId}")

        // Initialize RecyclerView and Adapter
        pairAdapter = OrderPairAdapter(emptyList(), frameRepository, lensRepository, imageRepository)
        binding.pairsRecycler.layoutManager = LinearLayoutManager(this)
        binding.pairsRecycler.adapter = pairAdapter

        // ITEM SPACING USING RESOURCE DIMENS XML FILE
        val itemDecoration = VerticalSpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.recycler_view_spacing))
        binding.pairsRecycler.addItemDecoration(itemDecoration)


        // Initialize ViewModel and Repository
        pairViewModel = PairViewModel(pairRepository,cartElementRepository) // Initialize your PairViewModel

        // Observe pairs based on orderId from ViewModel
        orderId?.let { fetchPairsByOrderId(it) }


        binding.backButton.setOnClickListener{
            finish()
        }
        binding.ordersRedirect.setOnClickListener{
            finish()
        }
    }

    private fun fetchPairsByOrderId(orderId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val pairs = pairViewModel.getPairsByOrderId(orderId)
                pairAdapter.updatePairs(pairs)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
    private class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: android.view.View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = verticalSpaceHeight
        }
    }
}
