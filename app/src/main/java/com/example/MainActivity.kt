package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UiState
import com.example.ui.KalleshMainShell
import com.example.ui.auth.AuthLandingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KalleshHubViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KalleshAiHubRoot(application = application)
        }
    }
}

@Composable
fun KalleshAiHubRoot(application: android.app.Application) {
    val auth = remember { Firebase.auth }
    var currentUserId by remember { mutableStateOf(auth.currentUser?.uid) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUid = firebaseAuth.currentUser?.uid
            if (firebaseUid != null) {
                currentUserId = firebaseUid
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val activeUid = currentUserId
    if (activeUid.isNullOrBlank()) {
        MyApplicationTheme(darkTheme = true) {
            AuthLandingScreen(
                onAuthSuccess = {
                    currentUserId = Firebase.auth.currentUser?.uid ?: "kallesh_founder_preview"
                },
                onInstantPreviewAccess = {
                    currentUserId = Firebase.auth.currentUser?.uid ?: "kallesh_founder_preview"
                }
            )
        }
    } else {
        val hubViewModel: KalleshHubViewModel = viewModel(
            key = "kallesh_hub_$activeUid",
            factory = KalleshHubViewModel.Factory(application, activeUid)
        )
        val profileState by hubViewModel.userProfileState.collectAsStateWithLifecycle()
        val themePref = (profileState as? UiState.Success)?.data?.themeMode ?: "DARK"
        val useDark = when (themePref.uppercase()) {
            "LIGHT" -> false
            "SYSTEM" -> isSystemInDarkTheme()
            else -> true
        }

        MyApplicationTheme(darkTheme = useDark) {
            KalleshMainShell(
                viewModel = hubViewModel,
                onSignedOut = {
                    currentUserId = null
                }
            )
        }
    }
}
