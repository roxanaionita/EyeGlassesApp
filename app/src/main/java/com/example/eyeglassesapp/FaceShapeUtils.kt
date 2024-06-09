package com.example.eyeglassesapp

import com.example.eyeglassesapp.dao.FaceShapeDao
import com.example.eyeglassesapp.entities.FaceShapeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FaceShapeUtils {
    fun populateFaceShapes(faceShapeDao: FaceShapeDao) {
        CoroutineScope(Dispatchers.IO).launch {
            val existingFaceShapes = faceShapeDao.getAllFaceShapes()
            val faceShapes = listOf(
                FaceShapeEntity(faceShape = "Round", description = "A round face has softer, equal-length features with a rounded jawline and " +
                        "hairline. The width and length of the face are almost equal, creating a circular appearance. " +
                        "This face shape benefits from angular frames that add definition and contrast to the soft, curved lines, " +
                        "enhancing the overall facial structure."),
                FaceShapeEntity(faceShape = "Oval", description = "An oval face is characterized by balanced proportions, with a slightly wider forehead and gently curved chin. " +
                        "The length of the face is about one and a half times its width, giving it an elongated look. " +
                        "This versatile shape suits almost any frame style, highlighting its natural symmetry and balanced features."),
                FaceShapeEntity(faceShape = "Square", description = "A square face features a strong jawline, broad forehead, and angular chin, with equal width and length. " +
                        "This face shape often has prominent cheekbones and a more defined structure. " +
                        "Curved or rounded frames are ideal, as they soften the sharp angles and add a harmonious balance to the face's robust characteristics."),
                FaceShapeEntity(faceShape = "Oblong", description = "An oblong face is longer than it is wide, with a long straight cheek line and sometimes a longer nose. This face shape often features a high forehead and a square or rounded jawline. " +
                        "Frames with depth, such as tall or oversized styles, help to shorten the face and create a more balanced look."),
                FaceShapeEntity(faceShape = "Heart", description = "A heart-shaped face has a wider forehead and cheekbones that taper down to a narrow chin. " +
                        "This shape often features a widow's peak hairline and high cheekbones. Frames that are wider at the top than the bottom, like aviators or cat-eye styles, complement the " +
                        "natural contours by balancing the broader forehead and narrower chin.")
            )
            // Filter out the face shapes that are already in the database
            val newFaceShapes = faceShapes.filter { newShape ->
                existingFaceShapes.none { it.faceShape == newShape.faceShape }
            }

            // Insert the new face shapes
            if (newFaceShapes.isNotEmpty()) {
                faceShapeDao.insertAll(newFaceShapes)
            }
        }
    }
}