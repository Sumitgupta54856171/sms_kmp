package com.example.schoolmanagement.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.schoolmanagement.util.ToastEvent
import com.example.schoolmanagement.util.ToastManager
import com.example.schoolmanagement.util.ToastType
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CheckCircle
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import compose.icons.fontawesomeicons.solid.InfoCircle
import compose.icons.fontawesomeicons.solid.Times
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ToastHost(toastManager: ToastManager) {
    val activeToasts = remember { mutableStateListOf<ToastEvent>() }

    LaunchedEffect(toastManager) {
        toastManager.events.collect { event ->
            activeToasts.add(event)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .padding(top = 64.dp), // Avoid overlapping with TopAppBar if any
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            activeToasts.forEach { toast ->
                key(toast) {
                    var visible by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(Unit) {
                        delay(4000)
                        visible = false
                        delay(500)
                        activeToasts.remove(toast)
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInHorizontally { it } + fadeIn(),
                        exit = slideOutHorizontally { it } + fadeOut()
                    ) {
                        ToastCard(toast) {
                            visible = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToastCard(toast: ToastEvent, onDismiss: () -> Unit) {
    val iconColor = when (toast.type) {
        ToastType.SUCCESS -> Color(0xFF0D9488)
        ToastType.ERROR -> Color(0xFFEF4444)
        ToastType.INFO -> Color(0xFF3B82F6)
    }

    val icon = when (toast.type) {
        ToastType.SUCCESS -> FontAwesomeIcons.Solid.CheckCircle
        ToastType.ERROR -> FontAwesomeIcons.Solid.ExclamationCircle
        ToastType.INFO -> FontAwesomeIcons.Solid.InfoCircle
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = toast.message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Times,
                    contentDescription = "Close",
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
