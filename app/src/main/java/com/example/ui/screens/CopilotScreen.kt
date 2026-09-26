package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CopilotMessageEntity
import com.example.ui.AppNavTab
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CopilotScreen(
    messages: List<CopilotMessageEntity>,
    isThinking: Boolean,
    onSendQuery: (String) -> Unit,
    onNavigate: (AppNavTab) -> Unit,
    onGeneratePaymentReminder: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickQuestions = listOf(
        "Recover ₹2.15L overdue from Gujarat Tooling",
        "Estimate Net GSTR-3B Tax & ITC Claim",
        "Analyze Low Stock & Draft Restock Plan",
        "Which clients offer highest profit margin?",
        "How does MSMED Act 45-day rule apply?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("copilot_screen")
    ) {
        // Copilot Status Strip
        Surface(
            color = ImperialNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RoyalTeakGold)
                    )
                    Column {
                        Text(
                            text = "GrowthEngine Strategic Copilot",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time GST, Khata & Working Capital Advisory",
                            color = RoyalTeakGoldLight,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    color = RoyalTeakGoldContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "GEMINI FLASH 3.5",
                        color = RoyalTeakGoldDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Quick Suggestion Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmIvorySurfaceVariant)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { question ->
                SuggestionChip(
                    onClick = {
                        onSendQuery(question)
                    },
                    label = {
                        Text(
                            text = question,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImperialNavy
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = WarmIvorySurface
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = RoyalTeakGold.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("copilot_chip_${question.take(10)}")
                )
            }
        }

        // Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                val isUser = message.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ImperialNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RoyalTeakGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Surface(
                            color = if (isUser) ImperialNavy else WarmIvorySurface,
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isUser) 12.dp else 2.dp,
                                bottomEnd = if (isUser) 2.dp else 12.dp
                            ),
                            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder) else null,
                            tonalElevation = if (isUser) 0.dp else 2.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = message.messageText,
                                    color = if (isUser) Color.White else TextPrimaryDark,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )

                                // Action Buttons if provided
                                if (message.actionType == "PAYMENT_REMINDER") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onGeneratePaymentReminder,
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("copilot_action_reminder")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Draft WhatsApp Notice with UPI", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ImperialNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = RoyalTeakGold,
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analyzing enterprise financials & GST rules...",
                            color = TextSecondaryMuted,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Surface(
            color = WarmIvorySurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Copilot regarding GST, Khata, Stock...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copilot_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImperialNavy,
                        unfocusedBorderColor = WarmIvoryBorder
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val text = inputText
                            inputText = ""
                            onSendQuery(text)
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ImperialNavy)
                        .testTag("copilot_send_button"),
                    enabled = inputText.isNotBlank() && !isThinking
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) RoyalTeakGoldLight else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
