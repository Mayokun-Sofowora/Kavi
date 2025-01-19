package com.mayor.kavi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * KaviApplication class for initializing global dependencies.
 * - Initializes Tensorflow Lite for ai models.
 * - Sets up Timber for logging in debug builds.
 */
@HiltAndroidApp
class KaviApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        // Initialize TensorFlow Lite
        // try {
        //     System.loadLibrary("tensorflowlite_gpu_jni")
        //     Timber.d("TensorFlow Lite GPU support initialized")
        // } catch (e: UnsatisfiedLinkError) {
        //     Timber.w("TensorFlow Lite GPU support not available: ${e.message}")
        // }
    }
}

