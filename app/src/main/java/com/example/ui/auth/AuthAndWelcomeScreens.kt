package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.SystemConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOutUser(
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun GoogleSignInButton(
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isLoading = true
            onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    isLoading = false
                    onAuthSuccess()
                },
                onAuthError = { errorMsg ->
                    isLoading = false
                    onAuthError(errorMsg)
                },
                scope = coroutineScope,
                onAuthCancelled = { isLoading = false }
            )
        },
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("google_sign_in_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Connecting Google Account...", fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Sign in with Google", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private data class LandingFeatureItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthLandingScreen(
    onAuthSuccess: () -> Unit,
    onInstantPreviewAccess: () -> Unit = onAuthSuccess
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showLogoShowcase by remember { mutableStateOf(false) }
    var isEnteringPreview by remember { mutableStateOf(false) }

    if (showLogoShowcase) {
        KalleshBrandLogoShowcaseDialog(onDismiss = { showLogoShowcase = false })
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    val features = remember {
        listOf(
            LandingFeatureItem(Icons.AutoMirrored.Filled.Chat, "Multi-Model AI Chat", "Gemini 3.5 Flash, 3.1 Pro & Lite with streaming"),
            LandingFeatureItem(Icons.Default.Image, "AI Image Studio", "Create & edit visuals with Gemini 3.1 Flash Image"),
            LandingFeatureItem(Icons.Default.Movie, "Veo 3 Video Studio", "Text-to-video & animate photos in 16:9 or 9:16"),
            LandingFeatureItem(Icons.Default.MusicNote, "Lyria 3 Music Lab", "Compose studio clips & full-length AI tracks"),
            LandingFeatureItem(Icons.Default.GraphicEq, "Live Voice & TTS", "Gemini 3.8 Live voice chat & audio transcription"),
            LandingFeatureItem(Icons.Default.Public, "Search & Maps Grounding", "Live Google Search & Maps verified citations"),
            LandingFeatureItem(Icons.Default.Description, "Document & Study AI", "Analyze PDFs, notes, quizzes & flashcards"),
            LandingFeatureItem(Icons.Default.Code, "Kallesh Code AI", "Generate, debug & convert across 13 languages")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A12),
                        Color(0xFF0D1528),
                        Color(0xFF070A12)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Brand Emblem
            val pulseTransition = rememberInfiniteTransition(label = "logo_pulse")
            val pulseScale by pulseTransition.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF00E5FF), Color(0xFF6366F1), Color(0xFFA855F7))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_kallesh_ref_logo),
                    contentDescription = "Kallesh AI Hub Reference Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { showLogoShowcase = true },
                modifier = Modifier.testTag("open_logo_showcase_button"),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View Official Kallesh AI Hub Logo Kit",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                shape = CircleShape
            ) {
                Text(
                    text = "FOUNDED BY KALLESH MC • ALL-IN-ONE AI PLATFORM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Kallesh AI Hub",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "One AI Hub. Limitless Possibilities.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your AI Hub for Chat, Images, Video, Voice, Music, Files, Search, Maps and Creativity — backed by Firebase Cloud & Gemini Intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF11192C).copy(alpha = 0.92f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your AI, All in One Hub",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sign in with your Google Account to unlock persistent chat memory, daily free tier credits, and your private AI workspace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    GoogleSignInButton(
                        onAuthSuccess = onAuthSuccess,
                        onAuthError = { authError = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            isEnteringPreview = true
                            scope.launch {
                                try {
                                    Firebase.auth.signInAnonymously().await()
                                    isEnteringPreview = false
                                    onAuthSuccess()
                                } catch (e: Exception) {
                                    isEnteringPreview = false
                                    onInstantPreviewAccess()
                                }
                            }
                        },
                        enabled = !isEnteringPreview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("instant_preview_access_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnteringPreview) "Opening Kallesh AI Hub..." else "Explore & Test App Now (Instant Access)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = authError != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${authError.orEmpty()}\nTip: On the cloud emulator without a signed-in Google OS account, tap 'Explore & Test App Now' above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "INTEGRATED AI STUDIOS & CAPABILITIES",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                features.forEach { feature ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0E1422)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = feature.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = feature.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FounderWelcomeDialog(
    config: SystemConfig,
    isPlayingAudio: Boolean,
    isMuted: Boolean,
    onPlayPause: () -> Unit,
    onReplay: () -> Unit,
    onToggleMute: () -> Unit,
    onStartExploring: () -> Unit
) {
    val paragraphs = remember(config.description) {
        config.description
            .split("\n\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    var highlightedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(paragraphs.size, isPlayingAudio) {
        if (isPlayingAudio && paragraphs.isNotEmpty()) {
            for (idx in highlightedIndex until paragraphs.size) {
                highlightedIndex = idx
                delay(3200L)
            }
        }
    }

    Dialog(
        onDismissRequest = onStartExploring,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0B1120),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Founder Introduction", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    TextButton(
                        onClick = onStartExploring,
                        modifier = Modifier.testTag("skip_intro_button")
                    ) {
                        Text("Skip Intro")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_kallesh_k_symbol),
                        contentDescription = "Kallesh AI Hub Emblem",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = config.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "By Kallesh MC • Founder, Kallesh AI Hub",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Synchronized Subtitle Viewer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141D33)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        paragraphs.forEachIndexed { index, paragraph ->
                            val isCurrent = index == highlightedIndex
                            Text(
                                text = paragraph,
                                style = if (isCurrent) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onPlayPause,
                        modifier = Modifier.testTag("intro_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingAudio) "Pause" else "Play"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPlayingAudio) "Pause Voice" else "Play Voice")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedButton(
                        onClick = {
                            highlightedIndex = 0
                            onReplay()
                        },
                        modifier = Modifier.testTag("intro_replay_button")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Replay")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replay")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.testTag("intro_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onStartExploring,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_exploring_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Start Exploring Kallesh AI Hub", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun KalleshBrandLogoShowcaseDialog(
    onDismiss: () -> Unit
) {
    var selectedVariant by remember { mutableIntStateOf(0) }
    var transparentPreviewBg by remember { mutableIntStateOf(0) } // 0 = Dark Navy, 1 = Transparent Grid, 2 = Light Slate

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF080C17),
            tonalElevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KALLESH AI HUB",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Official Futuristic Brand Identity & Logo Suite",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00E5FF)
                        )
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_logo_showcase_button")
                    ) {
                        Text("Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Variant Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("1. K AI Symbol", "2. Full Lockup", "3. Transparent Vector")
                    tabs.forEachIndexed { idx, label ->
                        val isSelected = selectedVariant == idx
                        FilledTonalButton(
                            onClick = { selectedVariant = idx },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("logo_variant_tab_$idx"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color(0xFF131C31),
                                contentColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main High-Res 1:1 Logo Preview Canvas
                when (selectedVariant) {
                    0 -> {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFF6366F1), Color(0xFFA855F7))
                                    ),
                                    shape = RoundedCornerShape(36.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kallesh_ref_logo),
                                contentDescription = "Kallesh AI Hub Reference App Icon",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Main 'K' Neural-Orbital App Icon Mark (1:1)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "3D glass-metal geometric 'K' integrated with AI neural circuit nodes & subtle orbital ring in electric cyan, blue, and violet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                    1 -> {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFF6366F1), Color(0xFFA855F7))
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kallesh_full_logo),
                                contentDescription = "Kallesh AI Hub Full Brand Lockup",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Full Brand Lockup — KALLESH AI HUB (1:1)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Centered startup emblem paired with bold geometric 'KALLESH' and visually highlighted 'AI HUB' futuristic typography.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        val previewBgColor = when (transparentPreviewBg) {
                            0 -> Color(0xFF070A12)
                            1 -> Color(0xFF1E293B)
                            else -> Color(0xFFF1F5F9)
                        }
                        val primaryTextColor = if (transparentPreviewBg == 2) Color(0xFF0F172A) else Color.White
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(previewBgColor)
                                .border(
                                    width = 1.5.dp,
                                    color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(28.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_kallesh_k_transparent),
                                    contentDescription = "Transparent Vector K Symbol",
                                    modifier = Modifier.size(164.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "KALLESH ",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = primaryTextColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "AI HUB",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Dark Navy", "Slate Grid", "Light Surface").forEachIndexed { idx, name ->
                                AssistChip(
                                    onClick = { transparentPreviewBg = idx },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scalable Transparent-Background Vector Mark",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Resolution-independent vector symbol + horizontal typography lockup for transparent overlays, headers, and mobile app icons.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Small App-Icon Scale Comparison Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11192C)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kallesh_ref_logo),
                                contentDescription = "64dp Icon",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Launcher 56dp", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kallesh_ref_logo),
                                contentDescription = "40dp Nav Icon",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Circle 40dp", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_kallesh_k_transparent),
                                contentDescription = "32dp Vector Badge",
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Vector 36dp", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}
