package dev.bitvictory.aeon.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StateButton(
	initialText: String,
	loadingText: String,
	isLoading: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Button(
		onClick = { if (!isLoading) onClick() },
		enabled = !isLoading,
		modifier = modifier,
	) {
		Row(
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			if (isLoading) {
				Text(text = loadingText)
				Spacer(modifier = Modifier.padding(4.dp))
				Icon(
					imageVector = Icons.Default.Cached,
					contentDescription = "Loading Icon",
					tint = MaterialTheme.colorScheme.onTertiary
				)
			} else {
				Text(text = initialText)
			}
		}
	}
}