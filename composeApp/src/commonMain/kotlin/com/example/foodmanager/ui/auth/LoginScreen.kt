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
import foodmanager.composeapp.generated.resources.Res
import foodmanager.composeapp.generated.resources.foodmanager
import org.jetbrains.compose.resources.painterResource







@Composable
fun LoginScreen(loginSuccess: () -> Unit) { //loginSuccess is the function for accepting the login

    // Initial value for username and password
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            value = username,
            onValueChange = { newText ->
                username = newText
            },
            label = { Text("Username")}
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
                // As this is a dummy login, we will have to check here whether the password and username are correct
                // LOG IN LOGIC

                loginSuccess()
                      },
                // Displays only if username and password are not empty
                enabled = username.isNotBlank() && password.isNotBlank()
        ) {
            Text(text = "Log In")
        }
    }}
