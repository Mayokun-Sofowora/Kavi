package com.mayor.kavi.ui.screens.modes

import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayor.kavi.R
import com.mayor.kavi.data.models.detection.Detection
import com.mayor.kavi.ui.components.*
import com.mayor.kavi.ui.viewmodel.DetectionViewModel
import com.mayor.kavi.ui.viewmodel.DetectionState
import com.mayor.kavi.ui.viewmodel.GameViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualModeScreen(
    onNavigateBack: () -> Unit,
    gameViewModel: GameViewModel = hiltViewModel(),
    detectionViewModel: DetectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detectionState by detectionViewModel.detectionState.collectAsState()
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var previewSize by remember { mutableStateOf(IntSize(0, 0)) }

    // Camera controller
    val cameraController = remember { LifecycleCameraController(context) }

    // Request camera permissions
    RequestPermissions()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button
        CenterAlignedTopAppBar(
            title = { Text("Virtual Mode") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.primary_container),
                titleContentColor = colorResource(id = R.color.on_primary_container)
            )
        )

        Box(modifier = Modifier.weight(1f)) {
            if (capturedImage == null) {
                // Show Camera Preview when no image is captured
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        controller = cameraController,
                        modifier = Modifier.fillMaxSize(),
                        onPreviewSizeChanged = { size ->
                            previewSize = size
                        }
                    )

                    // Floating capture button
                    FloatingActionButton(
                        onClick = {
                            cameraController.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = image.toBitmap()
                                        capturedImage = bitmap
                                        detectionViewModel.detectDice(bitmap)
                                        image.close()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Timber.e(exception, "Image capture failed")
                                        Toast.makeText(
                                            context,
                                            "Failed to capture image: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        },
        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = "Capture")
                    }
                }
            } else {
                // Show captured image with detections
                Box(modifier = Modifier.fillMaxSize()) {
                    // Display captured image
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Detection Overlays
                    when (val state = detectionState) {
                        is DetectionState.Success -> {
                            Timber.d("Drawing ${state.detections.size} detections")
                            state.detections.forEach { detection ->
                                Timber.d("Drawing detection box: label=${detection.label}, boundingBox=${detection.boundingBox}")
                                DrawDetectionBox(
                                    detection = detection,
                                    imageSize = Size(640f, 640f), // Target size from preprocessing
                                    canvasSize = Size(
                                        previewSize.width.toFloat(),
                                        previewSize.height.toFloat()
                                    )
                                )
                            }
                        }

                        is DetectionState.Error -> {
                            Timber.e("Detection error: ${state.message}")
                            Toast.makeText(
                                context,
                                "Detection error: ${state.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        DetectionState.Processing -> {
                            Timber.d("Processing image detection...")
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            Timber.d("Detection state: $state")
                        }
                    }
                }
            }
        }

        // Bottom Controls
        if (capturedImage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        capturedImage?.recycle()
                        capturedImage = null
                        detectionViewModel.clearDetections()
                    }
                ) {
                    Text("Retake")
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    detectionState: DetectionState,
    onSaveDetections: (List<Detection>) -> Unit,
    onClearDetections: () -> Unit
) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
            onClick = onClearDetections,
            enabled = detectionState is DetectionState.Success || detectionState is DetectionState.NoDetections || detectionState is DetectionState.Error
                    ) {
                        Text("Retake")
                    }

                    Button(
                        onClick = {
                if (detectionState is DetectionState.Success) {
                    onSaveDetections(detectionState.detections)
                }
            },
            enabled = detectionState is DetectionState.Success
        ) {
            Text("Save")
        }
    }
}
