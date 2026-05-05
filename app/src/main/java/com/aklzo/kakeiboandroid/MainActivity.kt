package com.aklzo.kakeiboandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.auth.SignInScreen
import com.aklzo.kakeiboandroid.ui.theme.KakeiboAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KakeiboAndroidTheme {
                val idToken by AuthManager.idToken.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (idToken == null) {
                        SignInScreen()
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("サインイン済み")
                        }
                    }
                }
            }
        }
    }
}
