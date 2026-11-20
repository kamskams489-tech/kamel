package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.authStateFlow
import com.example.data.configureAppCheck
import com.example.ui.AuthScreen
import com.example.ui.MainAppShell
import com.example.ui.theme.TasbihTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent BEFORE any UI or Firebase ops
        configureAppCheck(this, intent)

        // STEP 2: Enable dark edge-to-edge system bars matching nocturnal olive-gold theme
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        // STEP 3: Render Auth-gated Compose root
        setContent {
            TasbihTheme {
                TasbihAppRoot()
            }
        }
    }
}

@Composable
private fun TasbihAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { /* authStateFlow updates automatically */ })
    } else {
        key(user.uid) {
            MainAppShell(user = user)
        }
    }
}
