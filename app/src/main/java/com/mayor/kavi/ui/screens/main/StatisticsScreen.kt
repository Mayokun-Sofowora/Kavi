package com.mayor.kavi.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mayor.kavi.R
import com.mayor.kavi.data.manager.*
import com.mayor.kavi.ui.components.AnalyticsDashboard
import com.mayor.kavi.ui.viewmodel.*
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(appViewModel: AppViewModel = hiltViewModel(), navController: NavController) {
    val context = LocalContext.current
    val statisticsManager = StatisticsManager.LocalStatisticsManager.current
    val scope = rememberCoroutineScope()

    val modelTrainingStatus by appViewModel.modelRetrainingStatus.collectAsState()

    // State for confirmation dialog
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.primary_container),
                    titleContentColor = colorResource(id = R.color.on_primary_container)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AnalyticsDashboard(modifier = Modifier.weight(1f))

            modelTrainingStatus?.let { status ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { /* dismiss */ }) { Text("OK") } }
                ) { Text(status) }
            }

            ResetStatsButton(onClick = { showConfirmDialog = true })
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Reset Statistics") },
            text = { Text("Are you sure you want to reset all statistics? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            statisticsManager.clearAllData()
                            showConfirmDialog = false
                            Toast.makeText(
                                context,
                                "Statistics reset successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ResetStatsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(Icons.Default.RestartAlt, null, modifier = Modifier.padding(end = 8.dp))
        Text("Reset Statistics")
    }
}

