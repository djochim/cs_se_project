package dev.bitvictory.aeon.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.preat.peekaboo.image.picker.toImageBitmap
import dev.bitvictory.aeon.model.api.advisory.ImageMessageDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.StringMessageDTO

private val ChatBubbleShapeLeft = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp)
private val ChatBubbleShapeRight = RoundedCornerShape(20.dp, 0.dp, 20.dp, 20.dp)

@Composable
fun ChatMessage(
	message: MessageDTO,
	isCurrentUser: Boolean,
	isPreviousMessageBySameAuthor: Boolean,
	isNextMessageBySameAuthor: Boolean
) {
	val arrangement = if (isCurrentUser)
		Arrangement.End
	else
		Arrangement.Start
	Row(
		modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
		horizontalArrangement = arrangement
	) {
		ChatItemBubble(message, isCurrentUser)
	}
}

@Composable
fun ChatItemBubble(
	message: MessageDTO,
	isCurrentUser: Boolean,
) {
	val backgroundBubbleColor = if (isCurrentUser) {
		MaterialTheme.colorScheme.surfaceContainer
	} else {
		MaterialTheme.colorScheme.surfaceVariant
	}

	val textColor = if (isCurrentUser) {
		MaterialTheme.colorScheme.onSurface
	} else {
		MaterialTheme.colorScheme.onSurfaceVariant
	}

	val shape = if (isCurrentUser) {
		ChatBubbleShapeRight
	} else {
		ChatBubbleShapeLeft
	}

	Surface(
		color = backgroundBubbleColor,
		shape = shape
	) {
		message.messageContents.forEach { content ->
			val text = when (content) {
				is StringMessageDTO -> StringMessageBubble(content.content, textColor)
				is ImageMessageDTO -> ImageMessageBubble(content.content)
			}

		}
	}
}

@Composable
fun ImageMessageBubble(image: ByteArray) {
	Image(
		bitmap = image.toImageBitmap(),
		contentDescription = "Selected image preview",
		modifier = Modifier
			.size(48.dp) // Adjust size as needed
			.clip(RoundedCornerShape(4.dp)),
		contentScale = ContentScale.Crop
	)
}

@Composable
fun StringMessageBubble(text: String, textColor: Color) {
	Markdown(
		text,
		colors = markdownColor(
			text = textColor,
			codeText = textColor,
			codeBackground = textColor.copy(alpha = 0.1f),
			dividerColor = MaterialTheme.colorScheme.outlineVariant,
			tableBackground = textColor.copy(alpha = 0.02f)
		),
		components = markdownComponents(),
		typography = markdownTypography(),
		modifier = Modifier.padding(16.dp).defaultMinSize(minWidth = 100.dp)
	)
}
