package com.example.ui.home

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.CachedPromptEntity
import com.example.data.model.AVAILABLE_CHAT_MODELS
import com.example.data.model.AiModelOption
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.DailyUsage
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SystemConfig
import com.example.data.model.UiState
import com.example.data.model.UsageCategory
import com.example.data.model.UserProfile
import com.example.ui.auth.KalleshBrandLogoShowcaseDialog
import com.example.ui.viewmodel.CreativeStudioTab
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel
import com.example.ui.viewmodel.ProductivityToolTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeHubScreen(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?,
    todayUsageState: UiState<DailyUsage>,
    conversationsState: UiState<List<Conversation>>,
    founderConfig: SystemConfig,
    prompts: List<CachedPromptEntity>
) {
    var showBrandShowcase by remember { mutableStateOf(false) }
    val plan = userProfile?.subscriptionPlan ?: SubscriptionPlan.FREE
    val usage = (todayUsageState as? UiState.Success)?.data ?: DailyUsage()

    if (showBrandShowcase) {
        KalleshBrandLogoShowcaseDialog(onDismiss = { showBrandShowcase = false })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Futuristic Hero Banner with Brand Lockup & Founder Audio Trigger
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0A1124),
                                    Color(0xFF152244),
                                    Color(0xFF1E1B4B)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF6366F1), Color(0xFFA855F7))
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_kallesh_k_symbol),
                                    contentDescription = "Kallesh AI Hub Symbol",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "KALLESH ",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "AI HUB",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF00E5FF),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Text(
                                        text = "One AI Hub. Limitless Possibilities.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = plan.badgeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (founderConfig.announcement.isNotBlank()) {
                            Surface(
                                color = Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "✨ ${founderConfig.announcement}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startNewChat() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_start_chat_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start AI Chat")
                            }

                            OutlinedButton(
                                onClick = { viewModel.openFounderIntroModal(founderConfig) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_play_founder_intro_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Founder Intro", color = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openProductivityTool(ProductivityToolTab.HUB) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_open_ai_tool_hub_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("28+ AI Tool Hub", color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setShowWhatsNewModal(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_whats_new_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("What's New & Updates", color = Color(0xFF00E5FF))
                            }
                        }
                    }
                }
            }
        }

        // 2. Official Brand Logo & Identity Card (Direct Showcase of K Symbol + Full Lockup + Vector)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBrandShowcase = true }
                    .testTag("brand_logo_showcase_card"),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_kallesh_full_logo),
                        contentDescription = "Kallesh AI Hub Full Logo Lockup",
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kallesh AI Hub Brand & Logo Suite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to inspect the high-res 1:1 'K' Neural Symbol, Full 'KALLESH AI HUB' Lockup, and Transparent Vector Mark.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_kallesh_k_transparent),
                        contentDescription = "Transparent Vector K",
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        // 3. Separated AI Sections Bar / Quick Launch Grid
        item {
            Text(
                text = "AI STUDIOS & SPECIALIZED SECTIONS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val studioCards = listOf(
                Triple("AI Chat & Models", "Streaming chat, Search & Maps grounding", Icons.AutoMirrored.Filled.Chat) to {
                    viewModel.navigateToSection(HubSection.CHAT)
                },
                Triple("AI Image Studio", "Create & edit 1K-4K images with Flash Image", Icons.Default.Image) to {
                    viewModel.openCreativeStudio(CreativeStudioTab.IMAGE)
                },
                Triple("Veo 3 Video Studio", "16:9 & 9:16 text/photo-to-video generation", Icons.Default.Movie) to {
                    viewModel.openCreativeStudio(CreativeStudioTab.VIDEO)
                },
                Triple("Lyria 3 Music Lab", "Generate AI soundtracks & full pro songs", Icons.Default.MusicNote) to {
                    viewModel.openCreativeStudio(CreativeStudioTab.MUSIC)
                },
                Triple("Live Voice & Audio", "Gemini 3.8 Live voice & speech transcription", Icons.Default.GraphicEq) to {
                    viewModel.openCreativeStudio(CreativeStudioTab.VOICE_LIVE)
                },
                Triple("Document & PDF AI", "Upload files for deep summary & Q&A", Icons.Default.Description) to {
                    viewModel.openProductivityTool(ProductivityToolTab.DOCUMENT)
                },
                Triple("Kallesh Code AI", "Generate, debug & convert in 13 languages", Icons.Default.Code) to {
                    viewModel.openProductivityTool(ProductivityToolTab.CODE)
                },
                Triple("Study & Writing AI", "Quizzes, flashcards, essays & translation", Icons.Default.School) to {
                    viewModel.openProductivityTool(ProductivityToolTab.STUDY)
                }
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                studioCards.forEachIndexed { idx, (meta, action) ->
                    ElevatedCard(
                        onClick = action,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_section_card_$idx"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = meta.third,
                                contentDescription = meta.first,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = meta.first,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = meta.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Daily Usage & Per-Day Limits Meter Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's AI Usage & Daily Limits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Active Tier: ${plan.displayName} (${plan.priceLabel})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { viewModel.navigateToSection(HubSection.UPGRADE) },
                            modifier = Modifier.testTag("home_upgrade_plan_button")
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upgrade")
                        }
                    }

                    UsageMeterRow(
                        label = "AI Messages",
                        used = usage.chatCount,
                        limit = plan.dailyChatLimit,
                        onUpgradeClick = { viewModel.navigateToSection(HubSection.UPGRADE) }
                    )
                    UsageMeterRow(
                        label = "Image Generations",
                        used = usage.imageCount,
                        limit = plan.dailyImageLimit,
                        onUpgradeClick = { viewModel.navigateToSection(HubSection.UPGRADE) }
                    )
                    UsageMeterRow(
                        label = "Veo 3 Videos",
                        used = usage.videoCount,
                        limit = plan.dailyVideoLimit,
                        onUpgradeClick = { viewModel.navigateToSection(HubSection.UPGRADE) }
                    )
                    UsageMeterRow(
                        label = "File & Doc Analyses",
                        used = usage.fileCount,
                        limit = plan.dailyFileLimit,
                        onUpgradeClick = { viewModel.navigateToSection(HubSection.UPGRADE) }
                    )
                    UsageMeterRow(
                        label = "Live Voice & Audio Turns",
                        used = usage.voiceCount,
                        limit = plan.dailyVoiceLimit,
                        onUpgradeClick = { viewModel.navigateToSection(HubSection.UPGRADE) }
                    )
                    UsageMeterRow(
                        label = "Lyria 3 Music Tracks",
                        used = usage.musicCount,
                        limit = plan.dailyMusicLimit,
                        onUpgradeClick = { viewModel.navigateToSection(HubSection.UPGRADE) }
                    )
                }
            }
        }

        // 5. Quick Starter Prompts from Local Room Cache
        if (prompts.isNotEmpty()) {
            item {
                Text(
                    text = "ONE-TAP STARTER PROMPTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    prompts.take(5).forEach { prompt ->
                        Card(
                            onClick = {
                                viewModel.startNewChat()
                                viewModel.sendChatMessage(prompt.promptText)
                            },
                            modifier = Modifier.width(240.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = prompt.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prompt.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prompt.promptText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Recent Conversations (Persisted in Local Room DB)
        item {
            val conversations = (conversationsState as? UiState.Success)?.data.orEmpty()
            if (conversations.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PREVIOUS CONVERSATIONS (${conversations.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = { viewModel.navigateToSection(HubSection.CHAT) },
                        modifier = Modifier.testTag("home_view_all_chats_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View All History")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    conversations.take(5).forEach { conv ->
                        Card(
                            onClick = { viewModel.openConversation(conv.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_recent_conversation_${conv.id}"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (conv.isPinned) {
                                            Icon(
                                                Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = conv.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = conv.lastMessagePreview.ifBlank { "Tap to view messages..." },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${conv.messageCount} messages • ${conv.modelId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AssistChip(
                                    onClick = { viewModel.openConversation(conv.id) },
                                    label = { Text(conv.folder, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageMeterRow(
    label: String,
    used: Int,
    limit: Int,
    onUpgradeClick: (() -> Unit)? = null
) {
    val isExhausted = used >= limit && limit > 0
    val progress = if (limit > 0) (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isExhausted) "LIMIT REACHED ($used / $limit)" else "$used / $limit today",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExhausted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (isExhausted && onUpgradeClick != null) {
                    AssistChip(
                        onClick = onUpgradeClick,
                        label = { Text("Upgrade", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            color = if (isExhausted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainChatScreen(
    viewModel: KalleshHubViewModel,
    conversationsState: UiState<List<Conversation>>,
    messagesState: UiState<List<ChatMessage>>,
    activeConversationId: String?,
    selectedModel: AiModelOption,
    enableSearchGrounding: Boolean,
    enableMapsGrounding: Boolean,
    isGenerating: Boolean,
    streamingReplyText: String,
    chatSearchQuery: String,
    selectedFolder: String
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val context = LocalContext.current
    val allPersistedMessages by viewModel.allPersistedMessages.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val todayUsageState by viewModel.todayUsageState.collectAsStateWithLifecycle()
    val activePlan = (userProfileState as? UiState.Success)?.data?.subscriptionPlan ?: SubscriptionPlan.FREE
    val todayUsage = (todayUsageState as? UiState.Success)?.data ?: DailyUsage()
    val isChatLimitReached = todayUsage.chatCount >= activePlan.dailyChatLimit
    val allConversations = (conversationsState as? UiState.Success)?.data.orEmpty()
    var inputText by remember { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var editDraftText by remember { mutableStateOf("") }
    var renamingConversation by remember { mutableStateOf<Conversation?>(null) }
    var renameDraftTitle by remember { mutableStateOf("") }
    var renameDraftFolder by remember { mutableStateOf("General") }
    var isRecordingMic by remember { mutableStateOf(false) }

    // Attached file/image state
    var attachedMime by remember { mutableStateOf<String?>(null) }
    var attachedBase64 by remember { mutableStateOf<String?>(null) }
    var attachedName by remember { mutableStateOf("") }

    // Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                attachedMime = loaded.first
                attachedBase64 = loaded.second
                attachedName = loaded.third
            }
        }
    }

    // Document / File Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                attachedMime = loaded.first
                attachedBase64 = loaded.second
                attachedName = loaded.third
            }
        }
    }

    // Microphone permission launcher for Gemini 3.5 Transcribe
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecordingMic = true
            viewModel.startVoiceRecording()
        } else {
            viewModel.showStatus("Microphone permission is required for voice transcription.")
        }
    }

    val messages = (messagesState as? UiState.Success)?.data.orEmpty()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, streamingReplyText) {
        val totalItems = messages.size + if (streamingReplyText.isNotBlank()) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    if (editingMessage != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Edit & Resend Message") },
            text = {
                OutlinedTextField(
                    value = editDraftText,
                    onValueChange = { editDraftText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = editingMessage
                        if (msg != null && editDraftText.isNotBlank()) {
                            viewModel.editUserMessageAndResend(msg.id, editDraftText)
                        }
                        editingMessage = null
                    }
                ) {
                    Text("Update & Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (renamingConversation != null) {
        AlertDialog(
            onDismissRequest = { renamingConversation = null },
            title = { Text("Organize Conversation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = renameDraftTitle,
                        onValueChange = { renameDraftTitle = it },
                        label = { Text("Conversation Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Folder", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("General", "Coding", "Study", "Business", "Creative").forEach { folder ->
                            FilterChip(
                                selected = renameDraftFolder == folder,
                                onClick = { renameDraftFolder = folder },
                                label = { Text(folder) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val conv = renamingConversation
                        if (conv != null) {
                            viewModel.renameChat(conv.id, renameDraftTitle, renameDraftFolder)
                        }
                        renamingConversation = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingConversation = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear Local Chat History?") },
            text = {
                Text("This will remove all locally persisted conversations and messages from the Room database on this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChatHistory()
                        showClearHistoryConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_chat_screen")
    ) {
        // Top Chat Control Bar: History toggle, Model switcher, Grounding chips, Export
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalIconButtonWithLabel(
                            icon = Icons.Default.History,
                            label = if (showHistorySheet) "Hide History (${allConversations.size})" else "Chats (${allConversations.size})",
                            onClick = { showHistorySheet = !showHistorySheet },
                            testTag = "toggle_chat_history_button"
                        )
                        FilledTonalIconButtonWithLabel(
                            icon = Icons.Default.Add,
                            label = "New Chat",
                            onClick = { viewModel.startNewChat() },
                            testTag = "new_chat_button"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val exported = viewModel.formatConversationForExport("MARKDOWN")
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/markdown"
                                    putExtra(Intent.EXTRA_SUBJECT, "Kallesh AI Hub Chat Export")
                                    putExtra(Intent.EXTRA_TEXT, exported)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Conversation"))
                            },
                            modifier = Modifier.testTag("export_chat_button")
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = "Export Conversation")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal Model Selector + Live Grounding Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AVAILABLE_CHAT_MODELS.forEach { option ->
                        val isSelected = selectedModel.alias == option.alias
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectModel(option) },
                            label = { Text("${option.alias} (${option.badge})") },
                            modifier = Modifier.testTag("model_chip_${option.badge.lowercase()}")
                        )
                    }

                    FilterChip(
                        selected = enableSearchGrounding,
                        onClick = { viewModel.toggleSearchGrounding() },
                        leadingIcon = {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Google Search") },
                        modifier = Modifier.testTag("toggle_search_grounding_chip")
                    )

                    FilterChip(
                        selected = enableMapsGrounding,
                        onClick = { viewModel.toggleMapsGrounding() },
                        leadingIcon = {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Google Maps") },
                        modifier = Modifier.testTag("toggle_maps_grounding_chip")
                    )
                }
            }
        }

        // Collapsible Conversation History Drawer Panel
        AnimatedVisibility(visible = showHistorySheet) {
            ChatHistoryPanel(
                conversationsState = conversationsState,
                allPersistedMessages = allPersistedMessages,
                activeConversationId = activeConversationId,
                searchQuery = chatSearchQuery,
                selectedFolder = selectedFolder,
                onSearchChange = viewModel::updateChatSearchQuery,
                onSelectFolder = viewModel::selectFolder,
                onSelectConversation = { id ->
                    viewModel.openConversation(id)
                    showHistorySheet = false
                },
                onPinToggle = viewModel::togglePinChat,
                onRename = { conv ->
                    renamingConversation = conv
                    renameDraftTitle = conv.title
                    renameDraftFolder = conv.folder
                },
                onShareToggle = viewModel::toggleShareChat,
                onDelete = viewModel::deleteChat,
                onClearAllHistory = { showClearHistoryConfirm = true }
            )
        }

        // Main Chat Transcript Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && streamingReplyText.isBlank()) {
                EmptyChatWelcomePlaceholder(
                    selectedModel = selectedModel,
                    previousConversations = allConversations,
                    onOpenConversation = { id -> viewModel.openConversation(id) },
                    onQuickPrompt = { prompt -> viewModel.sendChatMessage(prompt) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chat_messages_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id.ifBlank { it.hashCode().toString() } }) { msg ->
                        ChatMessageBubble(
                            message = msg,
                            onCopy = { text ->
                                copyToClipboard(context, text)
                                viewModel.showStatus("Copied response to clipboard.")
                            },
                            onSpeak = { text -> viewModel.speakTextAloud(text) },
                            onEdit = {
                                editingMessage = msg
                                editDraftText = msg.content
                            },
                            onRegenerate = { viewModel.regenerateLastResponse() },
                            onRate = { rating -> viewModel.rateMessage(msg.id, rating) },
                            onDelete = { viewModel.deleteSingleMessage(msg.id) }
                        )
                    }

                    if (streamingReplyText.isNotBlank() || isGenerating) {
                        item {
                            StreamingMessageBubble(
                                partialText = streamingReplyText,
                                modelAlias = selectedModel.alias,
                                onStop = { viewModel.stopGenerating() }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Multimodal Composer Bar
        Surface(
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (isChatLimitReached) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("chat_daily_limit_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Chat Limit Reached (${todayUsage.chatCount}/${activePlan.dailyChatLimit})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Upgrade your ${activePlan.displayName} plan to unlock up to ${SubscriptionPlan.ENTERPRISE.dailyChatLimit} messages/day.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val target = if (activePlan == SubscriptionPlan.FREE) SubscriptionPlan.PRO else SubscriptionPlan.ENTERPRISE
                                    viewModel.selectSubscriptionPlan(target)
                                },
                                modifier = Modifier.testTag("chat_limit_instant_upgrade_button")
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (activePlan == SubscriptionPlan.FREE) "Upgrade to Pro" else "Go Enterprise")
                            }
                        }
                    }
                }

                if (attachedName.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Attached: $attachedName") },
                            leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        TextButton(
                            onClick = {
                                attachedMime = null
                                attachedBase64 = null
                                attachedName = ""
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("attach_image_button")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Attach Image")
                    }

                    IconButton(
                        onClick = {
                            documentPickerLauncher.launch(arrayOf("application/pdf", "text/*", "application/json"))
                        },
                        modifier = Modifier.testTag("attach_file_button")
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                    }

                    IconButton(
                        onClick = {
                            if (isRecordingMic) {
                                isRecordingMic = false
                                viewModel.stopVoiceRecordingAndTranscribe { transcript ->
                                    inputText = if (inputText.isBlank()) transcript else "$inputText $transcript"
                                }
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    isRecordingMic = true
                                    viewModel.startVoiceRecording()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier.testTag("voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isRecordingMic) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecordingMic) "Stop Recording & Transcribe" else "Voice Input",
                            tint = if (isRecordingMic) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                if (isRecordingMic) "Listening... Tap stop to transcribe"
                                else "Message ${selectedModel.alias}..."
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4
                    )

                    if (isGenerating) {
                        IconButton(
                            onClick = { viewModel.stopGenerating() },
                            modifier = Modifier.testTag("stop_generating_button")
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop Generating",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.sendChatMessage(
                                    userText = inputText,
                                    attachedMimeType = attachedMime,
                                    attachedBase64 = attachedBase64,
                                    attachedFileName = attachedName
                                )
                                inputText = ""
                                attachedMime = null
                                attachedBase64 = null
                                attachedName = ""
                            },
                            enabled = inputText.isNotBlank() || !attachedBase64.isNullOrBlank(),
                            modifier = Modifier.testTag("send_message_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilledTonalIconButtonWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
private fun ChatHistoryPanel(
    conversationsState: UiState<List<Conversation>>,
    allPersistedMessages: List<ChatMessage>,
    activeConversationId: String?,
    searchQuery: String,
    selectedFolder: String,
    onSearchChange: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onPinToggle: (Conversation) -> Unit,
    onRename: (Conversation) -> Unit,
    onShareToggle: (Conversation) -> Unit,
    onDelete: (String) -> Unit,
    onClearAllHistory: () -> Unit
) {
    val allConversations = (conversationsState as? UiState.Success)?.data.orEmpty()
    val matchingMessagesByConvId = remember(allPersistedMessages, searchQuery) {
        if (searchQuery.isBlank()) emptyMap()
        else {
            allPersistedMessages
                .filter { it.content.contains(searchQuery, ignoreCase = true) }
                .groupBy { it.conversationId }
        }
    }
    val filtered = remember(allConversations, searchQuery, selectedFolder, matchingMessagesByConvId) {
        allConversations
            .filter { conv ->
                (selectedFolder == "All" || conv.folder.equals(selectedFolder, ignoreCase = true)) &&
                    (searchQuery.isBlank() ||
                        conv.title.contains(searchQuery, ignoreCase = true) ||
                        conv.lastMessagePreview.contains(searchQuery, ignoreCase = true) ||
                        matchingMessagesByConvId.containsKey(conv.id))
            }
            .sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.updatedAt?.seconds ?: 0L })
    }

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .testTag("chat_history_panel")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Persisted Room History • ${allConversations.size} Chats • ${allPersistedMessages.size} Messages",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (allConversations.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAllHistory,
                        modifier = Modifier.testTag("clear_all_chat_history_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search conversations or message history...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_conversations_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "General", "Coding", "Study", "Business", "Creative").forEach { folder ->
                    FilterChip(
                        selected = selectedFolder == folder,
                        onClick = { onSelectFolder(folder) },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text(folder) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No matching conversations yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filtered, key = { it.id }) { conv ->
                        val isActive = conv.id == activeConversationId
                        val matchedMsg = matchingMessagesByConvId[conv.id]?.firstOrNull()
                        Card(
                            onClick = { onSelectConversation(conv.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("history_conversation_item_${conv.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (conv.isPinned) {
                                            Icon(
                                                Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = conv.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    val previewLine = if (matchedMsg != null && searchQuery.isNotBlank()) {
                                        "Matched message: \"${matchedMsg.content.replace("\n", " ").take(90)}\""
                                    } else {
                                        "${conv.folder} • ${conv.messageCount} msgs • ${conv.lastMessagePreview}"
                                    }
                                    Text(
                                        text = previewLine,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onPinToggle(conv) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.PushPin, contentDescription = "Pin", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { onRename(conv) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { onShareToggle(conv) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { onDelete(conv.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatWelcomePlaceholder(
    selectedModel: AiModelOption,
    previousConversations: List<Conversation>,
    onOpenConversation: (String) -> Unit,
    onQuickPrompt: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.img_kallesh_k_symbol),
                    contentDescription = "Kallesh AI Hub Symbol",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Kallesh AI Chat • ${selectedModel.alias}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedModel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (previousConversations.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "PREVIOUS CONVERSATIONS (${previousConversations.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    previousConversations.take(4).forEach { conv ->
                        ElevatedCard(
                            onClick = { onOpenConversation(conv.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_previous_conversation_${conv.id}"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conv.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = conv.lastMessagePreview.ifBlank { "Open conversation..." },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AssistChip(
                                    onClick = { onOpenConversation(conv.id) },
                                    label = { Text("${conv.messageCount} msgs", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            val suggestions = listOf(
                "Explain quantum computing with a Markdown comparison table",
                "Write a Kotlin Coroutines Flow retry helper with unit test",
                "Create a 7-day full-stack AI startup launch checklist",
                "Analyze key trends in renewable energy storage for 2026"
            )
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "QUICK STARTER PROMPTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                suggestions.forEachIndexed { idx, prompt ->
                    OutlinedButton(
                        onClick = { onQuickPrompt(prompt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_suggestion_button_$idx"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(prompt, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onEdit: () -> Unit,
    onRegenerate: () -> Unit,
    onRate: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isUser) "You" else "Kallesh AI • ${message.modelId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (message.attachmentName.isNotBlank()) {
                        Text(
                            text = "📎 ${message.attachmentName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                RichMarkdownAndCodeContent(
                    rawContent = message.content,
                    onCopyCode = { code ->
                        copyToClipboard(context, code)
                    }
                )

                if (message.citations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "VERIFIED SOURCES & GROUNDING LINKS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.citations.forEach { cite ->
                            AssistChip(
                                onClick = { copyToClipboard(context, cite) },
                                label = {
                                    Text(
                                        text = cite.take(48),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message Action Toolbar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onCopy(message.content) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(15.dp))
                    }
                    if (isUser) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Message", modifier = Modifier.size(15.dp))
                        }
                    } else {
                        IconButton(onClick = { onSpeak(message.content) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read Aloud", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onRate("LIKE") }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                Icons.Default.ThumbUp,
                                contentDescription = "Helpful",
                                modifier = Modifier.size(15.dp),
                                tint = if (message.feedback == "LIKE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRate("DISLIKE") }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                Icons.Default.ThumbDown,
                                contentDescription = "Not Helpful",
                                modifier = Modifier.size(15.dp),
                                tint = if (message.feedback == "DISLIKE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Message",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingMessageBubble(
    partialText: String,
    modelAlias: String,
    onStop: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.widthIn(max = 560.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    text = "$modelAlias is streaming response...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onStop) {
                    Text("Stop")
                }
            }
            if (partialText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = partialText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun RichMarkdownAndCodeContent(
    rawContent: String,
    onCopyCode: (String) -> Unit
) {
    val segments = remember(rawContent) {
        rawContent.split("```")
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        segments.forEachIndexed { index, block ->
            if (index % 2 == 1) {
                // Fenced Code Block
                val lines = block.lines()
                val language = lines.firstOrNull()?.trim().orEmpty().ifBlank { "code" }
                val codeBody = if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else block.trim()

                Surface(
                    color = Color(0xFF090D16),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF)
                            )
                            TextButton(onClick = { onCopyCode(codeBody) }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFF00E5FF)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Code", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            text = codeBody,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            } else if (block.isNotBlank()) {
                Text(
                    text = block.trim(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Kallesh AI Hub", text))
}
