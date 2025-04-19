package dev.bitvictory.aeon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.bitvictory.aeon.dependencyinjection.startSdk

class MainActivity: ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)


		startSdk(this@MainActivity)

		setContent {
			App()
		}
	}
}

@Preview
@Composable
fun AppAndroidPreview() {
	App()
}