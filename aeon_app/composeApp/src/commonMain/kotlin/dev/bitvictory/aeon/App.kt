package dev.bitvictory.aeon

import AppTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import aeon.composeapp.generated.resources.Res
import aeon.composeapp.generated.resources.compose_multiplatform
import dev.bitvictory.aeon.navigation.Navigation

@Composable
@Preview
fun App() {
    AppTheme {
        Navigation()
    }
}