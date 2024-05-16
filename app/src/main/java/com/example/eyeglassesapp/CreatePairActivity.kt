package com.example.eyeglassesapp

import FrameViewModelFactory
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.eyeglassesapp.ViewModels.FrameViewModel
import com.example.eyeglassesapp.entities.ImageEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.example.eyeglassesapp.ViewModels.LensViewModel
import com.example.eyeglassesapp.ViewModels.PairViewModel
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.databinding.ActivityAdminUsersPageBinding
import com.example.eyeglassesapp.databinding.ActivityCreatePairBinding
import com.example.eyeglassesapp.entities.PairEntity
import com.example.eyeglassesapp.repositories.CartElementRepository
import com.example.eyeglassesapp.repositories.LensRepository
import com.example.eyeglassesapp.repositories.PairRepository
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers

class CreatePairActivity : AppCompatActivity() {

    private val frameViewModel: FrameViewModel by viewModels {
        FrameViewModelFactory(FrameRepository(AppDatabase.getDatabase(applicationContext).frameDao()))
    }
    private val lensViewModel: LensViewModel by viewModels {
        LensViewModelFactory(LensRepository(AppDatabase.getDatabase(applicationContext).lensDao()))
    }
    private val pairViewModel: PairViewModel by viewModels {
        PairViewModelFactory(PairRepository(AppDatabase.getDatabase(applicationContext).pairDao()),
            CartElementRepository(AppDatabase.getDatabase(applicationContext).cartElementDao()))
    }
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }
    private lateinit var binding : ActivityCreatePairBinding

    private var frameId: Int = 0
    private var userId : Int = 0

    private var selectedLensType: String? = null
    private var selectedMaterial: String? = null
    private var isUVFilterSelected: Boolean = false
    private var isPCFilterSelected: Boolean = false
    private var pcFilterOption : Boolean = false
    private var uvFilterOption : Boolean = false

    private var rightDiopter : Double = 0.0
    private var leftDiopter : Double = 0.0

    private var lastLensId : Int? = null
    private var finalPairPrice : Double = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreatePairBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //GESTIONARE EXTRAGERE USER ID
        userViewModel.getUserIdFromFirebaseEmail()
        userViewModel.userId.observe(this) { fetchedUserId ->
            if (fetchedUserId != null) {
                // User ID a fost obținut cu succes
                userId = fetchedUserId

                // debug
                Log.d("UserIdDebug", "User id: $userId")

            } else {
                // User ID nu a putut fi obținut
                Log.d("UserIdDebug", "user id failed to be fetched")
            }
        }


        // GESTIONARE AFISARE CORESPUNZATOARE A FRAME INFO

            frameId = intent.getIntExtra("frameId", 0)

            // Observare LiveData pentru FrameWithImages
            frameViewModel.getFrameWithImagesById(frameId)
                .observe(this, Observer { frameWithImages ->
                    if (frameWithImages != null) {
                        // Afiseaza imaginile in ViewPager2 si RecyclerView
                        displayImages(frameWithImages.images)
                        // Afișează informațiile despre cadru
                        displayFrameInfo(frameWithImages)
                    }
                })


            // GESTIONARE EXTRAGERE LENTILA DIN BAZA DE DATE IN FUNCTIE DE USER INPUT

            // select type
            binding.oftalmicLens.setOnClickListener {
                selectedLensType = "Oftalmic"
                Log.d("SelectionDebug", "Selected Lens Type: $selectedLensType")
                updatePairPrice()
            }

            binding.polarizedLens.setOnClickListener {
                selectedLensType = "Polarized"
                Log.d("SelectionDebug", "Selected Lens Type: $selectedLensType")
                updatePairPrice()
            }

            binding.antireflexLens.setOnClickListener {
                selectedLensType = "Antireflex"
                Log.d("SelectionDebug", "Selected Lens Type: $selectedLensType")
                updatePairPrice()
            }

            // select material
            binding.plasticLens.setOnClickListener {
                selectedMaterial = "Plastic"
                Log.d("SelectionDebug", "Selected Lens Material: $selectedMaterial")
                updatePairPrice()
            }

            binding.glassLens.setOnClickListener {
                selectedMaterial = "Glass"
                Log.d("SelectionDebug", "Selected Lens Material: $selectedMaterial")
                updatePairPrice()
            }

            // select uv filter
            binding.noUvFilter.setOnClickListener {
                // uv option was selected
                isUVFilterSelected = true
                uvFilterOption = false
                Log.d("SelectionDebug", "Selected UV: $isUVFilterSelected")
                Log.d("SelectionDebug", "Selected UV: $uvFilterOption")
                updatePairPrice()
            }

            binding.uvFilter.setOnClickListener {
                isUVFilterSelected = true
                uvFilterOption = true
                Log.d("SelectionDebug", "Selected UV: $isUVFilterSelected")
                Log.d("SelectionDebug", "Selected UV: $uvFilterOption")
                updatePairPrice()
            }

            // select pc filter
            binding.noPcFilter.setOnClickListener {
                //pc filter selected
                isPCFilterSelected = true
                pcFilterOption = false
                Log.d("SelectionDebug", "Selected PC: $isPCFilterSelected")
                Log.d("SelectionDebug", "Selected PC: $pcFilterOption")
                updatePairPrice()
            }

            binding.pcFilter.setOnClickListener {
                isPCFilterSelected = true
                pcFilterOption = true
                Log.d("SelectionDebug", "Selected PC: $isPCFilterSelected")
                Log.d("SelectionDebug", "Selected PC: $pcFilterOption")
                updatePairPrice()
            }


            //GESTIONARE USER INPUT PENTRU NUMBER PICKER - DIOPTERS
            //SPINNER

            val spinnerRight: Spinner = binding.spinnerRightDiopter
            val spinnerLeft: Spinner = binding.spinnerLeftDiopter

            // Creează un ArrayAdapter pentru a afișa opțiunile în Spinner
            val values = (-10..10).map { it.toString() }.toTypedArray()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Setează adapterul Spinner-ului dreapta
            spinnerRight.adapter = adapter
            spinnerRight.setSelection(10) // Selectează 0 (zero) inițial

            // Setează adapterul Spinner-ului stânga
            spinnerLeft.adapter = adapter
            spinnerLeft.setSelection(10) // Selectează 0 (zero) inițial

            // Adaugă un listener pentru selecția din Spinner
            spinnerRight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val rightValue = parent?.getItemAtPosition(position).toString()
                    Log.d("right diopter Debug", "Right Diopter: $rightValue")
                    rightDiopter = rightValue.toDouble()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    rightDiopter = 0.0
                }
            }

            spinnerLeft.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val leftValue = parent?.getItemAtPosition(position).toString()
                    Log.d("left diopter Debug", "Left Diopter: $leftValue")
                    leftDiopter = leftValue.toDouble()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    leftDiopter = 0.0
                }
            }


        //GESTIONARE CREARE OBIECT PERECHE ON ADD TO CART CLICKED
        binding.addToCart.setOnClickListener{
            //get lens id
            observeLensIdAndInsertPair()
        }

        binding.cartRedirectBtn.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }



    }

    private fun observeLensIdAndInsertPair() {
        lensViewModel.getLensId(selectedLensType!!, selectedMaterial!!, isUVFilterSelected, isPCFilterSelected)
            .observe(this, Observer { lensId ->
                if (lensId != null) {
                    lastLensId = lensId
                    Log.d("LensIdDebug", "LensId: $lensId")

                    // Dacă lensId-ul a fost obținut cu succes, putem crea și insera PairEntity
                    createAndInsertPair()
                } else {
                    Toast.makeText(this, "LensId nu a putut fi obținut.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun createAndInsertPair() {
        // Verificam dacă avem un lensId valid pentru a crea PairEntity
        lastLensId?.let { lensId ->
            val frameId = frameId // ID-ul cadrului ales
            val rightDiopter = rightDiopter // Valoarea dioptriei dreapta
            val leftDiopter = leftDiopter // Valoarea dioptriei stânga
            val orderId: Int? = null // Ordinul încă nu există
            val price = finalPairPrice

            // Creează obiectul PairEntity cu valorile obținute
            val pair = PairEntity(
                frameId = frameId,
                lensId = lensId,
                rightDiopter = rightDiopter,
                leftDiopter = leftDiopter,
                orderId = orderId,
                price = price,
                quantity = 1
            )

            Log.d("PairObject", "PairEntity: $pair")

            // ViewModel pt insert operation
            pairViewModel.insertPairAndCartElement(pair, userId)

            // Observe the result of the insertion operation using the insertedPairId LiveData
            pairViewModel.insertedPairId.observe(this, Observer { insertedPairId ->
                if (insertedPairId != null) {
                    Log.d("PairInsertion", "Inserted Pair ID: $insertedPairId")
                    Toast.makeText(this, "Pair added to cart successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add pair to cart", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Log.e("LensIdDebug", "LensId is null")
        }
    }

    private fun updatePairPrice() {
        if (selectedLensType != null && selectedMaterial != null && isUVFilterSelected && isPCFilterSelected) {
            lensViewModel.getPriceForLens(selectedLensType!!, selectedMaterial!!, uvFilterOption, pcFilterOption).observe(this) { lensPrice ->
                if (lensPrice != null) {
                    Log.d("LensPriceDebug", "Lens Price: $lensPrice")

                    frameViewModel.getFrameWithImagesById(frameId).observe(this, Observer { frameWithImages ->
                        if (frameWithImages != null) {
                            val baseFramePrice = frameWithImages.frame.price
                            val pairPrice = baseFramePrice + lensPrice
                            Log.d("PairPriceDebug", "Pair Price: $pairPrice")
                            finalPairPrice = pairPrice
                            updateTotalPrice(lensPrice,pairPrice)
                        } else {
                            Toast.makeText(this, "Detaliile cadrului nu pot fi obținute.", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Prețul lentilei nu poate fi obținut.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Selectați toate opțiunile.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateTotalPrice(lensPrice : Double, pairPrice: Double) {
        binding.lensPriceValue.text = "$lensPrice"
        binding.priceValue.text = "$pairPrice"

    }


    private fun displayFrameInfo(frameWithImages: FrameWithImages) {
        findViewById<TextView>(R.id.product_brand).text = frameWithImages.frame.brand
        findViewById<TextView>(R.id.product_Price).text = "$${frameWithImages.frame.price}"
        findViewById<TextView>(R.id.product_model).text = frameWithImages.frame.model
        findViewById<TextView>(R.id.product_color).text = frameWithImages.frame.colour
        findViewById<TextView>(R.id.product_gender).text = frameWithImages.frame.gender
        findViewById<TextView>(R.id.product_category).text = frameWithImages.frame.category
        findViewById<TextView>(R.id.product_material).text = frameWithImages.frame.material
        findViewById<TextView>(R.id.product_Description).text = frameWithImages.frame.description

    }

    private fun displayImages(images: List<ImageEntity>) {
        // Images in Pager
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val dotsIndicator: DotsIndicator = findViewById(R.id.dot_indicator)

        val imagePagerAdapter = ImagePagerAdapter(images)
        viewPager.adapter = imagePagerAdapter
        dotsIndicator.setViewPager2(viewPager)

        // Other images in RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.otherImagesRecyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        val adapter = OtherImagesAdapter(images.subList(1, images.size))
        recyclerView.adapter = adapter
    }
}