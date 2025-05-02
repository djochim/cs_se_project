package dev.bitvictory.aeon.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val circleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val circleRadius = 3.dp
    val circleSpace = 10.dp

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .size(width = 60.dp, height = 30.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = circleColor,
                radius = circleRadius.toPx(),
                center = Offset(x = 10.dp.toPx(), y = 15.dp.toPx() + offsetY)
            )
            drawCircle(
                color = circleColor,
                radius = circleRadius.toPx(),
                center = Offset(x = (10.dp + circleSpace).toPx(), y = 15.dp.toPx() - offsetY)
            )
            drawCircle(
                color = circleColor,
                radius = circleRadius.toPx(),
                center = Offset(x = (10.dp + circleSpace * 2).toPx(), y = 15.dp.toPx() + offsetY)
            )
        }
    }
}