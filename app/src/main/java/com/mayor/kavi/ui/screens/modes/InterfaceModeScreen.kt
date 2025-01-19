package com.mayor.kavi.ui.screens.modes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.mayor.kavi.R
import com.mayor.kavi.ui.theme.AdlamFont
import com.mayor.kavi.ui.viewmodel.AppViewModel
import com.mayor.kavi.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceModeScreen(navController: NavController, viewModel: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer
        BackgroundLayer()

        // Content Layer
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(navController)
            MainContent(navController, viewModel)
        }
    }
}

@Composable
private fun BackgroundLayer() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(100.dp)
                    .padding(vertical = 2.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(
                onClick = { navController.navigateToInstructions(1) } // Using extension function
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun MainContent(navController: NavController, viewModel: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        HeaderSection()

        Spacer(modifier = Modifier.height(48.dp))

        GameModeSelection(navController, viewModel)

        Spacer(modifier = Modifier.height(32.dp))

        DescriptionText()
    }
}

@Composable
private fun HeaderSection() {
    Text(
        text = "INTERFACE MODE",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = AdlamFont,
            color = Color.White,
            fontWeight = FontWeight.Bold
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun GameModeSelection(navController: NavController, viewModel: AppViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModeSelectionButton(
            label = "VIRTUAL",
            imageRes = R.drawable.virtual,
            onClick = {
                viewModel.setInterfaceMode("virtual")
                navController.navigateToVirtual()
            }
        )

        ModeSelectionButton(
            label = "CLASSIC",
            imageRes = R.drawable.classic,
            onClick = {
                viewModel.setInterfaceMode("classic")
                navController.navigateToBoards()
            }
        )
    }
}

@Composable
private fun ModeSelectionButton(
    label: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "$label Mode",
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = AdlamFont,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun DescriptionText() {
    Text(
        text = "Choose between Virtual mode to detect dice images or classic mode to play the traditional way.",
        style = MaterialTheme.typography.bodyLarge.copy(
            color = Color.White.copy(alpha = 0.9f)
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}