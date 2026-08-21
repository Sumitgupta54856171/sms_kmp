package com.example.schoolmanagement.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 900.dp
        
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel (Hidden on small screens)
            if (isWide) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF0D9488))
                ) {
                    // Dotted Grid Overlay (Simplified)
                    Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
                        val dotRadius = 1.dp.toPx()
                        val spacing = 32.dp.toPx()
                        for (x in 0..(size.width / spacing).toInt()) {
                            for (y in 0..(size.height / spacing).toInt()) {
                                drawCircle(
                                    color = Color.White,
                                    radius = dotRadius,
                                    center = Offset(x * spacing, y * spacing)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(48.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header / Logo
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = Color(0xFF6366F1)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("RC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                            }
                            Text(
                                "Rose Convent High School",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        // Quote
                        Column(modifier = Modifier.widthIn(max = 450.dp)) {
                            Text(
                                text = "\"The administrative weight of a school should never outweigh the teaching inside it.\"",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light,
                                lineHeight = 38.sp
                            )
                            Spacer(Modifier.height(32.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(Modifier.size(40.dp).background(Color(0xFF1E293B), CircleShape))
                                Column {
                                    Text("Mr. Mohan Lal Sen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Head of School, Rose Convent", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                                }
                            }
                        }
                        
                        // Empty spacer at bottom
                        Box(Modifier.height(40.dp))
                    }
                }
            }

            // Right Panel (Form)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFFAFAFA)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Form Header
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Welcome back",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Sign in to the Rose Convent workspace.",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Inputs
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Email address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { viewModel.onEmailChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("email@example.com", color = Color.LightGray) },
                                shape = MaterialTheme.shapes.small,
                                singleLine = true,
                                enabled = state !is LoginState.Loading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { viewModel.onPasswordChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter your password", color = Color.LightGray) },
                                visualTransformation = PasswordVisualTransformation(),
                                shape = MaterialTheme.shapes.small,
                                singleLine = true,
                                enabled = state !is LoginState.Loading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        if (state is LoginState.Error) {
                            Text(
                                text = (state as LoginState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { viewModel.login() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = MaterialTheme.shapes.small,
                            enabled = state !is LoginState.Loading,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (state is LoginState.Loading) "Signing in..." else "Sign in",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (state !is LoginState.Loading) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("→", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
