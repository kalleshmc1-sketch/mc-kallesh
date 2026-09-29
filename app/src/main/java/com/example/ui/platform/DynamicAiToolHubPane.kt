package com.example.ui.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SubscriptionPlan
import com.example.platform.AiToolCategory
import com.example.platform.FeatureBadge
import com.example.platform.FeatureStatus
import com.example.platform.PlatformFeature
import com.example.platform.RoutedAiExecutionResult
import com.example.ui.home.RichMarkdownAndCodeContent
import com.example.ui.home.copyToClipboard
import com.example.ui.viewmodel.KalleshHubViewModel

fun resolveFeatureIcon(iconKey: String): ImageVector = when (iconKey.lowercase()) {
    "chat" -> Icons.AutoMirrored.Filled.Chat
    "search" -> Icons.Default.Search
    "science" -> Icons.Default.Science
    "image" -> Icons.Default.Image
    "brush" -> Icons.Default.Brush
    "videocam" -> Icons.Default.Videocam
    "music_note" -> Icons.Default.MusicNote
    "record_voice_over" -> Icons.Default.RecordVoiceOver
    "mic" -> Icons.Default.Mic
    "visibility" -> Icons.Default.Visibility
    "document_scanner" -> Icons.Default.DocumentScanner
    "description" -> Icons.Default.Description
    "picture_as_pdf" -> Icons.Default.PictureAsPdf
    "folder_open" -> Icons.Default.FolderOpen
    "code" -> Icons.Default.Code
    "bug_report" -> Icons.Default.BugReport
    "terminal" -> Icons.Default.Terminal
    "school" -> Icons.Default.School
    "edit_note" -> Icons.Default.EditNote
    "translate" -> Icons.Default.Translate
    "summarize" -> Icons.Default.Summarize
    "slideshow" -> Icons.Default.Slideshow
    "analytics" -> Icons.Default.Analytics
    "public" -> Icons.Default.Public
    "smart_toy" -> Icons.Default.SmartToy
    "account_tree" -> Icons.Default.AccountTree
    "task_alt" -> Icons.Default.TaskAlt
    "workspaces" -> Icons.Default.Workspaces
    "build" -> Icons.Default.Build
    else -> Icons.Default.AutoAwesome
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DynamicAiToolHubPane(
    viewModel: KalleshHubViewModel,
    features: List<PlatformFeature>,
    activeFeature: PlatformFeature?,
    userPlan: SubscriptionPlan,
    installedAppVersion: String,
    isGenerating: Boolean,
    studioResultText: String,
    lastRouterExecution: RoutedAiExecutionResult?,
    onSelectFeature: (PlatformFeature?) -> Unit,
    onShowWhatsNew: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AiToolCategory?>(null) }
    var filterNewOrBetaOnly by remember { mutableStateOf(false) }

    // If an active feature is selected for interactive execution inside the Universal Runner:
    if (activeFeature != null) {
        DynamicFeatureRunnerView(
            viewModel = viewModel,
            feature = activeFeature,
            userPlan = userPlan,
            isGenerating = isGenerating,
            studioResultText = studioResultText,
            lastRouterExecution = lastRouterExecution,
            onBackToCatalog = { onSelectFeature(null) }
        )
        return
    }

    val filteredFeatures = remember(features, searchQuery, selectedCategory, filterNewOrBetaOnly) {
        features.filter { feat ->
            feat.isVisible &&
                (selectedCategory == null || feat.category == selectedCategory) &&
                (!filterNewOrBetaOnly || feat.badge != FeatureBadge.NONE) &&
                (searchQuery.isBlank() ||
                    feat.name.contains(searchQuery, ignoreCase = true) ||
                    feat.description.contains(searchQuery, ignoreCase = true) ||
                    feat.category.displayName.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dynamic_ai_tool_hub_pane"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scalable AI Tool Hub (${features.count { it.isVisible }} Modules)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Powered by Dynamic Feature Registry & AI Model Router • App v$installedAppVersion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = onShowWhatsNew,
                            label = { Text("What's New") },
                            leadingIcon = {
                                Icon(Icons.Default.NewReleases, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("hub_whats_new_chip")
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search 28+ AI categories, tools, or endpoints...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_tool_hub_search_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null && !filterNewOrBetaOnly,
                            onClick = {
                                selectedCategory = null
                                filterNewOrBetaOnly = false
                            },
                            label = { Text("All (${features.count { it.isVisible }})") }
                        )
                        FilterChip(
                            selected = filterNewOrBetaOnly,
                            onClick = { filterNewOrBetaOnly = !filterNewOrBetaOnly },
                            label = { Text("✨ NEW & BETA") }
                        )
                        AiToolCategory.entries.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = if (selectedCategory == category) null else category
                                },
                                label = { Text(category.displayName) }
                            )
                        }
                    }
                }
            }
        }

        items(filteredFeatures, key = { it.featureId }) { feature ->
            val statusColor = when (feature.status) {
                FeatureStatus.ACTIVE -> Color(0xFF00E676)
                FeatureStatus.MAINTENANCE -> Color(0xFFFFAB00)
                FeatureStatus.COMING_SOON -> Color(0xFF00E5FF)
                FeatureStatus.DISABLED -> MaterialTheme.colorScheme.error
            }
            val isPlanAllowed = feature.isPlanEntitled(userPlan)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (feature.badge == FeatureBadge.NEW) Color(0xFF00E5FF).copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("feature_card_${feature.featureId}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = resolveFeatureIcon(feature.iconKey),
                                    contentDescription = feature.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = feature.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (feature.badge != FeatureBadge.NONE) {
                                        Surface(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = feature.badge.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF00E5FF),
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${feature.category.displayName} • v${feature.version} • Rollout ${feature.rolloutPercentage}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = feature.status.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = feature.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Router: ${feature.taskType.primaryModelId} → ${feature.taskType.fallbackModelId}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Limit (${userPlan.badgeText}): ${feature.dailyLimitForPlan(userPlan)}/day",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (!isPlanAllowed) {
                                    viewModel.showStatus("${feature.name} requires ${feature.allowedPlans.joinToString("/")} plan. Upgrade below to unlock!")
                                    viewModel.navigateToSection(com.example.ui.viewmodel.HubSection.UPGRADE)
                                } else {
                                    viewModel.launchDynamicFeature(
                                        feature = feature,
                                        onOpenInToolRunner = { onSelectFeature(feature) }
                                    )
                                }
                            },
                            enabled = feature.status == FeatureStatus.ACTIVE,
                            modifier = Modifier.testTag("launch_feature_${feature.featureId}")
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when {
                                    feature.status == FeatureStatus.MAINTENANCE -> "In Maintenance"
                                    feature.status == FeatureStatus.COMING_SOON -> "Coming Soon"
                                    !isPlanAllowed -> "Upgrade to Unlock"
                                    else -> "Open AI Tool"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DynamicFeatureRunnerView(
    viewModel: KalleshHubViewModel,
    feature: PlatformFeature,
    userPlan: SubscriptionPlan,
    isGenerating: Boolean,
    studioResultText: String,
    lastRouterExecution: RoutedAiExecutionResult?,
    onBackToCatalog: () -> Unit
) {
    val context = LocalContext.current
    var promptText by remember(feature.featureId) { mutableStateOf("") }
    var forceWebGrounding by remember(feature.featureId) {
        mutableStateOf(feature.taskType.usesSearchGrounding)
    }
    var attachedMime by remember { mutableStateOf<String?>(null) }
    var attachedB64 by remember { mutableStateOf<String?>(null) }
    var attachedName by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val read = viewModel.readUriAsBase64(context, uri)
            if (read != null) {
                attachedMime = read.first
                attachedB64 = read.second
                attachedName = read.third
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dynamic_feature_runner_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = onBackToCatalog) {
                            Text("← All AI Tools")
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${feature.category.displayName} • v${feature.version}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = feature.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = feature.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "How to use: ${feature.howToUse}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Backend Route: ${feature.backendEndpoint} • Primary: ${feature.taskType.primaryModelId} (Fallback: ${feature.taskType.fallbackModelId})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text(feature.inputPlaceholder) },
                        minLines = 4,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dynamic_runner_prompt_input")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { filePicker.launch("*/*") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (attachedName.isBlank()) "Attach File / Image / PDF" else attachedName)
                        }
                        FilterChip(
                            selected = forceWebGrounding,
                            onClick = { forceWebGrounding = !forceWebGrounding },
                            label = { Text("Live Google Search Grounding") }
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.executeDynamicFeatureWithRouter(
                                feature = feature,
                                prompt = promptText,
                                forceGrounding = forceWebGrounding,
                                inlineMimeType = attachedMime,
                                inlineBase64Data = attachedB64
                            )
                        },
                        enabled = (promptText.isNotBlank() || attachedB64 != null) && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dynamic_runner_execute_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Routing via ${feature.taskType.primaryModelId}...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Execute ${feature.name}")
                        }
                    }
                }
            }
        }

        if (studioResultText.isNotBlank()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AI Router Output",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (lastRouterExecution != null) {
                                    Text(
                                        text = buildString {
                                            append("Model: ${lastRouterExecution.actualModelUsed}")
                                            if (lastRouterExecution.usedFallbackProvider) {
                                                append(" (Fallback Activated)")
                                            }
                                            append(" • ${lastRouterExecution.responseTimeMs}ms • ~${lastRouterExecution.estimatedTokens} tokens")
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { viewModel.speakTextAloud(studioResultText) }) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Speak Output")
                                }
                                IconButton(onClick = { copyToClipboard(context, studioResultText) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Output")
                                }
                            }
                        }
                        HorizontalDivider()
                        RichMarkdownAndCodeContent(
                            rawContent = studioResultText,
                            onCopyCode = { copyToClipboard(context, it) }
                        )

                        AnimatedVisibility(
                            visible = lastRouterExecution?.citations?.isNotEmpty() == true
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                HorizontalDivider()
                                Text(
                                    text = "Grounded Web Sources:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                lastRouterExecution?.citations?.forEach { citation ->
                                    Text(
                                        text = "• $citation",
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
    }
}
