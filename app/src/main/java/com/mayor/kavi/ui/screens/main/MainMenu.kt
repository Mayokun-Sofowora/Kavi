package com.mayor.kavi.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mayor.kavi.R
import com.mayor.kavi.ui.theme.*
import com.mayor.kavi.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayor.kavi.ui.viewmodel.AppViewModel

/**
 * Main menu screen of the application.
 *
 * This screen serves as the primary navigation hub, featuring:
 * - Game mode selection
 * - Access to statistics and settings
 * - Visual branding elements
 * - Responsive layout with background image
 *
 * The screen prevents back navigation to maintain proper app flow.
 */
@Composable
fun MainMenuScreen(navController: NavController, appViewModel: AppViewModel = hiltViewModel()) {

    BackHandler(enabled = true) { /* Prevent navigation to sign-in page*/ }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer
        BackgroundImage()

        // Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoSection()
            Spacer(modifier = Modifier.height(32.dp))
            MenuButtonsSection(navController, appViewModel)
        }
    }
}

/**
 * Background image component for the main menu.
 *
 * Displays a full-screen background with:
 * - Proper scaling and positioning
 * - Optional overlay or tint
 * - Responsive sizing
 */
@Composable
private fun BackgroundImage() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))  // Slightly less dark overlay
        )
    }
}

/**
 * Logo section of the main menu.
 *
 * Displays the app's branding elements:
 * - App logo/icon
 * - Title text
 * - Optional subtitle or version information
 */
@Composable
private fun LogoSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.03f))
            .padding(vertical = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun MenuButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    val buttonColor = Color(0xFFAF423C)  // Custom red color
    val disabledColor = Color.Gray
    Button(
        onClick = onClick.takeIf { enabled } ?: {},
        modifier = Modifier
            .width(320.dp)
            .heightIn(min = 56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) buttonColor else disabledColor,
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (enabled) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = AdlamFont,
                color = Color.White,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Main menu navigation buttons section.
 *
 * Displays a column of buttons for:
 * - Game mode selection (Pig, Greed, Balut)
 * - Statistics view
 * - Settings access
 * - Custom game creation
 *
 * @param navController Navigation controller for screen transitions
 * @param appViewModel AppViewModel for user state
 */
@Composable
private fun MenuButtonsSection(
    navController: NavController,
    appViewModel: AppViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MenuButton(
                label = "Start",
                onClick = { navController.navigateToStart() }
            )
            MenuButton(
                label = "Settings",
                onClick = { navController.navigateToSettings() }
            )
            MenuButton(
                label = "Instructions",
                onClick = { navController.navigateToInstructions() }
            )
            MenuButton(
                label = "Statistics",
                onClick = { navController.navigateToStatistics() }
            )
        }
    }
}
