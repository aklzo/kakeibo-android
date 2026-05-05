package com.aklzo.kakeiboandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.auth.SignInScreen
import com.aklzo.kakeiboandroid.progress.ProgressScreen
import com.aklzo.kakeiboandroid.summary.SummaryScreen
import com.aklzo.kakeiboandroid.transactions.AddTransactionScreen
import com.aklzo.kakeiboandroid.transactions.TransactionListScreen
import com.aklzo.kakeiboandroid.ui.theme.KakeiboAndroidTheme

private enum class Screen(val label: String) {
    Add("追加"),
    List("一覧"),
    Summary("集計"),
    Budget("予算")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KakeiboAndroidTheme {
                val idToken by AuthManager.idToken.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                var currentScreen by remember { mutableStateOf(Screen.Add) }

                if (idToken == null) {
                    SignInScreen()
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Add,
                                    onClick = { currentScreen = Screen.Add },
                                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                    label = { Text(Screen.Add.label) }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.List,
                                    onClick = { currentScreen = Screen.List },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    label = { Text(Screen.List.label) }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Summary,
                                    onClick = { currentScreen = Screen.Summary },
                                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                    label = { Text(Screen.Summary.label) }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Budget,
                                    onClick = { currentScreen = Screen.Budget },
                                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                    label = { Text(Screen.Budget.label) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        when (currentScreen) {
                            Screen.Add -> AddTransactionScreen(innerPadding, snackbarHostState)
                            Screen.List -> TransactionListScreen(innerPadding)
                            Screen.Summary -> SummaryScreen(innerPadding)
                            Screen.Budget -> ProgressScreen(innerPadding)
                        }
                    }
                }
            }
        }
    }
}
