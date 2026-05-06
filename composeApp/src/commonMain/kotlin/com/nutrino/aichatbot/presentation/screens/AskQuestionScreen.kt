package com.nutrino.aichatbot.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrino.aichatbot.domain.model.askQuestion.AskQuestionRequest
import com.nutrino.aichatbot.domain.model.askQuestion.Content
import com.nutrino.aichatbot.domain.model.askQuestion.Part
import com.nutrino.aichatbot.isDesktop
import com.nutrino.aichatbot.presentation.states.AskQuestionsUIState
import com.nutrino.aichatbot.presentation.theme.AI_Gradient_End
import com.nutrino.aichatbot.presentation.theme.AI_Gradient_Start
import com.nutrino.aichatbot.presentation.viewmodel.GeminiViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * REDESIGNED: Futuristic AI Chat Interface
 *
 * This version replaces the traditional Scaffold with a layered, glassmorphic design.
 * It uses custom animations for message entry and a floating input controller.
 */
@Composable
fun AskQuestionScreen(
    viewModel: GeminiViewModel = koinViewModel()
) {
    val uiState by viewModel.askQuestionUIState.collectAsStateWithLifecycle()
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    var nextMessageId by remember { mutableStateOf(0L) }
    var prompt by remember { mutableStateOf("") }

    val isLoading = uiState is AskQuestionsUIState.isLoading

    // Sync business logic states
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AskQuestionsUIState.Success -> {
                val answerText =
                    state.data.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?.takeIf { it.isNotBlank() } ?: "..."

                if (chatMessages.lastOrNull()?.text != answerText) {
                    chatMessages.add(ChatMessage(nextMessageId++, ChatRole.ASSISTANT, answerText))
                }
            }

            is AskQuestionsUIState.Error -> {
                chatMessages.add(ChatMessage(nextMessageId++, ChatRole.ERROR, state.message))
            }

            else -> Unit
        }
    }

    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

















    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AI_Gradient_Start.copy(alpha = 0.18f),
                            AI_Gradient_End.copy(alpha = 0.18f),
                        )
                    )
                )
                .blur(100.dp)
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(AI_Gradient_End.copy(alpha = 0.1f), CircleShape)
                .blur(100.dp)
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .background(AI_Gradient_Start.copy(alpha = 0.1f), CircleShape)
                .blur(100.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Stylized Header (Non-standard)
            AppHeader()

            // Chat Content
            Box(modifier = Modifier.weight(1f)) {
                if (chatMessages.isEmpty() && !isLoading) {
                    EmptyStateHint()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = 100.dp,
                            top = 20.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            AnimatedMessageEntry {
                                ChatBubble(msg)
                            }
                        }
                        if (isLoading) {
                            item {
                                LoadingIndicator()
                            }
                        }
                    }
                }
            }
        }


        // Floating Composer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                .widthIn(max = 600.dp)
        ) {
            FuturisticComposer(
                value = prompt,
                onValueChange = { prompt = it },

                onSend = {
                    val cleanedPrompt = prompt.trim()
                    if (cleanedPrompt.isNotBlank()) {
                        chatMessages.add(ChatMessage(nextMessageId++, ChatRole.USER, cleanedPrompt))
                        viewModel.askQuestionUsingPrompt(buildRequest(cleanedPrompt))
                        prompt = ""
                    }
                },
                enabled = !isLoading
            )
        }
    }
}

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "GEMINI",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 17.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Black,
                color = AI_Gradient_End
            )
        )
    }
}

@Composable
private fun EmptyStateHint() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Initiate sequence...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val isError = message.role == ChatRole.ERROR

    val arrangement = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = when {
        isUser -> RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
        else -> RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp)
    }

    val gradient = when {
        isUser -> Brush.linearGradient(listOf(AI_Gradient_Start, AI_Gradient_End))
        isError -> Brush.linearGradient(listOf(Color(0xFFFF5252), Color(0xFFFF1744)))
        else -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surface
            )
        )
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = arrangement) {
        Surface(
            modifier = Modifier
                .widthIn(max = if (isDesktop) 500.dp else 280.dp)
                .graphicsLayer {
                    shadowElevation = 8f
                    this.shape = shape
                    clip = true
                }
                .border(1.dp, Color.White.copy(alpha = 0.1f), shape),
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.background(gradient).padding(16.dp)) {
                Text(
                    text = message.text,
                    color = if (isUser || isError) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp)
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .graphicsLayer { this.alpha = alpha }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(AI_Gradient_Start, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Processing stream...", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun FuturisticComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(32.dp) // Define once for consistency

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // Remove the .background() from here!
            .then(Modifier),
        shape = shape,
        color = Color.Transparent, // Surface needs to be transparent to see the brush
        tonalElevation = 8.dp,
        border = borderStroke(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AI_Gradient_Start.copy(alpha = 0.18f),
                            AI_Gradient_End.copy(alpha = 0.18f),
                        )
                    )
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                enabled = enabled,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = Brush.verticalGradient(listOf(AI_Gradient_Start, AI_Gradient_End)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (value.isNotBlank()) {
                            onSend()
                            keyboardController?.hide()
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            "Ask Gemini...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(AI_Gradient_Start, AI_Gradient_End)),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun AnimatedMessageEntry(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn() + expandVertically()
    ) {
        content()
    }
}

private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

private fun buildRequest(prompt: String) = AskQuestionRequest(
    contents = listOf(Content(parts = listOf(Part(text = prompt))))
)

private data class ChatMessage(val id: Long, val role: ChatRole, val text: String)
private enum class ChatRole { USER, ASSISTANT, ERROR }
