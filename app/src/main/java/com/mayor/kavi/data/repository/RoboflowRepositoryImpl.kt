package com.mayor.kavi.data.repository

import android.graphics.Bitmap
import android.graphics.Color
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
import kotlin.math.max

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
        private const val API_KEY = BuildConfig.ROBOFLOW_API_KEY

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
            try {
                // Step 1: Convert to RGB if needed
                val rgbBitmap = ensureRGBFormat(bitmap)

                // Step 2: Enhance contrast and normalize lighting
                val enhancedBitmap = enhanceContrast(rgbBitmap)

                // Step 3: Scale while maintaining aspect ratio
                val scaledBitmap = scaleWithAspectRatio(enhancedBitmap, TARGET_SIZE)

                // Step 4: Apply noise reduction
                val finalBitmap = reduceNoise(scaledBitmap)

                Timber.d("Preprocessing completed successfully")
                finalBitmap
            } catch (e: Exception) {
                Timber.e(e, "Error during image preprocessing")
                // Fallback to basic scaling if enhancement fails
                Bitmap.createScaledBitmap(bitmap, TARGET_SIZE, TARGET_SIZE, true)
            }
        }
    }

    private fun ensureRGBFormat(bitmap: Bitmap): Bitmap {
        return if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else bitmap
    }

    private fun enhanceContrast(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Calculate histogram
        val histogram = IntArray(256)
        for (pixel in pixels) {
            val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            histogram[gray]++
        }

        // Find min and max gray values
        var min = 0
        var max = 255
        for (i in histogram.indices) {
            if (histogram[i] > 0) {
                min = i
                break
            }
        }
        for (i in histogram.indices.reversed()) {
            if (histogram[i] > 0) {
                max = i
                break
            }
        }

        // Apply contrast stretching
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Stretch each channel
                val newR = ((r - min) * 255 / (max - min)).coerceIn(0, 255)
                val newG = ((g - min) * 255 / (max - min)).coerceIn(0, 255)
                val newB = ((b - min) * 255 / (max - min)).coerceIn(0, 255)

                output.setPixel(x, y, Color.rgb(newR, newG, newB))
            }
        }
        return output
    }

    private fun scaleWithAspectRatio(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = targetSize.toFloat() / max(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun reduceNoise(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Apply 3x3 median filter
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val neighbors = mutableListOf<Int>()
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        neighbors.add(bitmap.getPixel(x + dx, y + dy))
                    }
                }
                // Sort by luminance
                neighbors.sortBy { Color.red(it) + Color.green(it) + Color.blue(it) }
                // Use median value
                output.setPixel(x, y, neighbors[4])
            }
        }
        return output
    }
}
