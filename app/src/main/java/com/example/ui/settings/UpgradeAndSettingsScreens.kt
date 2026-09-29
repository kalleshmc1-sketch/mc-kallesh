package com.example.ui.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailyUsage
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SystemConfig
import com.example.data.model.UiState
import com.example.data.model.UsageCategory
import com.example.data.model.UserProfile
import com.example.ui.auth.KalleshBrandLogoShowcaseDialog
import com.example.ui.platform.FounderPlatformAdminControlSection
import com.example.ui.home.UsageMeterRow
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsageAndUpgradeScreen(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?,
    todayUsageState: UiState<DailyUsage>
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val activePlan = userProfile?.subscriptionPlan ?: SubscriptionPlan.FREE
    val usage = (todayUsageState as? UiState.Success)?.data ?: DailyUsage()
    val nextTier = if (activePlan == SubscriptionPlan.FREE) SubscriptionPlan.PRO else SubscriptionPlan.ENTERPRISE

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("usage_and_upgrade_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Daily Usage & Plan Upgrade", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Real-time per-day quota tracking backed by Cloud Firestore",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Plan: ${activePlan.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Resets daily at 00:00 UTC • Max upload ${activePlan.maxFileSizeMb}MB", style = MaterialTheme.typography.bodySmall)
                        }
                        AssistChip(onClick = {}, label = { Text(activePlan.badgeText) })
                    }

                    UsageMeterRow(
                        label = "AI Chat Messages",
                        used = usage.chatCount,
                        limit = activePlan.dailyChatLimit,
                        onUpgradeClick = { viewModel.selectSubscriptionPlan(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Image Studio Generations",
                        used = usage.imageCount,
                        limit = activePlan.dailyImageLimit,
                        onUpgradeClick = { viewModel.selectSubscriptionPlan(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Veo 3 Video Generations",
                        used = usage.videoCount,
                        limit = activePlan.dailyVideoLimit,
                        onUpgradeClick = { viewModel.selectSubscriptionPlan(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Document & File Analyses",
                        used = usage.fileCount,
                        limit = activePlan.dailyFileLimit,
                        onUpgradeClick = { viewModel.selectSubscriptionPlan(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Live Voice & Audio Turns",
                        used = usage.voiceCount,
                        limit = activePlan.dailyVoiceLimit,
                        onUpgradeClick = { viewModel.selectSubscriptionPlan(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Lyria 3 Music Tracks",
                        used = usage.musicCount,
                        limit = activePlan.dailyMusicLimit,
                        onUpgradeClick = { viewModel.selectSubscriptionPlan(nextTier) }
                    )
                }
            }
        }

        // Daily Limit Simulator & Quota Reset Controls
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Limit & Upgrade Simulator",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Test reaching your daily limit for any AI category to preview the instant Upgrade prompt, or reset today's counters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.resetTodayUsageCounters() },
                            modifier = Modifier.testTag("reset_daily_usage_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Today")
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UsageCategory.entries.forEach { category ->
                            AssistChip(
                                onClick = { viewModel.simulateDailyLimitReached(category) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                label = { Text("Simulate ${category.label} Limit") },
                                modifier = Modifier.testTag("simulate_limit_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        SubscriptionPlan.entries.forEach { plan ->
            item {
                val isCurrent = plan == activePlan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(plan.priceLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            if (isCurrent) {
                                AssistChip(
                                    onClick = {},
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Active Plan") }
                                )
                            }
                        }

                        Text("• ${plan.dailyChatLimit} AI Chat Messages / day", style = MaterialTheme.typography.bodySmall)
                        Text("• ${plan.dailyImageLimit} AI Image Generations / day", style = MaterialTheme.typography.bodySmall)
                        Text("• ${plan.dailyVideoLimit} Veo 3 Video Generations / day", style = MaterialTheme.typography.bodySmall)
                        Text("• ${plan.dailyFileLimit} Document & PDF Analyses / day (up to ${plan.maxFileSizeMb}MB)", style = MaterialTheme.typography.bodySmall)
                        Text("• ${plan.dailyVoiceLimit} Live Voice & TTS turns / day", style = MaterialTheme.typography.bodySmall)
                        Text("• ${plan.dailyMusicLimit} Lyria 3 Music Tracks / day", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { viewModel.selectSubscriptionPlan(plan) },
                            enabled = !isCurrent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_plan_button_${plan.id.lowercase()}"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isCurrent) "Currently Active" else "Activate ${plan.displayName}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAndAdminScreen(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?,
    founderConfig: SystemConfig,
    onSignOut: () -> Unit
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    var displayName by remember(userProfile?.displayName) { mutableStateOf(userProfile?.displayName ?: "Explorer") }
    var preferredLanguage by remember(userProfile?.preferredLanguage) { mutableStateOf(userProfile?.preferredLanguage ?: "English") }
    var responseStyle by remember(userProfile?.responseStyle) { mutableStateOf(userProfile?.responseStyle ?: "Professional") }
    var defaultModel by remember(userProfile?.defaultModel) { mutableStateOf(userProfile?.defaultModel ?: "gemini-3-flash-preview") }
    var runtimeApiKeyInput by remember { mutableStateOf(viewModel.getRuntimeGeminiApiKey()) }
    var themeMode by remember(userProfile?.themeMode) { mutableStateOf(userProfile?.themeMode ?: "DARK") }
    var voiceEnabled by remember(userProfile?.voiceEnabled) { mutableStateOf(userProfile?.voiceEnabled ?: true) }
    var memoryEnabled by remember(userProfile?.memoryEnabled) { mutableStateOf(userProfile?.memoryEnabled ?: true) }
    var memorySummary by remember(userProfile?.memorySummary) { mutableStateOf(userProfile?.memorySummary ?: "") }

    // Founder Admin Config state
    var introTitle by remember(founderConfig.title) { mutableStateOf(founderConfig.title) }
    var introDescription by remember(founderConfig.description) { mutableStateOf(founderConfig.description) }
    var introVoice by remember(founderConfig.voiceName) { mutableStateOf(founderConfig.voiceName) }
    var announcement by remember(founderConfig.announcement) { mutableStateOf(founderConfig.announcement) }
    var showLogoDialog by remember { mutableStateOf(false) }

    val platformFeatures by viewModel.platformFeatures.collectAsStateWithLifecycle()
    val appRelease by viewModel.appReleaseState.collectAsStateWithLifecycle()
    val platformAnalytics by viewModel.platformAnalytics.collectAsStateWithLifecycle()
    val qualityChecks by viewModel.qualityChecks.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    if (showLogoDialog) {
        KalleshBrandLogoShowcaseDialog(onDismiss = { showLogoDialog = false })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_and_admin_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Account, Memory & Platform Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Signed in as ${userProfile?.email?.ifBlank { "Authenticated User" } ?: "Authenticated User"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick Actions: Founder Intro Replay & Official Brand Logo Kit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openFounderIntroModal(founderConfig) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_replay_intro_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Founder Intro")
                }

                OutlinedButton(
                    onClick = { showLogoDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_logo_kit_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Brand Logo Kit")
                }
            }
        }

        // Personalization & AI Memory Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("AI Personalization & Persistent Memory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Preferred Language", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("English", "Kannada", "Hindi", "Tamil", "Telugu", "Spanish").forEach { lang ->
                            FilterChip(
                                selected = preferredLanguage == lang,
                                onClick = { preferredLanguage = lang },
                                label = { Text(lang) }
                            )
                        }
                    }

                    Text("AI Response Tone", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Professional", "Friendly", "Detailed", "Short", "Teacher Mode").forEach { style ->
                            FilterChip(
                                selected = responseStyle == style,
                                onClick = { responseStyle = style },
                                label = { Text(style) }
                            )
                        }
                    }

                    Text("Theme Appearance", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { themeMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable AI Memory Across Chats", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = memoryEnabled, onCheckedChange = { memoryEnabled = it })
                    }

                    OutlinedTextField(
                        value = memorySummary,
                        onValueChange = { memorySummary = it },
                        label = { Text("Your Goals, Tech Stack & Interests (AI Memory)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            viewModel.saveUserPersonalization(
                                displayName = displayName,
                                preferredLanguage = preferredLanguage,
                                responseStyle = responseStyle,
                                defaultModel = defaultModel,
                                themeMode = themeMode,
                                voiceEnabled = voiceEnabled,
                                memoryEnabled = memoryEnabled,
                                memorySummary = memorySummary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_personalization_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Personalization to Cloud")
                    }
                }
            }
        }

        // Administrator & Founder Control Panel
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Founder & Administrator Control Panel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    val keyConfigured = viewModel.isGeminiKeyConfigured()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = if (keyConfigured) "GEMINI_API_KEY: Active & Ready" else "GEMINI_API_KEY: Smart Studio Mode (Add Key for Live Cloud API)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Add GEMINI_API_KEY in the AI Studio Secrets panel (.env) or enter a key below to connect directly to live Gemini 3, Veo 3 & Lyria 3 REST endpoints.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = runtimeApiKeyInput,
                                onValueChange = { runtimeApiKeyInput = it },
                                label = { Text("Optional Runtime Gemini API Key Override") },
                                placeholder = { Text("Paste AIzaSy... key here") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("runtime_gemini_api_key_input"),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.saveRuntimeGeminiApiKey(runtimeApiKeyInput) },
                                    enabled = runtimeApiKeyInput.isNotBlank(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_runtime_api_key_button")
                                ) {
                                    Text("Apply API Key")
                                }
                                if (runtimeApiKeyInput.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            runtimeApiKeyInput = ""
                                            viewModel.clearRuntimeGeminiApiKey()
                                        },
                                        modifier = Modifier.testTag("clear_runtime_api_key_button")
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = introTitle,
                        onValueChange = { introTitle = it },
                        label = { Text("Founder Welcome Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Founder TTS Voice (Gemini TTS)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Puck", "Charon", "Fenrir", "Kore", "Aoede").forEach { v ->
                            FilterChip(
                                selected = introVoice == v,
                                onClick = { introVoice = v },
                                label = { Text(v) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = introDescription,
                        onValueChange = { introDescription = it },
                        label = { Text("Founder Introduction Script") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    OutlinedTextField(
                        value = announcement,
                        onValueChange = { announcement = it },
                        label = { Text("Global Platform Announcement Banner") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveAdminFounderConfig(
                                founderConfig.copy(
                                    title = introTitle,
                                    description = introDescription,
                                    voiceName = introVoice,
                                    announcement = announcement
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_admin_config_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Update Founder Intro & Platform Config")
                    }
                }
            }
        }

        // Dynamic Feature System, Semantic Versioning, Rollback, Quality Gate & Analytics Section
        item {
            FounderPlatformAdminControlSection(
                viewModel = viewModel,
                features = platformFeatures,
                appRelease = appRelease,
                analytics = platformAnalytics,
                qualityChecks = qualityChecks,
                auditLogs = auditLogs,
                onTriggerUpdateDialogPreview = { viewModel.triggerUpdateDialogPreview() },
                onTriggerWhatsNewDialog = { viewModel.setShowWhatsNewModal(true) }
            )
        }

        // Sign Out Button
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Kallesh AI Hub")
            }
        }
    }
}
