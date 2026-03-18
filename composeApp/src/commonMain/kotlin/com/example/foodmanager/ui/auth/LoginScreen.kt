package com.example.foodmanager.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import com.example.foodmanager.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import foodmanager.composeapp.generated.resources.Res
import foodmanager.composeapp.generated.resources.foodmanager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource







@Composable
fun LoginScreen(loginSuccess: () -> Unit) { //loginSuccess is the function for accepting the login

    // Initial value for username and password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // Food Manager Icon
        Image(
            painter = painterResource(Res.drawable.foodmanager),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
        // Title
        Text(text = "Food Manager",
            style = MaterialTheme.typography.headlineLarge)

        // Gap between text fields
        Spacer(modifier = Modifier.height(16.dp))

        // Receiving the inputs for username and password
        OutlinedTextField(
            value = email,
            onValueChange = { newText ->
                email = newText
            },
            label = { Text("Email")}
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { newPassword ->
                password = newPassword
            },
            label = { Text("Password")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password) // Avoids autocorrect
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Actual login button
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    infoMessage = null
                    try {
                        supabase.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        loginSuccess()
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Login failed."
                    } finally {
                        isLoading = false
                    }
                }
                      },
                // Displays only if username and password are not empty
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
        ) {
            Text(text = "Log In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    infoMessage = null
                    try {
                        supabase.auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        infoMessage = "Registration successful."
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Registration failed."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
        ) {
            Text(text = "Register")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
        }

        if (infoMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = infoMessage ?: "")
        }
    }}
