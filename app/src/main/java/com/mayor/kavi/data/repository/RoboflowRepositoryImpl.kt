package com.mayor.kavi.data.repository

import android.graphics.Bitmap
import android.graphics.RectF
import com.mayor.kavi.data.models.detection.Detection
import com.mayor.kavi.data.service.RoboflowService
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody

/**
 * Implementation of [RoboflowRepository] that handles dice detection using the Roboflow API.
 * This class processes images, makes API calls, and converts the responses into application-specific
 * detection objects.
 *
 * @property roboflowService The service interface for making API calls to Roboflow
 */
class RoboflowRepositoryImpl @Inject constructor(
    private val roboflowService: RoboflowService
) : RoboflowRepository {

    companion object {
        /** API key for authenticating with the Roboflow service */
        private const val API_KEY = "zE2nvgkdH4HiZvTv1S84"

        /** Minimum confidence threshold for accepting a detection (70%) */
        private const val CONFIDENCE_THRESHOLD = 0.4f

        /** Target size for image preprocessing (required by the Roboflow model) */
        private const val TARGET_SIZE = 640
    }

    /**
     * Detects dice in the provided bitmap image using the Roboflow API.
     * The process involves:
     * 1. Preprocessing the image to meet model requirements
     * 2. Converting the image to a format suitable for API transmission
     * 3. Making the API call to detect dice
     * 4. Processing and converting the response into [Detection] objects
     *
     * @param bitmap The input image to process
     * @return A list of [Detection] objects representing detected dice, or an empty list if no dice are detected
     */
    override suspend fun detectDice(bitmap: Bitmap): List<Detection> {
        return withContext(Dispatchers.IO) {
            try {
                // Preprocess image to required dimensions
                val processedBitmap = preprocessImage(bitmap)

                // Convert bitmap to request body for API transmission
                val byteArrayOutputStream = ByteArrayOutputStream()
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val requestFile = byteArrayOutputStream
                    .toByteArray()
                    .toRequestBody("image/jpeg".toMediaTypeOrNull())

                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    "image.jpg",
                    requestFile
                )

                // Make API call to detect dice
                val response = roboflowService.detectDice(
                    apiKey = API_KEY,
                    file = filePart
                )

                if (!response.isSuccessful) {
                    Timber.e("API call failed: ${response.errorBody()?.string()}")
                    return@withContext emptyList()
                }

                // Log raw API response
                Timber.d("Raw API Response: ${response.body()}")

                // Convert API predictions to Detection objects, filtering out low-confidence detections
                response.body()?.predictions?.mapNotNull { prediction ->
                    // Log each prediction before confidence filtering
                    Timber.d("Raw prediction: class=${prediction.`class`}, confidence=${prediction.confidence}, x=${prediction.x}, y=${prediction.y}, width=${prediction.width}, height=${prediction.height}")

                    if (prediction.confidence < CONFIDENCE_THRESHOLD) {
                        Timber.d("Prediction filtered out due to low confidence: ${prediction.confidence}")
                        return@mapNotNull null
                    }

                    Detection(
                        label = prediction.`class`,
                        confidence = prediction.confidence.toFloat(),
                        boundingBox = RectF(
                            prediction.x - (prediction.width / 2f),
                            prediction.y - (prediction.height / 2f),
                            prediction.x + (prediction.width / 2f),
                            prediction.y + (prediction.height / 2f)
                        )
                    ).also { detection ->
                        // Log each processed detection
                        Timber.d("Processed detection: label=${detection.label}, confidence=${detection.confidence}, boundingBox=${detection.boundingBox}")
                    }
                } ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error detecting dice")
                emptyList()
            }
        }
    }

    /**
     * Preprocesses the input image to meet the model's requirements.
     * Scales the image to the target size (640x640) while maintaining aspect ratio.
     * This operation is performed on the Default dispatcher for CPU-intensive work.
     *
     * @param bitmap The original input image
     * @return A scaled bitmap of size TARGET_SIZE x TARGET_SIZE
     */
    private suspend fun preprocessImage(bitmap: Bitmap): Bitmap {
        return withContext(Dispatchers.Default) {
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                TARGET_SIZE,
                TARGET_SIZE,
                true
            )
            scaledBitmap
        }
    }
}
