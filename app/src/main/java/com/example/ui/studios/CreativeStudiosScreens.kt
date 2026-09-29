package com.example.ui.studios

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.UiState
import com.example.data.model.WorkspaceItem
import com.example.ui.viewmodel.CreativeStudioTab
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeStudiosHubScreen(
    viewModel: KalleshHubViewModel,
    selectedTab: CreativeStudioTab,
    isGenerating: Boolean,
    latestImagePath: String?,
    latestMusicPath: String?,
    liveVoiceHistory: List<Pair<String, String>>,
    workspaceItemsState: UiState<List<WorkspaceItem>>,
    isPlayingAudio: Boolean
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val allItems = (workspaceItemsState as? UiState.Success)?.data.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("creative_studios_screen")
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 12.dp
        ) {
            CreativeStudioTab.entries.forEach { tab ->
                val icon = when (tab) {
                    CreativeStudioTab.IMAGE -> Icons.Default.Image
                    CreativeStudioTab.VIDEO -> Icons.Default.Movie
                    CreativeStudioTab.MUSIC -> Icons.Default.MusicNote
                    CreativeStudioTab.VOICE_LIVE -> Icons.Default.GraphicEq
                }
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.openCreativeStudio(tab) },
                    text = { Text(tab.title) },
                    icon = { Icon(icon, contentDescription = tab.title) },
                    modifier = Modifier.testTag("studio_tab_${tab.name.lowercase()}")
                )
            }
        }

        when (selectedTab) {
            CreativeStudioTab.IMAGE -> ImageGenerationStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                latestImagePath = latestImagePath,
                savedImages = allItems.filter { it.type == "IMAGE" }
            )
            CreativeStudioTab.VIDEO -> VideoGenerationStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                savedVideos = allItems.filter { it.type == "VIDEO" }
            )
            CreativeStudioTab.MUSIC -> MusicLabStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                latestMusicPath = latestMusicPath,
                isPlayingAudio = isPlayingAudio,
                savedTracks = allItems.filter { it.type == "MUSIC" }
            )
            CreativeStudioTab.VOICE_LIVE -> LiveVoiceStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                liveVoiceHistory = liveVoiceHistory,
                isPlayingAudio = isPlayingAudio
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImageGenerationStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    latestImagePath: String?,
    savedImages: List<WorkspaceItem>
) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var selectedAspect by remember { mutableStateOf("1:1") }
    var selectedQuality by remember { mutableStateOf("1K") }
    var selectedStyle by remember { mutableStateOf("Futuristic Neon") }
    var sourcePhotoBase64 by remember { mutableStateOf<String?>(null) }
    var sourcePhotoName by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                sourcePhotoBase64 = loaded.second
                sourcePhotoName = loaded.third
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AI Image Studio • Gemini 3.1 Flash Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate ultra-sharp visuals or upload a photo to edit with natural language instructions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("Describe the image or photo edit...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_studio_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Text("Style Preset", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Futuristic Neon", "Photorealistic", "3D Glass-Metal", "Digital Art", "Anime", "Cinematic").forEach { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                                label = { Text(style) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1:1", "16:9", "9:16", "4:3", "3:4").forEach { ratio ->
                                    FilterChip(
                                        selected = selectedAspect == ratio,
                                        onClick = { selectedAspect = ratio },
                                        label = { Text(ratio) }
                                    )
                                }
                            }
                        }
                    }

                    Text("Resolution / Size", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("512px", "1K", "2K", "4K").forEach { size ->
                            FilterChip(
                                selected = selectedQuality == size,
                                onClick = { selectedQuality = size },
                                label = { Text(size) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.testTag("pick_reference_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (sourcePhotoName.isBlank()) "Upload Photo to Edit" else "Ref: $sourcePhotoName")
                        }

                        if (sourcePhotoBase64 != null) {
                            AssistChip(
                                onClick = {
                                    sourcePhotoBase64 = null
                                    sourcePhotoName = ""
                                },
                                label = { Text("Clear Photo") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val styledPrompt = "$prompt, style: $selectedStyle"
                            viewModel.generateOrEditStudioImage(
                                prompt = styledPrompt,
                                aspectRatio = selectedAspect,
                                qualitySize = selectedQuality,
                                sourceImageBase64 = sourcePhotoBase64
                            )
                        },
                        enabled = prompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_image_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rendering with Gemini 3.1 Flash Image...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (sourcePhotoBase64 != null) "Edit Photo with AI" else "Generate AI Image")
                        }
                    }
                }
            }
        }

        if (!latestImagePath.isNullOrBlank()) {
            item {
                GeneratedImagePreviewCard(
                    title = "Latest Generated Image",
                    filePath = latestImagePath,
                    subtitle = "$selectedAspect • $selectedQuality"
                )
            }
        }

        if (savedImages.isNotEmpty()) {
            item {
                Text(
                    text = "YOUR IMAGE STUDIO GALLERY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(savedImages, key = { it.id }) { item ->
                GeneratedImagePreviewCard(
                    title = item.title,
                    filePath = item.mediaData,
                    subtitle = "${item.aspectRatio} • ${item.category}"
                )
            }
        }
    }
}

@Composable
private fun GeneratedImagePreviewCard(
    title: String,
    filePath: String,
    subtitle: String
) {
    val bitmap = remember(filePath) {
        val file = File(filePath)
        if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VideoGenerationStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    savedVideos: List<WorkspaceItem>
) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var aspectRatio by remember { mutableStateOf("16:9") }
    var startingPhotoBase64 by remember { mutableStateOf<String?>(null) }
    var startingPhotoName by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                startingPhotoBase64 = loaded.second
                startingPhotoName = loaded.third
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Veo 3 Video Studio • veo-3.1-fast-generate-preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate cinematic text-to-video scenes or animate an uploaded photo in 16:9 Landscape or 9:16 Vertical.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("Describe camera motion, subject, and lighting...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_studio_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = aspectRatio == "16:9",
                            onClick = { aspectRatio = "16:9" },
                            label = { Text("16:9 Landscape") }
                        )
                        FilterChip(
                            selected = aspectRatio == "9:16",
                            onClick = { aspectRatio = "9:16" },
                            label = { Text("9:16 Vertical") }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (startingPhotoName.isBlank()) "Optional: Animate Photo" else "Frame: $startingPhotoName")
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.generateVeoVideo(
                                prompt = prompt,
                                aspectRatio = aspectRatio,
                                sourceImageBase64 = startingPhotoBase64
                            )
                        },
                        enabled = prompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_video_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submitting to Veo 3 Engine...")
                        } else {
                            Icon(Icons.Default.Movie, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Veo 3 Video")
                        }
                    }
                }
            }
        }

        if (savedVideos.isNotEmpty()) {
            item {
                Text(
                    text = "VEO 3 VIDEO RENDERING QUEUE & HISTORY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(savedVideos, key = { it.id }) { item ->
                val previewBitmap = remember(item.mediaData) {
                    if (item.mediaData.isNotBlank() && item.mediaData.endsWith(".png")) {
                        val f = File(item.mediaData)
                        if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                    } else null
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = { viewModel.pollVideoJobStatus(item) },
                                label = { Text(item.status) },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = "Poll Status", modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                        Text(
                            text = "${item.category} • Aspect ${item.aspectRatio}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.mediaData.isNotBlank()) {
                            Text(
                                text = if (previewBitmap != null) "HD Storyboard Keyframe Preview Rendered" else "Output URI: ${item.mediaData}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicLabStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    latestMusicPath: String?,
    isPlayingAudio: Boolean,
    savedTracks: List<WorkspaceItem>
) {
    var prompt by remember { mutableStateOf("") }
    var useProFullSong by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lyria 3 Music Lab",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Compose 30-second studio loops (lyria-3-clip-preview) or full-length songs with lyrics (lyria-3-pro-preview).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("Describe genre, instruments, BPM, mood, or lyrics...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_studio_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !useProFullSong,
                            onClick = { useProFullSong = false },
                            label = { Text("30s Studio Clip (Lyria 3 Clip)") }
                        )
                        FilterChip(
                            selected = useProFullSong,
                            onClick = { useProFullSong = true },
                            label = { Text("Full Track (Lyria 3 Pro)") }
                        )
                    }

                    Button(
                        onClick = { viewModel.generateMusic(prompt, useProFullSong) },
                        enabled = prompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_music_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Composing Track with Lyria 3...")
                        } else {
                            Icon(Icons.Default.MusicNote, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Music")
                        }
                    }
                }
            }
        }

        if (!latestMusicPath.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Latest Generated Track", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Ready for playback", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                if (isPlayingAudio) {
                                    viewModel.audioVoiceManager.togglePauseResume()
                                } else {
                                    viewModel.audioVoiceManager.playWavFile(latestMusicPath)
                                }
                            }
                        ) {
                            Icon(
                                if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlayingAudio) "Pause" else "Play")
                        }
                    }
                }
            }
        }

        items(savedTracks, key = { it.id }) { track ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(track.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (track.mediaData.isNotBlank()) {
                        OutlinedButton(onClick = { viewModel.audioVoiceManager.playWavFile(track.mediaData) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveVoiceStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    liveVoiceHistory: List<Pair<String, String>>,
    isPlayingAudio: Boolean
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var manualPrompt by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecording = true
            viewModel.startVoiceRecording()
        } else {
            viewModel.showStatus("Microphone permission is needed for Live Voice.")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(44.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.GraphicEq,
                            contentDescription = "Live Voice Orb",
                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Text(
                        text = "Kallesh Live Voice • gemini-3.8-live",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRecording) {
                            "Listening... Tap Stop to transcribe with gemini-3.5-transcribe & speak reply."
                        } else if (isPlayingAudio) {
                            "Kallesh AI is speaking..."
                        } else {
                            "Tap the microphone to talk naturally or type below for spoken AI conversation."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (isRecording) {
                                    isRecording = false
                                    viewModel.stopVoiceRecordingAndTranscribe { transcript ->
                                        viewModel.sendLiveVoiceMessage(transcript)
                                    }
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        isRecording = true
                                        viewModel.startVoiceRecording()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("live_voice_mic_button")
                        ) {
                            Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRecording) "Stop & Send Audio" else "Tap to Speak")
                        }

                        if (isPlayingAudio) {
                            OutlinedButton(onClick = { viewModel.audioVoiceManager.stopPlayback() }) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop Audio")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualPrompt,
                            onValueChange = { manualPrompt = it },
                            placeholder = { Text("Or ask Live Voice via text...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("live_voice_text_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.sendLiveVoiceMessage(manualPrompt)
                                manualPrompt = ""
                            },
                            enabled = manualPrompt.isNotBlank() && !isGenerating,
                            modifier = Modifier.testTag("live_voice_send_button")
                        ) {
                            Text("Speak")
                        }
                    }
                }
            }
        }

        items(liveVoiceHistory) { (role, text) ->
            Surface(
                color = if (role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (role == "user") "You (Spoken)" else "Kallesh Live Voice",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
