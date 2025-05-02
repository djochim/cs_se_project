package dev.bitvictory.aeon.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
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
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
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
        val text = when (val content = message.messageContent) {
            is StringMessageDTO -> content.content
        }
        Markdown(
            text,
            colors = markdownColor(),
            components = markdownComponents(),
            typography = markdownTypography(),
            modifier = Modifier.padding(16.dp).defaultMinSize(minWidth = 100.dp)
        )
    }
}
