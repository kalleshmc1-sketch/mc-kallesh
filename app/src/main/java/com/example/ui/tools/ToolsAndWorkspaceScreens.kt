package com.example.ui.tools

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CachedPromptEntity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UiState
import com.example.data.model.UsageCategory
import com.example.data.model.WorkspaceItem
import com.example.ui.home.RichMarkdownAndCodeContent
import com.example.ui.home.copyToClipboard
import com.example.ui.platform.DynamicAiToolHubPane
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel
import com.example.ui.viewmodel.ProductivityToolTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityToolsScreen(
    viewModel: KalleshHubViewModel,
    selectedTab: ProductivityToolTab,
    isGenerating: Boolean,
    studioResultText: String
) {
    val platformFeatures by viewModel.platformFeatures.collectAsStateWithLifecycle()
    val activeDynamicFeature by viewModel.activeDynamicFeature.collectAsStateWithLifecycle()
    val lastRouterExecution by viewModel.lastRouterExecution.collectAsStateWithLifecycle()
    val appRelease by viewModel.appReleaseState.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val userPlan = (userProfileState as? UiState.Success)?.data?.subscriptionPlan ?: SubscriptionPlan.FREE

    BackHandler {
        if (selectedTab == ProductivityToolTab.HUB && activeDynamicFeature != null) {
            viewModel.selectActiveDynamicFeature(null)
        } else {
            viewModel.navigateToSection(HubSection.HOME)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("productivity_tools_screen")
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 12.dp
        ) {
            ProductivityToolTab.entries.forEach { tab ->
                val icon = when (tab) {
                    ProductivityToolTab.HUB -> Icons.Default.AutoAwesome
                    ProductivityToolTab.DOCUMENT -> Icons.Default.Description
                    ProductivityToolTab.CODE -> Icons.Default.Code
                    ProductivityToolTab.STUDY -> Icons.Default.School
                    ProductivityToolTab.WRITING -> Icons.Default.EditNote
                    ProductivityToolTab.TRANSLATOR -> Icons.Default.Translate
                }
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.openProductivityTool(tab) },
                    text = { Text(tab.title) },
                    icon = { Icon(icon, contentDescription = tab.title) },
                    modifier = Modifier.testTag("tool_tab_${tab.name.lowercase()}")
                )
            }
        }

        when (selectedTab) {
            ProductivityToolTab.HUB -> DynamicAiToolHubPane(
                viewModel = viewModel,
                features = platformFeatures,
                activeFeature = activeDynamicFeature,
                userPlan = userPlan,
                installedAppVersion = appRelease.installedVersion,
                isGenerating = isGenerating,
                studioResultText = studioResultText,
                lastRouterExecution = lastRouterExecution,
                onSelectFeature = { viewModel.selectActiveDynamicFeature(it) },
                onShowWhatsNew = { viewModel.setShowWhatsNewModal(true) }
            )
            ProductivityToolTab.DOCUMENT -> DocumentAnalysisPane(viewModel, isGenerating, studioResultText)
            ProductivityToolTab.CODE -> KalleshCodeAiPane(viewModel, isGenerating, studioResultText)
            ProductivityToolTab.STUDY -> StudyAndExamAiPane(viewModel, isGenerating, studioResultText)
            ProductivityToolTab.WRITING -> AiWritingStudioPane(viewModel, isGenerating, studioResultText)
            ProductivityToolTab.TRANSLATOR -> MultiLanguageTranslatorPane(viewModel, isGenerating, studioResultText)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentAnalysisPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    resultText: String
) {
    val context = LocalContext.current
    var question by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf("Executive Summary & Key Takeaways") }
    var docMime by remember { mutableStateOf<String?>(null) }
    var docBase64 by remember { mutableStateOf<String?>(null) }
    var docName by remember { mutableStateOf("") }

    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                docMime = loaded.first
                docBase64 = loaded.second
                docName = loaded.third
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
                    Text("Document & File Analysis Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Upload a PDF, TXT, Markdown, JSON, CSV, or image document for instant summarization, extraction, and Q&A.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { docPicker.launch(arrayOf("application/pdf", "text/*", "image/*", "application/json")) },
                            modifier = Modifier.testTag("upload_document_button")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (docName.isBlank()) "Select Document / File" else docName)
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Executive Summary & Key Takeaways",
                            "Extract Action Items & Tables",
                            "Explain Complex Terms Simply",
                            "Generate 10 Quiz Questions"
                        ).forEach { mode ->
                            FilterChip(
                                selected = selectedTask == mode,
                                onClick = { selectedTask = mode },
                                label = { Text(mode) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Paste document text or ask a specific question about the file...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("document_question_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            val fullPrompt = "Task: $selectedTask.\nUser Question / Text:\n$question"
                            viewModel.runStudioPrompt(
                                workspaceType = "DOCUMENT",
                                categoryLabel = selectedTask,
                                title = if (docName.isNotBlank()) "Doc: $docName" else "Document Analysis",
                                prompt = fullPrompt,
                                useProModel = false,
                                usageCategory = UsageCategory.FILE,
                                inlineMimeType = docMime,
                                inlineBase64Data = docBase64
                            )
                        },
                        enabled = (question.isNotBlank() || !docBase64.isNullOrBlank()) && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("analyze_document_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Document...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze Document")
                        }
                    }
                }
            }
        }

        if (resultText.isNotBlank()) {
            item {
                StudioOutputCard(title = "Document Analysis Result", content = resultText)
            }
        }
    }
}

@Composable
private fun KalleshCodeAiPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    resultText: String
) {
    val languages = listOf(
        "Kotlin", "Python", "TypeScript", "JavaScript", "React", "Node.js",
        "Java", "C++", "C", "SQL", "HTML", "CSS", "Swift"
    )
    val actions = listOf(
        "Generate Production Code",
        "Debug & Fix Errors",
        "Explain Step-by-Step",
        "Optimize Performance",
        "Write Unit Tests",
        "Convert Language"
    )
    var selectedLang by remember { mutableStateOf("Kotlin") }
    var selectedAction by remember { mutableStateOf(actions.first()) }
    var codeInput by remember { mutableStateOf("") }

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
                    Text("Kallesh Code AI • Gemini 3.1 Pro Reasoning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Target Language", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = selectedLang == lang,
                                onClick = { selectedLang = lang },
                                label = { Text(lang) }
                            )
                        }
                    }

                    Text("Engineering Action", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        actions.forEach { act ->
                            FilterChip(
                                selected = selectedAction == act,
                                onClick = { selectedAction = act },
                                label = { Text(act) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Paste code snippet, bug stacktrace, or architecture spec...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_studio_input"),
                        minLines = 4,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            val prompt = "Language: $selectedLang\nMode: $selectedAction\n\nRequest / Code:\n$codeInput"
                            viewModel.runStudioPrompt(
                                workspaceType = "CODE",
                                categoryLabel = "$selectedLang • $selectedAction",
                                title = "$selectedLang: ${codeInput.take(40)}",
                                prompt = prompt,
                                useProModel = true
                            )
                        },
                        enabled = codeInput.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_code_ai_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Running Gemini 3.1 Pro Code Engine...")
                        } else {
                            Icon(Icons.Default.Code, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Kallesh Code AI")
                        }
                    }
                }
            }
        }

        if (resultText.isNotBlank()) {
            item {
                StudioOutputCard(title = "$selectedLang Code Output", content = resultText)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyAndExamAiPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    resultText: String
) {
    val studyModes = listOf(
        "Explain Simply with Analogies",
        "Structured Exam Revision Notes",
        "Interactive 5-Question Quiz & Key",
        "Active-Recall Flashcards",
        "Step-by-Step Math/Science Solver"
    )
    var selectedMode by remember { mutableStateOf(studyModes.first()) }
    var topic by remember { mutableStateOf("") }

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
                    Text("Study & Exam Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        studyModes.forEach { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Enter subject, theorem, syllabus topic, or homework question...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_topic_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.runStudioPrompt(
                                workspaceType = "STUDY",
                                categoryLabel = selectedMode,
                                title = "Study: ${topic.take(45)}",
                                prompt = "Study Mode: $selectedMode\nTopic / Problem:\n$topic",
                                useProModel = true
                            )
                        },
                        enabled = topic.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_study_guide_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (isGenerating) "Preparing Study Material..." else "Generate Study Material")
                    }
                }
            }
        }

        if (resultText.isNotBlank()) {
            item {
                StudioOutputCard(title = selectedMode, content = resultText)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiWritingStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    resultText: String
) {
    val formats = listOf("Executive Email", "Blog Article", "ATS Resume Bullet Points", "Cover Letter", "YouTube / Reel Script", "Academic Essay")
    var selectedFormat by remember { mutableStateOf(formats.first()) }
    var brief by remember { mutableStateOf("") }

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
                    Text("AI Writing & Content Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        formats.forEach { fmt ->
                            FilterChip(
                                selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt },
                                label = { Text(fmt) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = brief,
                        onValueChange = { brief = it },
                        label = { Text("Describe key points, audience, and goal...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("writing_brief_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.runStudioPrompt(
                                workspaceType = "WRITING",
                                categoryLabel = selectedFormat,
                                title = "$selectedFormat: ${brief.take(40)}",
                                prompt = "Write a polished $selectedFormat based on this brief:\n$brief"
                            )
                        },
                        enabled = brief.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_writing_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (isGenerating) "Drafting Content..." else "Draft $selectedFormat")
                    }
                }
            }
        }

        if (resultText.isNotBlank()) {
            item {
                StudioOutputCard(title = selectedFormat, content = resultText)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiLanguageTranslatorPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    resultText: String
) {
    val languages = listOf("Kannada", "Hindi", "English", "Tamil", "Telugu", "Spanish", "French", "German", "Japanese", "Arabic")
    var targetLanguage by remember { mutableStateOf("Kannada") }
    var sourceText by remember { mutableStateOf("") }

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
                    Text("Multi-Language AI Translator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Translate Into:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = targetLanguage == lang,
                                onClick = { targetLanguage = lang },
                                label = { Text(lang) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        label = { Text("Enter text to translate into $targetLanguage...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translator_source_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.runStudioPrompt(
                                workspaceType = "WRITING",
                                categoryLabel = "Translation ($targetLanguage)",
                                title = "Translate to $targetLanguage",
                                prompt = "Translate the following text accurately into $targetLanguage. Include natural script, phonetic pronunciation guide, and cultural/contextual notes:\n\n$sourceText"
                            )
                        },
                        enabled = sourceText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("translate_text_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (isGenerating) "Translating..." else "Translate to $targetLanguage")
                    }
                }
            }
        }

        if (resultText.isNotBlank()) {
            item {
                StudioOutputCard(title = "$targetLanguage Translation", content = resultText)
            }
        }
    }
}

@Composable
private fun StudioOutputCard(
    title: String,
    content: String
) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { copyToClipboard(context, content) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Output")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            RichMarkdownAndCodeContent(
                rawContent = content,
                onCopyCode = { copyToClipboard(context, it) }
            )
        }
    }
}

@Composable
fun PersonalWorkspaceScreen(
    viewModel: KalleshHubViewModel,
    workspaceItemsState: UiState<List<WorkspaceItem>>,
    prompts: List<CachedPromptEntity>
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val context = LocalContext.current
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var showCreatePromptDialog by remember { mutableStateOf(false) }
    var newPromptTitle by remember { mutableStateOf("") }
    var newPromptCategory by remember { mutableStateOf("Coding") }
    var newPromptBody by remember { mutableStateOf("") }

    if (showCreatePromptDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePromptDialog = false },
            title = { Text("Save Custom AI Prompt") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPromptTitle,
                        onValueChange = { newPromptTitle = it },
                        label = { Text("Prompt Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPromptCategory,
                        onValueChange = { newPromptCategory = it },
                        label = { Text("Category (Coding, Study, Images, Business)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPromptBody,
                        onValueChange = { newPromptBody = it },
                        label = { Text("Prompt Template Text") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomPrompt(newPromptTitle, newPromptCategory, newPromptBody)
                        newPromptTitle = ""
                        newPromptBody = ""
                        showCreatePromptDialog = false
                    }
                ) {
                    Text("Save Prompt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePromptDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val allArtifacts = (workspaceItemsState as? UiState.Success)?.data.orEmpty()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val savedConversations = (conversationsState as? UiState.Success)?.data.orEmpty()
    val filteredArtifacts = remember(allArtifacts, selectedTypeFilter) {
        if (selectedTypeFilter == "ALL") allArtifacts
        else allArtifacts.filter { it.type.equals(selectedTypeFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("personal_workspace_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Personal AI Workspace", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Saved prompts, Room chat history, images, videos, tracks, code, and documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreatePromptDialog = true },
                    modifier = Modifier.testTag("add_custom_prompt_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Prompt")
                }
            }
        }

        // Persisted Chat Conversations (Local Room Database)
        if (savedConversations.isNotEmpty()) {
            item {
                Text(
                    text = "PERSISTED CHAT CONVERSATIONS (${savedConversations.size})",
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
                    savedConversations.forEach { conv ->
                        Card(
                            onClick = { viewModel.openConversation(conv.id) },
                            modifier = Modifier
                                .width(270.dp)
                                .testTag("workspace_conversation_card_${conv.id}"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = { viewModel.openConversation(conv.id) },
                                        label = { Text("${conv.folder} • ${conv.messageCount} msgs") }
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteChat(conv.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Conversation", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = conv.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = conv.lastMessagePreview.ifBlank { "Tap to view messages" },
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

        // Prompt Library Section
        item {
            Text(
                text = "PROMPT LIBRARY (${prompts.size})",
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
                prompts.forEach { p ->
                    Card(
                        onClick = {
                            viewModel.startNewChat()
                            viewModel.sendChatMessage(p.promptText)
                        },
                        modifier = Modifier.width(260.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(onClick = {}, label = { Text(p.category) })
                                if (!p.isBuiltIn) {
                                    IconButton(onClick = { viewModel.deleteCustomPrompt(p.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Prompt", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Text(p.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                p.promptText,
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

        // Filter Chips for Cloud Workspace Artifacts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "IMAGE", "VIDEO", "MUSIC", "CODE", "DOCUMENT", "STUDY", "WRITING").forEach { filter ->
                    FilterChip(
                        selected = selectedTypeFilter == filter,
                        onClick = { selectedTypeFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        if (filteredArtifacts.isEmpty()) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No saved artifacts in this category yet.", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Generate images, videos, music, code, or study guides to populate your cloud workspace.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredArtifacts, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.type} • ${item.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                IconButton(onClick = { copyToClipboard(context, item.content.ifBlank { item.prompt }) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(onClick = { viewModel.deleteWorkspaceArtifact(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                        if (item.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.content,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
