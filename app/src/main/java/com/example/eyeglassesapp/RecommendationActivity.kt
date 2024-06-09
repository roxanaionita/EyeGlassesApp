package com.example.eyeglassesapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.eyeglassesapp.ViewModels.UserViewModel
import com.example.eyeglassesapp.entities.UserEntity
import com.example.eyeglassesapp.repositories.UserRepository
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class RecommendationActivity : AppCompatActivity() {
    private lateinit var overlayView: OverlayView
    private lateinit var pickImageButton: Button
    private lateinit var redirectButton: Button
    private lateinit var imageView: ImageView
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(AppDatabase.getDatabase(applicationContext).userDao()))
    }
    private var userId: Int = 0
    private var currentUser: UserEntity? = null

    private val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("face_landmarker.task")

    private val optionsBuilder =
        FaceLandmarker.FaceLandmarkerOptions.builder().setBaseOptions(baseOptionsBuilder.build())
            .setMinFaceDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setMinFacePresenceConfidence(0.5f)
            .setNumFaces(1)
            .setRunningMode(RunningMode.IMAGE)

    private val options = optionsBuilder.build()
    private var faceLandmarker: FaceLandmarker? = null


    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    uri?.let { mediaUri ->
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, mediaUri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(contentResolver, mediaUri)
                        }.copy(Bitmap.Config.ARGB_8888, true)

                        bitmap?.let {
                            val mpImage = BitmapImageBuilder(it).build()
                            val result = faceLandmarker?.detect(mpImage)

                            withContext(Dispatchers.Main) {
                                imageView.load(it)
                                //result.faceLandmarks() lista cu toate landmarks
                                if (result != null && result.faceLandmarks().isNotEmpty()) {
                                    Log.d("FaceLandmarks", "$result")

                                    overlayView.setResults(result, it.height, it.width)

//                                    val faceShape = predictFaceShape(result)
//                                    Log.d("FaceShape", "Predicted Face Shape: $faceShape")
//                                    redirectButton.visibility = View.VISIBLE
                                    val faceShapeAnalysis = predictFaceShape(result)
                                    val faceShape = faceShapeAnalysis.first
                                    val measurements = faceShapeAnalysis.second

                                    Log.d("FaceShape", "Predicted Face Shape: $faceShape")
                                    saveFaceShapeToUser(measurements, faceShape)
                                    redirectButton.visibility = View.VISIBLE

                                } else {
                                    overlayView.clear() // Clear the overlay view
                                    redirectButton.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation)

        faceLandmarker = FaceLandmarker.createFromOptions(this, options)
        overlayView = findViewById(R.id.overlay)
        pickImageButton = findViewById(R.id.pickImg)
        redirectButton = findViewById(R.id.redirectButton)
        imageView = findViewById(R.id.imageView)


        redirectButton.visibility = View.GONE

        // Retrieve the User ID from the Intent
        userId = intent.getIntExtra("userId", 0)

        // Observe userLiveData
        userViewModel.userLiveData.observe(this) { user ->
            user?.let {
                currentUser = it
            }
        }

        // Fetch user data
        userViewModel.getUserById(userId)

        pickImageButton.setOnClickListener {
            getContent.launch(arrayOf("image/*"))
        }

        redirectButton.setOnClickListener {
            val intent = Intent(this, UserReport::class.java)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }
    }

    private fun calculateFaceLengthWithHairlineEstimate(
        point9: NormalizedLandmark,
        point10: NormalizedLandmark,
        point152: NormalizedLandmark
    ): Float {
        // Distanța de la punctul 9 la punctul 10
        val distanceForeheadToMid =
            euclideanDistance(point9.x(), point9.y(), point10.x(), point10.y())

        // Distanța de la punctul 10 la punctul 152 (bărbie)
        val distanceMidToChin =
            euclideanDistance(point10.x(), point10.y(), point152.x(), point152.y())

        // Lungimea totală a feței este suma acestor două distanțe
        return distanceForeheadToMid + distanceMidToChin
    }

    private fun euclideanDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    private fun calculateVectorAngle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float
    ): Float {
        val vectorA = floatArrayOf(x2 - x1, y2 - y1)
        val vectorB = floatArrayOf(x3 - x2, y3 - y2)

        // produs scalar
        val dotProduct = vectorA[0] * vectorB[0] + vectorA[1] * vectorB[1]
        // lungimea vectorului
        val magnitudeA = sqrt(vectorA[0].pow(2) + vectorA[1].pow(2))
        val magnitudeB = sqrt(vectorB[0].pow(2) + vectorB[1].pow(2))

        // din teorema cosinusului, el este egal cu produs scalar / produsul lungimilor vectorilor
        val cosTheta = (dotProduct / (magnitudeA * magnitudeB)).coerceIn(
            -1.0f,
            1.0f
        ) // handle precision errors

        // arccos de unghi = radiani
        val angleInRadians = acos(cosTheta)
        var angleInDegrees = Math.toDegrees(angleInRadians.toDouble()).toFloat()
        if (cosTheta < 0) {
            angleInDegrees = 180 - angleInDegrees
        }

        Log.d("VectorA", "Vector A: (${vectorA[0]}, ${vectorA[1]})")
        Log.d("VectorB", "Vector B: (${vectorB[0]}, ${vectorB[1]})")
        Log.d("DotProduct", "Dot Product: $dotProduct")
        Log.d("MagnitudeA", "Magnitude A: $magnitudeA")
        Log.d("MagnitudeB", "Magnitude B: $magnitudeB")
        Log.d("CosTheta", "Cos Theta: $cosTheta")
        Log.d("AngleRadians", "Angle in Radians: $angleInRadians")
        Log.d("AngleDegrees", "Angle in Degrees: $angleInDegrees")

        return angleInDegrees
    }

    private fun calculateChinAngle(landmarks: List<NormalizedLandmark>): Float {
        // Coordinates of points 152, 379, and 150
        val x152 = landmarks[152].x()
        val y152 = landmarks[152].y()
        val x379 = landmarks[379].x()
        val y379 = landmarks[379].y()
        val x150 = landmarks[150].x()
        val y150 = landmarks[150].y()

        // Log coordinates for verification
        Log.d("Coordinates", "Point 152: ($x152, $y152)")
        Log.d("Coordinates", "Point 379: ($x379, $y379)")
        Log.d("Coordinates", "Point 150: ($x150, $y150)")

        // Calculate the angle using vector math
        return calculateVectorAngle(x150, y150, x152, y152, x379, y379)
    }

    private fun calculateMandibleAngle(landmarks: List<NormalizedLandmark>): Float {
        // Coordonatele punctelor 288, 397 și 365
        val x288 = landmarks[288].x()
        val y288 = landmarks[288].y()
        val x397 = landmarks[397].x()
        val y397 = landmarks[397].y()
        val x365 = landmarks[365].x()
        val y365 = landmarks[365].y()

        // Calcularea unghiului folosind vectorii dintre aceste puncte
        return calculateVectorAngle(x288, y288, x397, y397, x365, y365)
    }

    private fun calculateCheekAngle(landmarks: List<NormalizedLandmark>): Float {
        // Coordinates of points 234, 454, and 323
        val x234 = landmarks[234].x()
        val y234 = landmarks[234].y()
        val x454 = landmarks[454].x()
        val y454 = landmarks[454].y()
        val x323 = landmarks[323].x()
        val y323 = landmarks[323].y()

        // Calculate the angle using vector math
        return calculateVectorAngle(x234, y234, x454, y454, x323, y323)
    }

    private fun predictFaceShape(result: FaceLandmarkerResult): Pair<String, FaceMeasurements> {
        val landmarks = result.faceLandmarks()[0]

        val cheekWidth = euclideanDistance(
            landmarks[234].x(), landmarks[234].y(),
            landmarks[454].x(), landmarks[454].y()
        )
        val under_nose = euclideanDistance(
            landmarks[132].x(), landmarks[132].y(),
            landmarks[361].x(), landmarks[361].y()
        )
        val under_it = euclideanDistance(
            landmarks[58].x(), landmarks[58].y(),
            landmarks[288].x(), landmarks[288].y()
        )
        val jaw_width = euclideanDistance(
            landmarks[172].x(), landmarks[172].y(),
            landmarks[397].x(), landmarks[397].y()
        )
        val chin_width = euclideanDistance(
            landmarks[150].x(), landmarks[150].y(),
            landmarks[379].x(), landmarks[379].y()
        )
        val faceLength = euclideanDistance(
            landmarks[10].x(), landmarks[10].y(),
            landmarks[152].x(), landmarks[152].y()
        )
        val foreheadWidth = euclideanDistance(
            landmarks[284].x(), landmarks[284].y(),
            landmarks[54].x(), landmarks[54].y()
        )
        val hairline = euclideanDistance(
            landmarks[67].x(), landmarks[67].y(),
            landmarks[297].x(), landmarks[297].y()
        )
        val faceWidth = euclideanDistance(
            landmarks[93].x(), landmarks[93].y(),
            landmarks[323].x(), landmarks[323].y()
        )
        val jawlineLength = euclideanDistance(
            landmarks[172].x(), landmarks[172].y(),
            landmarks[152].x(), landmarks[152].y()
        ) * 2
        val foregheadLength = euclideanDistance(
            landmarks[10].x(), landmarks[10].y(),
            landmarks[9].x(), landmarks[9].y()
        ) * 2

        val chinAngle = calculateChinAngle(landmarks)
        val cheekAngle = calculateCheekAngle(landmarks)
        val mandibleAngle = calculateMandibleAngle(landmarks)

        val totalFaceLength =
            calculateFaceLengthWithHairlineEstimate(landmarks[9], landmarks[10], landmarks[152])
        val faceLengthRatio = totalFaceLength / faceWidth
        val widthLengthRatio = faceWidth / totalFaceLength
        val cheekWidthRatio = cheekWidth / faceWidth
        val cheek_to_underNose = cheekWidth / under_nose //verde / primul negru
        val cheek_to_underIt = cheekWidth / under_it //verde / 2 negru
        val width_to_undernose = faceWidth / under_nose //rosu / 1negru
        val width_to_underit = faceWidth / under_it //rosu / 2 negru
        val underNose_toUnderIt = under_nose / under_it //under nose / under it negru/negru
        val width_to_jaw = faceWidth / jaw_width //rosu / cyan




        Log.d("Analytics", "Total Face Length including hairline estimate: $totalFaceLength")
        Log.d("Analytics", "Total Face Length 10 - 152: $faceLength")
        Log.d("Analytics", "Total Face Width: $faceWidth")
        Log.d("Analytics", "Forehead Width: $foreheadWidth")
        Log.d("Analytics", "Forehead Length: $foregheadLength")
        Log.d("Analytics", "Hairline Width: $hairline")
        Log.d("Analytics", "Cheek Width: $cheekWidth")
        Log.d("Analytics", "Under nose Width: $under_nose")
        Log.d("Analytics", "Under it Width: $under_it")
        Log.d("Analytics", "Jaw Width: $jaw_width")
        Log.d("Analytics", "Jaw length: $jawlineLength")
        Log.d("Analytics", "Chin Width: $chin_width")


        val cheekToUnderNose = (cheekWidth / under_nose * 100).roundToInt() / 100.0
        val cheekToUnderIt = (cheekWidth / under_it * 100).roundToInt() / 100.0
        val widthToUnderNose = (faceWidth / under_nose * 100).roundToInt() / 100.0
        val widthToUnderIt = (faceWidth / under_it * 100).roundToInt() / 100.0
        val underNoseToUnderIt = (under_nose / under_it * 100).roundToInt() / 100.0
        val widthToJaw = (faceWidth / jaw_width * 100).roundToInt() / 100.0
        val lengthMinusWidth = ((faceLength - faceWidth) * 100).roundToInt() / 100.0
        val foreheadMinusJawline = ((foreheadWidth - jaw_width) * 100).roundToInt() / 100.0
        val foreheadMinusChin = ((foreheadWidth - chin_width) * 100).roundToInt() / 100.0
        val chinMinusJawline = ((chin_width - jaw_width) * 100).roundToInt() / 100.0



        Log.d("Analytics", "Raport Length / Width: $faceLengthRatio")
        Log.d("Analytics", "Raport Width / Length: $widthLengthRatio")
        Log.d("Analytics", "Raport Cheek Width / Width : $cheekWidthRatio")
        Log.d("Analytics", "Raport cheek_to_underNose: $cheek_to_underNose")
        Log.d("Analytics", "Raport cheek_to_underIt: $cheek_to_underIt")
        Log.d("Analytics", "Raport width_to_undernose: $width_to_undernose")
        Log.d("Analytics", "Raport width_to_underit: $width_to_underit")
        Log.d("Analytics", "Raport width_to_jaw: $width_to_jaw")

        Log.d("Analytics", "Chin Angle: $chinAngle")
        Log.d("Analytics", "Cheek Angle: $cheekAngle")
        Log.d("Analytics", "UnderNose / UnderIt: $underNose_toUnderIt")
        Log.d("Analytics", "Mandible Angle: $mandibleAngle")


        val faceShapeScores = mutableMapOf(
            "Heart" to 0,
            "Oblong" to 0,
            "Oval" to 0,
            "Round" to 0,
            "Square" to 0
        )

        // Characteristics for Heart-shaped
        if (foreheadWidth > cheekWidth) faceShapeScores["Heart"] = faceShapeScores["Heart"]!! + 1
        if (faceLength > faceWidth) faceShapeScores["Heart"] = faceShapeScores["Heart"]!! + 1
        if (mandibleAngle in 10.0..13.0) faceShapeScores["Heart"] = faceShapeScores["Heart"]!! + 1
        if (cheekWidth > foreheadWidth && foreheadWidth > chin_width) faceShapeScores["Heart"] =
            faceShapeScores["Heart"]!! + 1

        // Characteristics for Oblong
        if (faceLengthRatio in 1.08..1.18) faceShapeScores["Oblong"] =
            faceShapeScores["Oblong"]!! + 1
        if (chinAngle in 37.0..45.5) faceShapeScores["Oblong"] = faceShapeScores["Oblong"]!! + 1
        if (foreheadWidth >= faceWidth) faceShapeScores["Oblong"] = faceShapeScores["Oblong"]!! + 1
        if (mandibleAngle in 10.6..13.0) faceShapeScores["Oblong"] = faceShapeScores["Oblong"]!! + 1

        // Characteristics for Oval
        if (faceLengthRatio in 1.1..1.5) faceShapeScores["Oval"] = faceShapeScores["Oval"]!! + 1
        if (chinAngle in 30.0..40.0) faceShapeScores["Oval"] = faceShapeScores["Oval"]!! + 1
        if (cheekWidth > jaw_width) faceShapeScores["Oval"] = faceShapeScores["Oval"]!! + 1
        if (cheekAngle < 85) faceShapeScores["Oval"] = faceShapeScores["Oval"]!! + 1
        if (faceLengthRatio > 1.5) faceShapeScores["Oval"] = faceShapeScores["Oval"]!! + 1

        // Characteristics for Round
        if (faceLengthRatio < 1.1) faceShapeScores["Round"] = faceShapeScores["Round"]!! + 1
        if (chinAngle < 35) faceShapeScores["Round"] = faceShapeScores["Round"]!! + 1
        if (cheekWidth >= faceWidth) faceShapeScores["Round"] = faceShapeScores["Round"]!! + 1

        // Characteristics for Square
        if (faceLengthRatio in 1.1..1.3) faceShapeScores["Square"] = faceShapeScores["Square"]!! + 1
        if (chinAngle < 30) faceShapeScores["Square"] = faceShapeScores["Square"]!! + 1
        if (faceWidth >= cheekWidth) faceShapeScores["Square"] = faceShapeScores["Square"]!! + 1
        if (mandibleAngle in 15.0..20.1) faceShapeScores["Square"] = faceShapeScores["Square"]!! + 1
        if (cheekAngle in 82.0..90.0) faceShapeScores["Square"] = faceShapeScores["Square"]!! + 1



        // Log the scores for each face shape
        faceShapeScores.forEach { (shape, score) ->
            Log.d("FaceShape", "$shape: $score")
        }

        // Determine the face shape with the most characteristics matched
        val maxScore = faceShapeScores.values.maxOrNull()
        var predictedShapes = faceShapeScores.filter { it.value == maxScore }.keys.toList()

        // Ensure only two shapes are returned, prioritize round over oblong and oval
        if (predictedShapes.contains("Round")) {
            predictedShapes = predictedShapes.filterNot { it == "Oblong" || it == "Oval" }.take(2)
        } else {
            predictedShapes = predictedShapes.take(2)
        }

        Log.d("Analytics", "$predictedShapes")

        val measurements = FaceMeasurements(
            faceLength = totalFaceLength,
            faceWidth = faceWidth,
            foreheadWidth = foreheadWidth,
            cheekbonesWidth = cheekWidth,
            cheekbonesAngle = cheekAngle
        )


        return Pair(predictedShapes.joinToString(" and "), measurements)

    }
    private fun saveFaceShapeToUser(measurements: FaceMeasurements, faceShape: String) {
        currentUser?.let { user ->
            val shapes = faceShape.split(" and ")
            val firstShape = shapes.getOrNull(0)
            val secondShape = shapes.getOrNull(1)

            val updatedUser = user.copy(
                faceLength = measurements.faceLength,
                faceWidth = measurements.faceWidth,
                foreheadWidth = measurements.foreheadWidth,
                cheekbonesWidth = measurements.cheekbonesWidth,
                cheekbonesAngle = measurements.cheekbonesAngle,
                firstPredictedShape = firstShape,
                secondPredictedShape = secondShape
            )

            userViewModel.updateUser(updatedUser)
            Log.d("UserUpdate", "Updated user with ID $userId: $updatedUser")
        }
    }

}


data class FaceMeasurements(
    val faceLength: Float,
    val faceWidth: Float,
    val foreheadWidth: Float,
    val cheekbonesWidth: Float,
    val cheekbonesAngle: Float
)

