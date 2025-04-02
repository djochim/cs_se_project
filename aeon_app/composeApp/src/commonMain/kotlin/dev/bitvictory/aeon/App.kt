package dev.bitvictory.aeon

import androidx.compose.runtime.Composable
import dev.bitvictory.aeon.navigation.MainNavigation
import dev.bitvictory.aeon.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
	KoinContext {
		AppTheme {
			MainNavigation()
		}
	}
}