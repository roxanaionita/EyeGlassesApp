package com.example.eyeglassesapp

import FrameViewModelFactory
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.Manifest
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import com.example.eyeglassesapp.ViewModels.CartElementViewModel


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
    private val cartElementViewModel: CartElementViewModel by viewModels {
        CartElementViewModelFactory(CartElementRepository(AppDatabase.getDatabase(applicationContext).cartElementDao()))
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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

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
                cartElementViewModel.fetchTotalCartItemCount(userId)

                cartElementViewModel.totalCartItemCount.observe(this) { count ->
                    Log.d("CartActivity", "Observed cart item count: $count")
                    findViewById<TextView>(R.id.cart_item_count).text = count.toString()
                }

            } else {
                // User ID nu a putut fi obținut
                Log.d("UserIdDebug", "user id failed to be fetched")
            }
        }


        // GESTIONARE AFISARE CORESPUNZATOARE A FRAME INFO
            //get frame id from intent
            frameId = intent.getIntExtra("frameId", 0)
            var savedImagePath: String? = null
            // Observare LiveData pentru FrameWithImages
            frameViewModel.getFrameWithImagesById(frameId)
                .observe(this, Observer { frameWithImages ->
                    if (frameWithImages != null) {
                        // Afiseaza imaginile in ViewPager2 si RecyclerView
                        displayImages(frameWithImages.images)
                        // Afișează informațiile despre cadru
                        displayFrameInfo(frameWithImages)

                        // Hide PC filter options if the frame category is "Sunglasses"
                        if (frameWithImages.frame.category == "Sunglasses") {
                            binding.alegereFiltruPc.visibility = View.GONE
                            binding.pcFilter.visibility = View.GONE
                            binding.noPcFilter.visibility = View.GONE
                            binding.infoPcFilter.visibility = View.GONE
                            isPCFilterSelected = true
                            pcFilterOption = false
                        } else {
                            binding.alegereFiltruPc.visibility = View.VISIBLE
                            binding.pcFilter.visibility = View.VISIBLE
                            binding.noPcFilter.visibility = View.VISIBLE
                            binding.infoPcFilter.visibility = View.VISIBLE
                        }
                        //take first image, remove background and save into no_bg folder
                        val firstImage = frameWithImages.images.firstOrNull()
                        if (firstImage != null) {
                            val fileName = "$frameId.png"
                            savedImagePath = getSavedImagePath(fileName)
                            if (savedImagePath != null) {
                                // Image already transformed
                                Log.d("Transform Pic Debug", "Picture was transformed before")
                                Log.d("path","$savedImagePath")
                            } else {
                                // Perform transformation using API
                                val imagePath = firstImage.imageUri // Assuming imagePath is the local path of the image
                                val bitmap = BitmapFactory.decodeFile(imagePath)
                                removeBackgroundFromImage(bitmap) { resultBitmap ->
                                    // Save the processed image to the "no_bg" folder
                                    if (resultBitmap != null) {
                                        val savedPath = saveImageToMediaStore(resultBitmap)
                                        if (savedPath != null) {
                                            savedImagePath = savedPath
                                            Toast.makeText(this, "Image saved to $savedPath", Toast.LENGTH_SHORT).show()
                                            Log.d("path","Image saved to $savedPath")
                                        } else {
                                            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                                            Log.d("path","Failed to save image")
                                        }
                                    } else {
                                        Toast.makeText(this, "Failed to remove background", Toast.LENGTH_SHORT).show()
                                        Log.d("path","Failed to remove background")
                                    }
                                }
                            }
                        }
                    }
                })

        binding.tryOnRedirect.setOnClickListener {
            if (allPermissionsGranted()) {
                Log.d("Image Debug","${savedImagePath}")
                showRulesDialog(savedImagePath)
            } else {
                showPermissionDialog(savedImagePath)
            }
        }


            // GESTIONARE EXTRAGERE LENTILA DIN BAZA DE DATE IN FUNCTIE DE USER INPUT

            // select type
            binding.oftalmicLens.setOnClickListener {
                selectedLensType = "Oftalmic"
                Log.d("SelectionDebug", "Selected Lens Type: $selectedLensType")
                updatePairPrice()
                binding.oftalmicLens.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            binding.polarizedLens.setOnClickListener {
                selectedLensType = "Polarized"
                Log.d("SelectionDebug", "Selected Lens Type: $selectedLensType")
                updatePairPrice()
                binding.polarizedLens.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            binding.antireflexLens.setOnClickListener {
                selectedLensType = "Antireflex"
                Log.d("SelectionDebug", "Selected Lens Type: $selectedLensType")
                updatePairPrice()
                binding.antireflexLens.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            // select material
            binding.plasticLens.setOnClickListener {
                selectedMaterial = "Plastic"
                Log.d("SelectionDebug", "Selected Lens Material: $selectedMaterial")
                updatePairPrice()
                binding.plasticLens.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            binding.glassLens.setOnClickListener {
                selectedMaterial = "Glass"
                Log.d("SelectionDebug", "Selected Lens Material: $selectedMaterial")
                updatePairPrice()
                binding.glassLens.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            // select uv filter
            binding.noUvFilter.setOnClickListener {
                // uv option was selected
                isUVFilterSelected = true
                uvFilterOption = false
                Log.d("SelectionDebug", "Selected UV: $isUVFilterSelected")
                Log.d("SelectionDebug", "Selected UV: $uvFilterOption")
                updatePairPrice()
                binding.noUvFilter.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            binding.uvFilter.setOnClickListener {
                isUVFilterSelected = true
                uvFilterOption = true
                Log.d("SelectionDebug", "Selected UV: $isUVFilterSelected")
                Log.d("SelectionDebug", "Selected UV: $uvFilterOption")
                updatePairPrice()
                binding.uvFilter.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            // select pc filter
            binding.noPcFilter.setOnClickListener {
                //pc filter selected
                isPCFilterSelected = true
                pcFilterOption = false
                Log.d("SelectionDebug", "Selected PC: $isPCFilterSelected")
                Log.d("SelectionDebug", "Selected PC: $pcFilterOption")
                updatePairPrice()
                binding.noPcFilter.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }

            binding.pcFilter.setOnClickListener {
                isPCFilterSelected = true
                pcFilterOption = true
                Log.d("SelectionDebug", "Selected PC: $isPCFilterSelected")
                Log.d("SelectionDebug", "Selected PC: $pcFilterOption")
                updatePairPrice()
                binding.pcFilter.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
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
            // Check if all options are selected
            if (selectedLensType != null && selectedMaterial != null && isUVFilterSelected && isPCFilterSelected) {
                // All options are selected, proceed with observing lens ID and inserting pair
                observeLensIdAndInsertPair()

            } else {
                // Not all options are selected, show an alert dialog
                showAlertDialog("Check All Options", "Please make sure you have selected lens type, material, and filters before adding to cart.")
            }
        }

        binding.cartRedirectBtn.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        binding.cartItemCount.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        binding.backButton.setOnClickListener{
            finish()
        }



    }
    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun showPermissionDialog(imagePath: String?) {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Needed")
            .setMessage("This app needs the Camera permission to try on the pair. Please allow the permission.")
            .setPositiveButton("OK") { _, _ ->
                requestCameraPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    //rules for the loaded picture
    private fun showRulesDialog(imagePath: String?) {
        AlertDialog.Builder(this)
            .setTitle("Camera Rules for TryOn")
            .setMessage("Load a clear, front faced picture of you.")
            .setPositiveButton("OK") { _, _ ->
                startTryOnActivity(imagePath)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("req code", "$requestCode")
        Log.d("permissions", "${permissions.joinToString()}")
        Log.d("grantResults", "${grantResults.joinToString()}")

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                val savedImagePath = getSavedImagePath("$frameId.png")
                startTryOnActivity(savedImagePath)
            } else {
                // Permission denied
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // Show rationale explaining why the permission is needed
                    // You can display a dialog or a snackbar here
                    Toast.makeText(this, "Permission Denied. Please grant access to the camera.", Toast.LENGTH_SHORT).show()
                } else {
                    // User has selected "Don't ask again", direct them to app settings
                    Toast.makeText(this, "Permission Denied. Please enable camera access in app settings.", Toast.LENGTH_SHORT).show()
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Please enable camera access in app settings to try on the pair.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun startTryOnActivity(imagePath: String?) {
        if (imagePath != null) {
            val intent = Intent(this, TryOnActivity::class.java).apply {
                putExtra("imagePath", imagePath)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Image not transformed yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeLensIdAndInsertPair() {
        lensViewModel.getLensId(selectedLensType!!, selectedMaterial!!, uvFilterOption, pcFilterOption)
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
            cartElementViewModel.fetchTotalCartItemCount(userId)

            cartElementViewModel.totalCartItemCount.observe(this) { count ->
                Log.d("CartActivity", "Observed cart item count: $count")
                findViewById<TextView>(R.id.cart_item_count).text = count.toString()
            }
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

    private fun saveImageToExternalStorage(finalBitmap: Bitmap, fileName: String): String? {
        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File("$root/no_bg")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val file = File(myDir, fileName)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveImageToMediaStore(finalBitmap: Bitmap): String? {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$frameId.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/no_bg"
                )
            }
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    return uri.toString()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    // Function to check if the image already exists in the "no_bg" folder
//    private fun getSavedImagePath(fileName: String): String? {
//        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
//        val filePath = "$root/no_bg/$fileName"
//        val file = File(filePath)
//        return if (file.exists()) {
//            filePath
//        } else {
//            null
//        }
//    }
    // Function to check if the image already exists in the "no_bg" folder
    private fun getSavedImagePath(fileName: String): String? {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val filePath = "$root/no_bg/$fileName"
        val file = File(filePath)
        return if (file.exists()) {
            filePath
        } else {
            null
        }
    }


    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}