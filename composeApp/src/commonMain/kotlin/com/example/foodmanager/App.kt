package com.example.foodmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodmanager.ui.theme.DarkBlue
import com.example.foodmanager.ui.theme.LightBlue
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.Surface
import foodmanager.composeapp.generated.resources.Res
import foodmanager.composeapp.generated.resources.compose_multiplatform
import com.example.foodmanager.ui.theme.FoodManagerTheme

@Composable
@Preview
fun App() {
    FoodManagerTheme {
        // Applies the Dark Color to the back of the page
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBlue
        )
        // Actual content of the app
        {
            Text("Food Manager")
        }
    }
}
