package com.mayor.kavi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.manager.SettingsManager
import com.mayor.kavi.ui.AppNavigation
import com.mayor.kavi.ui.theme.KaviTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * MainActivity class for the Kavi application, which is the main entry point of the application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Injected statistics manager for managing application statistics.
     */
    @Inject
    lateinit var statisticsManager: StatisticsManager

    /**
     * Injected settings manager for managing application settings.
     */
    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onResume() = super.onResume()
    override fun onPause() = super.onPause()

    /**
     * Called when the activity is starting. Initializes the application and sets up the UI, including
     * local providers for statistics, settings, and user repository.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            KaviTheme { KaviApp(statisticsManager, settingsManager) }
        }
    }

    @Composable
    fun KaviApp(statisticsManager: StatisticsManager, settingsManager: SettingsManager) {
        val localStatisticsManager = remember { statisticsManager }
        CompositionLocalProvider(
            StatisticsManager.LocalStatisticsManager provides localStatisticsManager,
        ) {
            val localSettingsManager = remember { settingsManager }
            CompositionLocalProvider(
                SettingsManager.LocalSettingsManager provides localSettingsManager
            ) {
                KaviTheme {
                    AppNavigation()
                }
            }
        }
    }
}
