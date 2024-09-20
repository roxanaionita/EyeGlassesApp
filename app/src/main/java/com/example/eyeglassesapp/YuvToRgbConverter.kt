package com.example.eyeglassesapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.media.Image
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class YuvToRgbConverter(context: Context) {
    private val rs = RenderScript.create(context)
    private val scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)

    fun yuvToRgb(image: Image, output: Bitmap) {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y channel
        yBuffer[nv21, 0, ySize]

        // U and V channels are interleaved
        var uvOffset = ySize
        for (i in 0 until uSize step 2) {
            nv21[uvOffset++] = vBuffer.get(i)
            nv21[uvOffset++] = uBuffer.get(i)
        }

        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(nv21.size)
        val allocationYuv = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT)
        val rgbType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(output.width).setY(output.height)
        val allocationRgb = Allocation.createTyped(rs, rgbType.create(), Allocation.USAGE_SCRIPT)

        allocationYuv.copyFrom(nv21)
        scriptYuvToRgb.setInput(allocationYuv)
        scriptYuvToRgb.forEach(allocationRgb)
        allocationRgb.copyTo(output)
    }

    companion object {
        //@OptIn(markerClass = androidx.camera.core.ExperimentalGetImage::class)
        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
        fun imageToBitmap(context: Context, imageProxy: ImageProxy): Bitmap {
            val image = imageProxy.image ?: throw IllegalStateException("Image is null")
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            YuvToRgbConverter(context).yuvToRgb(image, bitmap)
            return bitmap
            }
        }
}
