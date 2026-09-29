package com.example.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.platform.AppVersionRelease
import com.example.platform.ChangelogEntry
import com.example.platform.FeatureBadge
import com.example.platform.PlatformFeature

@Composable
fun AppUpdateAvailableDialog(
    release: AppVersionRelease,
    onUpdateNow: () -> Unit,
    onUpdateLater: () -> Unit
) {
    val isMandatory = release.isForcedUpdateRequired

    Dialog(
        onDismissRequest = {
            if (!isMandatory) onUpdateLater()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory,
            dismissOnClickOutside = !isMandatory,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("app_update_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "App Update Available",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (isMandatory) MaterialTheme.colorScheme.errorContainer else Color(0xFF00E5FF).copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isMandatory) "MANDATORY UPDATE" else "UPDATE AVAILABLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMandatory) MaterialTheme.colorScheme.error else Color(0xFF00E5FF),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "Released ${release.releaseDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = release.releaseTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Version ${release.latestAvailableVersion} is now available",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Installed: v${release.installedVersion} • Min Supported: v${release.minimumSupportedVersion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "v${release.latestAvailableVersion}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (release.newFeatures.isNotEmpty()) {
                    Text(
                        text = "What’s new:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    release.newFeatures.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (release.improvedItems.isNotEmpty()) {
                    Text(
                        text = "Improvements:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    release.improvedItems.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (release.bugFixes.isNotEmpty()) {
                    Text(
                        text = "Bug fixes & security:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    (release.bugFixes + release.securityUpdates).take(4).forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Data Preservation Guarantee
                Surface(
                    color = Color(0xFF00C853).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Data Preserved",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Zero Data Loss Update: Your account, chats, workspace files, prompts, AI memory & plan are 100% preserved.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isMandatory) {
                        OutlinedButton(
                            onClick = onUpdateLater,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("update_later_button")
                        ) {
                            Text("Later")
                        }
                    }
                    Button(
                        onClick = onUpdateNow,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("update_now_button")
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Now")
                    }
                }
            }
        }
    }
}

@Composable
fun NewFeatureDiscoveryDialog(
    feature: PlatformFeature,
    onTryNow: (PlatformFeature) -> Unit,
    onMaybeLater: (PlatformFeature) -> Unit
) {
    Dialog(
        onDismissRequest = { onMaybeLater(feature) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFD500F9))),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("new_feature_discovery_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NewReleases,
                                contentDescription = "New Feature",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "NEW FEATURE • v${feature.version}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = feature.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "How to use it:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = feature.howToUse,
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "AI Router Pipeline: ${feature.taskType.label} (${feature.taskType.primaryModelId})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onMaybeLater(feature) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_feature_maybe_later_button")
                    ) {
                        Text("Maybe Later")
                    }
                    Button(
                        onClick = { onTryNow(feature) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_feature_try_now_button")
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Try Now")
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsNewChangelogDialog(
    installedVersion: String,
    changelogs: List<ChangelogEntry>,
    highlightedFeatures: List<PlatformFeature>,
    onTryFeature: (PlatformFeature) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 660.dp)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("whats_new_changelog_dialog"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "What's New",
                            tint = Color(0xFF00E5FF)
                        )
                        Column {
                            Text(
                                text = "What’s New & Changelog",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Kallesh AI Hub • Installed Version v$installedVersion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                // Recently Released Features section
                if (highlightedFeatures.isNotEmpty()) {
                    Text(
                        text = "Recently Released AI Capabilities",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    highlightedFeatures.take(6).forEach { feat ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = feat.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (feat.badge != FeatureBadge.NONE) {
                                            Surface(
                                                color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = feat.badge.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF00E5FF),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = feat.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onTryFeature(feat)
                                    }
                                ) {
                                    Text("Try Now")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Versioned Changelogs (MAJOR.MINOR.PATCH)
                changelogs.forEach { entry ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kallesh AI Hub v${entry.version}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = entry.releaseDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = entry.headline,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (entry.newItems.isNotEmpty()) {
                                ChangelogGroupSection(label = "NEW", color = Color(0xFF00E5FF), items = entry.newItems)
                            }
                            if (entry.improvedItems.isNotEmpty()) {
                                ChangelogGroupSection(label = "IMPROVED", color = Color(0xFF7C4DFF), items = entry.improvedItems)
                            }
                            if (entry.fixedItems.isNotEmpty()) {
                                ChangelogGroupSection(label = "FIXED", color = Color(0xFF00E676), items = entry.fixedItems)
                            }
                            if (entry.securityItems.isNotEmpty()) {
                                ChangelogGroupSection(label = "SECURITY", color = Color(0xFFFFAB00), items = entry.securityItems)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogGroupSection(
    label: String,
    color: Color,
    items: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Surface(
            color = color.copy(alpha = 0.16f),
            shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DailyLimitReachedUpgradeDialog(
    alert: com.example.data.model.LimitReachedAlertState,
    onUpgradeToPlan: (com.example.data.model.SubscriptionPlan) -> Unit,
    onOpenUpgradeScreen: () -> Unit,
    onResetDailyQuota: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentPlan = alert.currentPlan
    val category = alert.category
    val dummyUsage = com.example.data.model.DailyUsage()
    val upgradePlans = com.example.data.model.SubscriptionPlan.entries.filter { it.ordinal > currentPlan.ordinal }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFFFFAB00), Color(0xFF00E5FF), Color(0xFF7C4DFF))),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("daily_limit_reached_upgrade_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFFFAB00), Color(0xFFFF3D00)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Daily Limit Reached",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "DAILY QUOTA REACHED • ${currentPlan.badgeText} PLAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upgrade to Keep Creating Today",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${category.label} (${alert.featureName})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${alert.usedCount} / ${alert.dailyLimit} used today",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (upgradePlans.isNotEmpty()) {
                    Text(
                        text = "Select an Instant Upgrade Plan:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )

                    upgradePlans.forEach { targetPlan ->
                        val newCategoryLimit = dummyUsage.limitFor(category, targetPlan)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = targetPlan.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = targetPlan.priceLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${alert.dailyLimit}/day → $newCategoryLimit/day",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF00E5FF),
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "• ${targetPlan.dailyChatLimit} AI Chat Messages/day • ${targetPlan.dailyImageLimit} Images/day • ${targetPlan.dailyVideoLimit} Veo 3 Videos/day",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• ${targetPlan.dailyFileLimit} File Analyses/day (${targetPlan.maxFileSizeMb}MB) • ${targetPlan.dailyVoiceLimit} Voice turns/day • ${targetPlan.dailyMusicLimit} Music tracks/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = { onUpgradeToPlan(targetPlan) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("modal_upgrade_to_${targetPlan.id.lowercase()}_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upgrade to ${targetPlan.displayName} Now")
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "You are currently on our highest tier (${currentPlan.displayName}). You can reset today's daily quota below to continue immediately.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetDailyQuota,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("modal_reset_daily_quota_button")
                    ) {
                        Text("Reset Today's Quota")
                    }
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenUpgradeScreen()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("modal_compare_all_plans_button")
                    ) {
                        Text("Compare Plans")
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("modal_dismiss_limit_dialog_button")
                ) {
                    Text("Not Now")
                }
            }
        }
    }
}
