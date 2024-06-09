package com.example.eyeglassesapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker.FACE_LANDMARKS_FACE_OVAL
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: FaceLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var textPaint = Paint()
    private var boxPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        textPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.black)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.RED
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.RED
        textPaint.textSize = 20f
        textPaint.style = Paint.Style.FILL

        boxPaint.color = Color.GREEN
        boxPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        boxPaint.style = Paint.Style.STROKE
    }

    // VARIANTA INITIALA START
//    override fun draw(canvas: Canvas) {
//        super.draw(canvas)
//        if(results == null || results!!.faceLandmarks().isEmpty()) {
//            clear()
//            return
//        }
//
//        results?.let { faceLandmarkerResult ->
//
////            for(landmark in faceLandmarkerResult.faceLandmarks()) {
////                for(normalizedLandmark in landmark) {
////                    canvas.drawPoint(normalizedLandmark.x() * imageWidth * scaleFactor, normalizedLandmark.y() * imageHeight * scaleFactor, pointPaint)
////                }
////            }
//
//
//            // FACE LANDMARK CONNECTORS = CONTUR PENTRU FATA, SPRANCENE,OCHI SI BUZE
//            FaceLandmarker.FACE_LANDMARKS_CONNECTORS.forEach {
//                canvas.drawLine(
//                    faceLandmarkerResult.faceLandmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
//                    faceLandmarkerResult.faceLandmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
//                    faceLandmarkerResult.faceLandmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
//                    faceLandmarkerResult.faceLandmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
//                    linePaint)
//            }
//        }
//    }
    // VARIANTA INITIALA STOP



//    override fun draw(canvas: Canvas) {
//        super.draw(canvas)
//        if (results == null || results!!.faceLandmarks().isEmpty()) {
//            clear()
//            return
//        }
//
//        results?.let { faceLandmarkerResult ->
//            // Draw points and numbers for each landmark in the face oval
//            FACE_LANDMARKS_FACE_OVAL.forEachIndexed { index, connection ->
//                val x = faceLandmarkerResult.faceLandmarks()[0][connection.start()].x() * imageWidth * scaleFactor
//                val y = faceLandmarkerResult.faceLandmarks()[0][connection.start()].y() * imageHeight * scaleFactor
//
//                // Draw the point
//                canvas.drawPoint(x, y, pointPaint)
//
//                // Draw the number
//                canvas.drawText(index.toString(), x, y, textPaint)
//            }
//        }
//    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (results == null || results!!.faceLandmarks().isEmpty()) {
            clear()
            return
        }

        results?.let { faceLandmarkerResult ->
            // Draw points and numbers for each landmark in the face oval
            FACE_LANDMARKS_FACE_OVAL.forEachIndexed { index, connection ->
                val x = faceLandmarkerResult.faceLandmarks()[0][connection.start()].x() * imageWidth * scaleFactor
                val y = faceLandmarkerResult.faceLandmarks()[0][connection.start()].y() * imageHeight * scaleFactor

                // Draw the point
                canvas.drawPoint(x, y, pointPaint)

                // Draw the number
                //canvas.drawText(index.toString(), x, y, textPaint)
            }

            // Draw a line between points with specified indices
            //LENGTH
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 10, 152,  Color.BLUE)

            // WIDTH
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 93, 323,  Color.RED)

            // CHEEKBONES WIDTH
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 234, 454, Color.GREEN)

            // FOREHEAD WIDTH
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 54, 284, Color.YELLOW)

            // UNGHI INTRE CHEEKBONES SI JAWLINE
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 454, 323, Color.MAGENTA)


            // JAWLINE
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 172, 397, Color.CYAN)
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 172, 152, Color.CYAN)
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 397, 152, Color.CYAN)
            // CHIN
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 150, 379, Color.DKGRAY)

            //arc mandibula cu obraji
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 361, 288, Color.LTGRAY)
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 288, 397, Color.LTGRAY)
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 397, 365, Color.LTGRAY)

            // deasupra buzelor
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 361, 132, Color.BLACK)
            // intersecteaza buzele
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 288, 58, Color.BLACK)
            //hairline
            drawLineBetweenPoints(canvas, faceLandmarkerResult, 297, 67, Color.BLACK)


        }
    }



    private fun drawLineBetweenPoints(
        canvas: Canvas,
        faceLandmarkerResult: FaceLandmarkerResult,
        index1: Int,
        index2: Int,
        color: Int
    ) {
        val point1 = faceLandmarkerResult.faceLandmarks()[0][index1]
        val point2 = faceLandmarkerResult.faceLandmarks()[0][index2]

        val x1 = point1.x() * imageWidth * scaleFactor
        val y1 = point1.y() * imageHeight * scaleFactor
        val x2 = point2.x() * imageWidth * scaleFactor
        val y2 = point2.y() * imageHeight * scaleFactor

        // Set the paint color
        linePaint.color = color

        canvas.drawLine(x1, y1, x2, y2, linePaint)
        //draw text representing the number of the landmark
        canvas.drawText(index1.toString(), x1, y1,textPaint)
        canvas.drawText(index2.toString(), x2, y2,textPaint)
    }



    fun setResults(
        faceLandmarkerResults: FaceLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = faceLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
        private const val TAG = "Face Landmarker Overlay"
    }
}


