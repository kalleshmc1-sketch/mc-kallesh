package com.example.ui.platform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SecurityAuditLogEntity
import com.example.platform.AiRouterTaskType
import com.example.platform.AiToolCategory
import com.example.platform.AppVersionRelease
import com.example.platform.FeatureBadge
import com.example.platform.FeatureStatus
import com.example.platform.PlatformAnalyticsSnapshot
import com.example.platform.PlatformFeature
import com.example.platform.QualityCheckItem
import com.example.platform.SemanticVersion
import com.example.ui.viewmodel.KalleshHubViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FounderPlatformAdminControlSection(
    viewModel: KalleshHubViewModel,
    features: List<PlatformFeature>,
    appRelease: AppVersionRelease,
    analytics: PlatformAnalyticsSnapshot,
    qualityChecks: List<QualityCheckItem>,
    auditLogs: List<SecurityAuditLogEntity>,
    onTriggerUpdateDialogPreview: () -> Unit,
    onTriggerWhatsNewDialog: () -> Unit
) {
    var activeAdminSubTab by remember { mutableIntStateOf(0) }
    val adminTabs = listOf(
        "Feature Registry & Flags",
        "Create / Edit AI Tool",
        "App Version & Updates",
        "Quality Gate & Analytics"
    )

    // State for Create / Edit Feature form
    var editingFeatureId by remember { mutableStateOf("feat_custom_assistant") }
    var featName by remember { mutableStateOf("AI Startup Advisor & Pitch Strategist") }
    var featDesc by remember { mutableStateOf("Analyze startup ideas, unit economics, GTM strategy, and investor pitch readiness.") }
    var featHowToUse by remember { mutableStateOf("Describe your startup concept, target market, and pricing model to generate a complete roadmap.") }
    var featCategory by remember { mutableStateOf(AiToolCategory.AI_PRODUCTIVITY) }
    var featTaskType by remember { mutableStateOf(AiRouterTaskType.DATA_PRESENTATION) }
    var featVersion by remember { mutableStateOf("1.0.0") }
    var featMinAppVersion by remember { mutableStateOf("1.0.0") }
    var featStatus by remember { mutableStateOf(FeatureStatus.ACTIVE) }
    var featBadge by remember { mutableStateOf(FeatureBadge.NEW) }
    var featRollout by remember { mutableIntStateOf(100) }
    var featEndpoint by remember { mutableStateOf("/v1beta/models/gemini-3.1-pro-preview:generateContent") }
    var featSystemPrompt by remember { mutableStateOf("You are an elite Venture Studio Advisor inside Kallesh AI Hub. Provide actionable GTM, moat, and financial analysis.") }
    var featChangelog by remember { mutableStateOf("Initial feature release via Dynamic Feature Management System.") }
    var featFreeAllowed by remember { mutableStateOf(true) }
    var featDailyFree by remember { mutableStateOf("15") }
    var featDailyPro by remember { mutableStateOf("100") }

    // State for App Version Release Publisher
    var nextVersionStr by remember(appRelease.latestAvailableVersion) {
        mutableStateOf(appRelease.latestAvailableVersion)
    }
    var minSupportedStr by remember(appRelease.minimumSupportedVersion) {
        mutableStateOf(appRelease.minimumSupportedVersion)
    }
    var isMandatoryRelease by remember(appRelease.isMandatory) {
        mutableStateOf(appRelease.isMandatory)
    }
    var releaseTitleStr by remember(appRelease.releaseTitle) {
        mutableStateOf(appRelease.releaseTitle)
    }
    var releaseNewItemsText by remember(appRelease.newFeatures) {
        mutableStateOf(appRelease.newFeatures.joinToString("\n"))
    }
    var releaseBugFixesText by remember(appRelease.bugFixes) {
        mutableStateOf(appRelease.bugFixes.joinToString("\n"))
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("founder_platform_admin_section"),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Founder Platform Evolution & Release Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "Create AI tools live, manage Feature Flags, Semantic Versions, Rollbacks & Analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = Color(0xFF00E676).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "v${appRelease.installedVersion} LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Admin Release Workflow Pipeline Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Workflow: Create → Configure → Connect Router → Quality Gate → Set SemVer → Beta (10%→50%) → Full Release (100%) → Notify Users",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Sub-navigation chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                adminTabs.forEachIndexed { idx, title ->
                    FilterChip(
                        selected = activeAdminSubTab == idx,
                        onClick = { activeAdminSubTab = idx },
                        label = { Text(title) },
                        modifier = Modifier.testTag("admin_subtab_$idx")
                    )
                }
            }

            HorizontalDivider()

            when (activeAdminSubTab) {
                // TAB 0: Dynamic Feature Registry, Feature Flags, Rollouts, Maintenance & Rollback
                0 -> {
                    Text(
                        text = "Active Feature Registry (${features.size} AI Modules)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Control global status, staged percentage rollouts (10% → 25% → 50% → 100%), maintenance mode, or instant version rollback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    features.take(12).forEach { feat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${feat.name} (v${feat.version})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${feat.category.displayName} • Status: ${feat.status.label} • Rollout: ${feat.rolloutPercentage}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    AssistChip(
                                        onClick = {
                                            editingFeatureId = feat.featureId
                                            featName = feat.name
                                            featDesc = feat.description
                                            featHowToUse = feat.howToUse
                                            featCategory = feat.category
                                            featTaskType = feat.taskType
                                            featVersion = feat.version
                                            featMinAppVersion = feat.minSupportedAppVersion
                                            featStatus = feat.status
                                            featBadge = feat.badge
                                            featRollout = feat.rolloutPercentage
                                            featEndpoint = feat.backendEndpoint
                                            featSystemPrompt = feat.systemInstruction
                                            featChangelog = feat.changelog
                                            activeAdminSubTab = 1
                                        },
                                        label = { Text("Edit") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = feat.status == FeatureStatus.ACTIVE,
                                        onClick = {
                                            val next = if (feat.status == FeatureStatus.ACTIVE) FeatureStatus.DISABLED else FeatureStatus.ACTIVE
                                            viewModel.updatePlatformFeatureStatus(feat.featureId, next)
                                        },
                                        label = { Text(if (feat.status == FeatureStatus.ACTIVE) "Enabled" else "Disabled") }
                                    )
                                    FilterChip(
                                        selected = feat.status == FeatureStatus.MAINTENANCE,
                                        onClick = {
                                            val next = if (feat.status == FeatureStatus.MAINTENANCE) FeatureStatus.ACTIVE else FeatureStatus.MAINTENANCE
                                            viewModel.updatePlatformFeatureStatus(feat.featureId, next)
                                        },
                                        label = { Text("Maintenance") }
                                    )
                                    listOf(10, 25, 50, 100).forEach { pct ->
                                        FilterChip(
                                            selected = feat.rolloutPercentage == pct,
                                            onClick = { viewModel.updateFeatureRolloutPercentage(feat.featureId, pct) },
                                            label = { Text("$pct%") }
                                        )
                                    }
                                    AssistChip(
                                        onClick = { viewModel.rollbackPlatformFeature(feat.featureId) },
                                        label = { Text("Rollback → v${feat.previousVersion}") },
                                        leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: Create / Edit Dynamic Feature Module
                1 -> {
                    Text(
                        text = "Register New AI Feature or Update Existing Module",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = editingFeatureId,
                        onValueChange = { editingFeatureId = it.lowercase().replace(" ", "_") },
                        label = { Text("Unique Feature ID (e.g. feat_startup_advisor)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_feature_id_input")
                    )
                    OutlinedTextField(
                        value = featName,
                        onValueChange = { featName = it },
                        label = { Text("Feature Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_feature_name_input")
                    )
                    OutlinedTextField(
                        value = featDesc,
                        onValueChange = { featDesc = it },
                        label = { Text("Feature Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = featHowToUse,
                        onValueChange = { featHowToUse = it },
                        label = { Text("How to Use Instructions") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category (28 AI Tool Hub Categories):", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiToolCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = featCategory == cat,
                                onClick = { featCategory = cat },
                                label = { Text(cat.displayName) }
                            )
                        }
                    }

                    Text("Backend AI Model Router Pipeline:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiRouterTaskType.entries.forEach { task ->
                            FilterChip(
                                selected = featTaskType == task,
                                onClick = {
                                    featTaskType = task
                                    featEndpoint = task.defaultEndpoint
                                },
                                label = { Text("${task.name} (${task.primaryModelId})") }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = featVersion,
                            onValueChange = { featVersion = it },
                            label = { Text("SemVer (MAJOR.MINOR.PATCH)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = featMinAppVersion,
                            onValueChange = { featMinAppVersion = it },
                            label = { Text("Min App Version") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Release Badge & Status:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeatureBadge.entries.forEach { b ->
                            FilterChip(
                                selected = featBadge == b,
                                onClick = { featBadge = b },
                                label = { Text(b.label.ifBlank { "Standard" }) }
                            )
                        }
                        FeatureStatus.entries.forEach { st ->
                            FilterChip(
                                selected = featStatus == st,
                                onClick = { featStatus = st },
                                label = { Text(st.label) }
                            )
                        }
                    }

                    Text("Percentage Rollout Stage:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 25, 50, 100).forEach { pct ->
                            FilterChip(
                                selected = featRollout == pct,
                                onClick = { featRollout = pct },
                                label = { Text("Rollout $pct%") }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = featDailyFree,
                            onValueChange = { featDailyFree = it },
                            label = { Text("Free Daily Limit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = featDailyPro,
                            onValueChange = { featDailyPro = it },
                            label = { Text("Pro Daily Limit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = featSystemPrompt,
                        onValueChange = { featSystemPrompt = it },
                        label = { Text("AI Router System Prompt Instruction") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = featChangelog,
                        onValueChange = { featChangelog = it },
                        label = { Text("Release Notes / Changelog") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Allow Free Plan Users", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = featFreeAllowed, onCheckedChange = { featFreeAllowed = it })
                    }

                    Button(
                        onClick = {
                            val allowedPlans = if (featFreeAllowed) {
                                listOf("FREE", "PRO", "PREMIUM")
                            } else {
                                listOf("PRO", "PREMIUM")
                            }
                            val newFeature = PlatformFeature(
                                featureId = editingFeatureId.ifBlank { "feat_${System.currentTimeMillis()}" },
                                name = featName.ifBlank { "Custom AI Tool" },
                                description = featDesc,
                                howToUse = featHowToUse,
                                iconKey = "auto_awesome",
                                category = featCategory,
                                version = featVersion.ifBlank { "1.0.0" },
                                previousVersion = "1.0.0",
                                status = featStatus,
                                badge = featBadge,
                                minSupportedAppVersion = featMinAppVersion.ifBlank { "1.0.0" },
                                allowedPlans = allowedPlans,
                                dailyLimitFree = featDailyFree.toIntOrNull() ?: 10,
                                dailyLimitPro = featDailyPro.toIntOrNull() ?: 100,
                                dailyLimitPremium = 500,
                                backendEndpoint = featEndpoint,
                                frontendRoute = "hub://tool/$editingFeatureId",
                                taskType = featTaskType,
                                systemInstruction = featSystemPrompt,
                                changelog = featChangelog,
                                rolloutPercentage = featRollout
                            )
                            viewModel.saveAndPublishDynamicFeature(newFeature, notifyUsers = true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_publish_feature_button")
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish Feature to AI Tool Hub & Notify Users")
                    }
                }

                // TAB 2: App Version & Semantic Versioning Release Manager
                2 -> {
                    Text(
                        text = "Semantic Version Management (MAJOR.MINOR.PATCH)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Installed: v${appRelease.installedVersion} • Latest Published: v${appRelease.latestAvailableVersion} • Min Supported: v${appRelease.minimumSupportedVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val parsed = SemanticVersion.parse(nextVersionStr)
                        OutlinedButton(
                            onClick = { nextVersionStr = "${parsed.major}.${parsed.minor}.${parsed.patch + 1}" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+PATCH (${parsed.major}.${parsed.minor}.${parsed.patch + 1})")
                        }
                        OutlinedButton(
                            onClick = { nextVersionStr = "${parsed.major}.${parsed.minor + 1}.0" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+MINOR (${parsed.major}.${parsed.minor + 1}.0)")
                        }
                        OutlinedButton(
                            onClick = { nextVersionStr = "${parsed.major + 1}.0.0" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+MAJOR (${parsed.major + 1}.0.0)")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nextVersionStr,
                            onValueChange = { nextVersionStr = it },
                            label = { Text("Latest Available Version") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minSupportedStr,
                            onValueChange = { minSupportedStr = it },
                            label = { Text("Min Supported Version") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = releaseTitleStr,
                        onValueChange = { releaseTitleStr = it },
                        label = { Text("Update Notification Headline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = releaseNewItemsText,
                        onValueChange = { releaseNewItemsText = it },
                        label = { Text("What's New (one bullet per line)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = releaseBugFixesText,
                        onValueChange = { releaseBugFixesText = it },
                        label = { Text("Bug Fixes & Security Updates (one per line)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mandatory Update (Require update before continuing)", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = isMandatoryRelease, onCheckedChange = { isMandatoryRelease = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTriggerUpdateDialogPreview,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Preview Update Modal")
                        }
                        OutlinedButton(
                            onClick = onTriggerWhatsNewDialog,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Changelog")
                        }
                    }

                    Button(
                        onClick = {
                            val updatedRelease = appRelease.copy(
                                latestAvailableVersion = nextVersionStr.trim(),
                                minimumSupportedVersion = minSupportedStr.trim(),
                                isMandatory = isMandatoryRelease,
                                releaseTitle = releaseTitleStr.trim().ifBlank { "Kallesh AI Hub Update Available" },
                                newFeatures = releaseNewItemsText.lines().map { it.trim() }.filter { it.isNotBlank() },
                                bugFixes = releaseBugFixesText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            )
                            viewModel.publishNewAppVersionRelease(updatedRelease)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_publish_app_version_button")
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Release v$nextVersionStr & Send Update Notification")
                    }
                }

                // TAB 3: 11-Point Quality Control Gate, Platform Analytics & Security Audit Logs
                3 -> {
                    Text(
                        text = "Platform Monitoring & Telemetry Dashboard",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadgeCard("Active / DAU / MAU", "${analytics.activeUsers} / ${analytics.dailyActiveUsers} / ${analytics.monthlyActiveUsers}")
                        MetricBadgeCard("API Requests", "${analytics.totalApiRequests} (${analytics.apiErrors} err)")
                        MetricBadgeCard("Avg Latency", "${analytics.avgResponseTimeMs} ms")
                        MetricBadgeCard("Fallback Router", "${analytics.fallbackActivations} switches")
                        MetricBadgeCard("Update Adoption", "${analytics.updateAdoptionRatePercent}% on latest")
                        MetricBadgeCard("DB Migration", "Room v${analytics.databaseSchemaVersion} (${analytics.preservedRecordsCount} saved)")
                        MetricBadgeCard("Tokens / Storage", "${analytics.estimatedTokensUsed} tok • ${analytics.storageUsageKb} KB")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "11-Point Pre-Release Quality Control Gate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        AssistChip(
                            onClick = { viewModel.runQualityControlValidation() },
                            label = { Text("Re-Run Checks") },
                            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                    }

                    qualityChecks.forEach { check ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (check.passed) Color(0xFF00E676) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(check.checkName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(check.details, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Security & Admin Audit Logs (${auditLogs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (auditLogs.isEmpty()) {
                        Text(
                            text = "All admin feature changes, version updates, and AI router fallback events are logged here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        auditLogs.take(8).forEach { log ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "[${log.action}] by ${log.actor} → ${log.targetId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF)
                                    )
                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun MetricBadgeCard(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
