package com.example.foodmanager.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodmanager.data.supabase
import com.example.foodmanager.utils.getPasswordError
import com.example.foodmanager.utils.isValidEmail
import foodmanager.composeapp.generated.resources.Res
import foodmanager.composeapp.generated.resources.foodmanager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.foodmanager),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
        Text(text = "Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.85f),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            }
        )

        Column(
            modifier = Modifier.fillMaxWidth(0.85f).padding(top = 4.dp, start = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            RequirementText(text = "At least 8 characters", isMet = hasMinLength)
            RequirementText(text = "Contains uppercase letter", isMet = hasUpper)
            RequirementText(text = "Contains lowercase letter", isMet = hasLower)
            RequirementText(text = "Contains a number", isMet = hasDigit)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text("Confirm Password") },
            // FIXED: Now properly uses confirmPasswordVisible
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.85f),
            trailingIcon = {
                // FIXED: Now properly toggles confirmPasswordVisible
                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Text(if (confirmPasswordVisible) "Hide" else "Show")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val passwordError = getPasswordError(password)

                when {
                    !isValidEmail(email) -> errorMessage = "Please enter a valid email address."
                    passwordError != null -> errorMessage = passwordError
                    password != confirmPassword -> errorMessage = "Passwords do not match." // The matching logic!
                    else -> {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                supabase.auth.signUpWith(Email) {
                                    this.email = email
                                    this.password = password
                                }
                                infoMessage = "Registration successful! Please log in."
                                // FIXED: Actually trigger the navigation now!
                                onRegisterSuccess()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Registration failed."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = "Register")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in here.")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
        }
        if (infoMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = infoMessage ?: "", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RequirementText(text: String, isMet: Boolean) {
    val color = if (isMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Text(
        text = "• $text",
        color = color,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 12.sp
    )
}