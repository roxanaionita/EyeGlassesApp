package com.example.eyeglassesapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class LiveOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var results: FaceLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var scaleFactorX: Float = 1f
    private var scaleFactorY: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var rotationDegrees: Int = 0

    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        //linePaint.color = ContextCompat.getColor(context!!, android.R.color.black)

        linePaint.color =
            ContextCompat.getColor(context!!, com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
        linePaint.strokeWidth = 8f
        linePaint.style = Paint.Style.STROKE

        //pointPaint.color = ContextCompat.getColor(context!!, android.R.color.white)
        pointPaint.color = ContextCompat.getColor(context!!, com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
        pointPaint.strokeWidth = 8f
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (results == null || results!!.faceLandmarks().isEmpty()) {
            clear()
            return
        }

        results?.let { faceLandmarkerResult ->
            canvas.save()
            canvas.rotate(-rotationDegrees.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())

            for (landmark in faceLandmarkerResult.faceLandmarks()) {
                for (normalizedLandmark in landmark) {
                    val x = normalizedLandmark.x() * imageWidth * scaleFactorX
                    val y = normalizedLandmark.y() * imageHeight * scaleFactorY
                    canvas.drawPoint(x, y, pointPaint)
                }
            }

            FaceLandmarker.FACE_LANDMARKS_CONNECTORS.forEach {
                canvas.drawLine(
                    faceLandmarkerResult.faceLandmarks()[0][it!!.start()].x() * imageWidth * scaleFactorX,
                    faceLandmarkerResult.faceLandmarks()[0][it.start()].y() * imageHeight * scaleFactorY,
                    faceLandmarkerResult.faceLandmarks()[0][it.end()].x() * imageWidth * scaleFactorX,
                    faceLandmarkerResult.faceLandmarks()[0][it.end()].y() * imageHeight * scaleFactorY,
                    linePaint
                )
            }

            drawGlassesOverlay(canvas, faceLandmarkerResult)
            canvas.restore()
        }
    }

    fun setResults(
        faceLandmarkerResults: FaceLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        rotationDegrees: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = faceLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactorX = width.toFloat() / imageWidth.toFloat()
        scaleFactorY = height.toFloat() / imageHeight.toFloat()

        invalidate()
    }

    private var glassesBitmap: Bitmap? = null

    fun setGlassesBitmap(bitmap: Bitmap) {
        this.glassesBitmap = bitmap
    }

    private fun drawGlassesOverlay(canvas: Canvas, result: FaceLandmarkerResult) {
        val leftEye = result.faceLandmarks()[0][133]
        val rightEye = result.faceLandmarks()[0][362]

        val xLeft = leftEye.x() * imageWidth * scaleFactorX
        val yLeft = leftEye.y() * imageHeight * scaleFactorY
        val xRight = rightEye.x() * imageWidth * scaleFactorX
        val yRight = rightEye.y() * imageHeight * scaleFactorY

        val distance = Math.sqrt(Math.pow((xRight - xLeft).toDouble(), 2.0) + Math.pow((yRight - yLeft).toDouble(), 2.0))

        val glassesWidth = distance * 3.0
        val glassesHeight = glassesBitmap?.height?.times(glassesWidth / glassesBitmap!!.width)

        val leftAdjustment = 30 // Move glasses 30 pixels to the right
        val topAdjustment = 50 // Move glasses 30 pixels down

        val left = xLeft - glassesWidth / 2 + leftAdjustment
        val top = yLeft - glassesHeight!! / 1.5 + topAdjustment

        glassesBitmap?.let {
            canvas.drawBitmap(it, null, RectF(left.toFloat(), top.toFloat(), (left + glassesWidth).toFloat(), (top + glassesHeight).toFloat()), null)
        }
    }

    private fun drawEye(canvas: Canvas, x: Float, y: Float) {
        val eyePaint = Paint().apply {
            color = Color.BLUE
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        val eyeRadius = 10f

        canvas.drawCircle(x, y, eyeRadius, eyePaint)
        }
}
