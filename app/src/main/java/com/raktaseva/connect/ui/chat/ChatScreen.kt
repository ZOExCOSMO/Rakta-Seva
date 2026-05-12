package com.raktaseva.connect.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.connect.data.model.ChatMessage
import com.raktaseva.connect.data.remote.ApiService
import com.raktaseva.connect.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: () -> Unit) {
    val messages   = remember { mutableStateListOf<ChatMessage>() }
    var inputText  by remember { mutableStateOf("") }
    var isLoading  by remember { mutableStateOf(false) }
    val scope      = rememberCoroutineScope()
    val listState  = rememberLazyListState()

    val suggestions = listOf(
        "Am I eligible to donate?",
        "What should I eat before donating?",
        "Is B+ blood common in India?",
        "How does the 10km alert work?"
    )
    var showSuggestions by remember { mutableStateOf(true) }

    // Welcome message
    LaunchedEffect(Unit) {
        messages.add(ChatMessage("assistant",
            "Namaste! 🙏 I'm Rakta AI.\n\nI can help with blood donation eligibility, " +
                    "pre/post donation care, blood type compatibility, and how the app works.\n\n" +
                    "What would you like to know?"))
    }

    suspend fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return
        showSuggestions = false
        inputText = ""
        messages.add(ChatMessage("user", text))
        listState.animateScrollToItem(messages.size - 1)
        isLoading = true
        try {
            val history = messages.map { mapOf("role" to it.role, "content" to it.content) }
            val response = ApiService.create().chat(ApiService.ChatRequest(history))
            messages.add(ChatMessage("assistant", response.reply))
        } catch (e: Exception) {
            messages.add(ChatMessage("assistant", "⚠️ Connection error. Please try again!"))
        }
        isLoading = false
        listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextSecondary)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(RedSurface, RoundedCornerShape(19.dp))
                                .border(0.5.dp, BorderMid, RoundedCornerShape(19.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("🤖", fontSize = 18.sp) }
                        Column {
                            Text("Rakta AI", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(Modifier.size(6.dp).background(GreenLight, RoundedCornerShape(3.dp)))
                                Text("Online · Powered by Claude", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(RedSurface, RoundedCornerShape(7.dp))
                            .border(0.5.dp, BorderMid, RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RedLight) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(BgCard).padding(bottom = 8.dp)) {
                // Suggestion chips
                if (showSuggestions) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        suggestions.forEach { sug ->
                            FilterChip(
                                selected = false,
                                onClick  = { scope.launch { sendMessage(sug) } },
                                label    = { Text(sug, fontSize = 11.5.sp, color = RedLight) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    containerColor = BgElevated,
                                    labelColor     = RedLight
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled  = true,
                                    selected = false,
                                    borderColor = BorderMid
                                )
                            )
                        }
                    }
                }
                // Input row
                Row(
                    modifier = Modifier
                        .padding(horizontal = 11.dp, vertical = 4.dp)
                        .border(0.5.dp, BorderMid, RoundedCornerShape(22.dp))
                        .background(BgElevated, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about donation, eligibility…", color = TextTertiary, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor   = Color.Transparent,
                            unfocusedTextColor      = TextPrimary,
                            focusedTextColor        = TextPrimary,
                            cursorColor             = RedPrimary,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines   = 3
                    )
                    IconButton(
                        onClick  = { scope.launch { sendMessage(inputText) } },
                        enabled  = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (inputText.isNotBlank()) RedDark else BgElevated,
                                RoundedCornerShape(18.dp)
                            )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 11.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
            if (isLoading) {
                item { TypingIndicator() }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    // Animate in
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(RedSurface, RoundedCornerShape(14.dp))
                        .border(0.5.dp, BorderMid, RoundedCornerShape(14.dp))
                        .align(Alignment.Bottom),
                    contentAlignment = Alignment.Center
                ) { Text("🤖", fontSize = 13.sp) }
                Spacer(Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        if (isUser)
                            Brush.linearGradient(listOf(RedDark, RedDeep))
                        else
                            Brush.linearGradient(listOf(BgCard, BgCard)),
                        RoundedCornerShape(
                            topStart = 17.dp, topEnd = 17.dp,
                            // Sharp corner on correct side per spec
                            bottomStart = if (isUser) 17.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 17.dp
                        )
                    )
                    .border(
                        0.5.dp,
                        if (isUser) RedBorder else BorderSubtle,
                        RoundedCornerShape(17.dp)
                    )
                    .padding(10.dp, 9.dp)
            ) {
                Text(
                    message.content,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else TextPrimary,
                    lineHeight = 19.sp
                )
            }

            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(BgElevated, RoundedCornerShape(14.dp))
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .align(Alignment.Bottom),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AM", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .background(BgElevated, RoundedCornerShape(17.dp))
                .border(0.5.dp, BorderSubtle, RoundedCornerShape(17.dp))
                .padding(14.dp, 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        Modifier.size(7.dp).background(TextTertiary, RoundedCornerShape(3.5.dp))
                    )
                }
            }
        }
    }
}