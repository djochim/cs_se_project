package dev.bitvictory.aeon.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.bitvictory.aeon.components.ChatMessage
import dev.bitvictory.aeon.components.JumpToBottom
import dev.bitvictory.aeon.components.TypingIndicator
import dev.bitvictory.aeon.model.api.AuthorDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data class Chat(val query: String)

fun NavGraphBuilder.chatDestination(onClose: () -> Unit) {
	composable<Chat> { backStackEntry ->
		val chat: Chat = backStackEntry.toRoute()
		ChatScreen(chat, onClose)
	}
}

fun NavController.navigateToChat(query: String) {
	navigate(route = Chat(query))
}

/**
 * Composable function for the chat screen.
 *
 * @param initialChat The initial chat to display.
 * @param onClose Callback to close the chat screen.
 * @param chatViewModel The view model for the chat screen.
 */
@Composable
fun ChatScreen(initialChat: Chat, onClose: () -> Unit, chatViewModel: ChatViewModel = koinInject()) {

	val uiState = chatViewModel.uiState.collectAsStateWithLifecycle()

	val scrollState = rememberLazyListState()
	val lifecycleOwner = LocalLifecycleOwner.current

	LaunchedEffect(key1 = lifecycleOwner) {
		chatViewModel.submitNewAdvisory(initialChat.query)
		chatViewModel.onLifecycleOwner(lifecycleOwner)
	}

	Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
		Row {
			IconButton(
				onClick = { onClose() },
				modifier = Modifier.padding(start = 8.dp)
			) {
				Icon(Icons.Outlined.Close, contentDescription = "Close chat")
			}
		}
		Messages(
			uiState.value.messages.sortedByDescending { it.creationDateTime },
			scrollState,
			uiState.value.isRefreshing,
			chatViewModel::refreshChat,
			modifier = Modifier.weight(1f)
		) // Push bottom row to the bottom
		Row(
			modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			OutlinedTextField(
				value = uiState.value.newMessage,
				onValueChange = chatViewModel::changeNewMessage,
				maxLines = 500,
				placeholder = { Text("Ask aeon") },
				modifier = Modifier.weight(1f)
			)
			FilledIconButton(
				onClick = chatViewModel::submitMessage,
				modifier = Modifier.padding(start = 8.dp, end = 8.dp)
			) {
				Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send query")
			}
		}
	}
}

private val JumpToBottomThreshold = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Messages(
	messages: List<MessageDTO>,
	scrollState: LazyListState,
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	modifier: Modifier = Modifier
) {
	if (messages.isEmpty()) return
	val scope = rememberCoroutineScope()
	Box(modifier = modifier) {
		PullToRefreshBox(
			isRefreshing = isRefreshing,
			onRefresh = onRefresh
		) {
			LazyColumn(
				reverseLayout = true,
				state = scrollState,
				modifier = Modifier
					.fillMaxSize()
			) {
				val lastMessage = messages.first()
				if (lastMessage.author == AuthorDTO.USER) {
					item {
						TypingIndicator()
					}
				}
				for (index in messages.indices) {
					val prevAuthor = messages.getOrNull(index - 1)?.author
					val nextAuthor = messages.getOrNull(index + 1)?.author
					val content = messages[index]
					val isPreviousMessageBySameAuthor = prevAuthor == content.author
					val isNextMessageBySameAuthor = nextAuthor == content.author

					item {
						ChatMessage(
							message = content,
							isCurrentUser = content.author == AuthorDTO.USER,
							isPreviousMessageBySameAuthor = isPreviousMessageBySameAuthor,
							isNextMessageBySameAuthor = isNextMessageBySameAuthor
						)
					}
				}
			}
		}
		// Jump to bottom button shows up when user scrolls past a threshold.
		// Convert to pixels:
		val jumpThreshold = with(LocalDensity.current) {
			JumpToBottomThreshold.toPx()
		}

		// Show the button if the first visible item is not the first one or if the offset is
		// greater than the threshold.
		val jumpToBottomButtonEnabled by remember {
			derivedStateOf {
				scrollState.firstVisibleItemIndex != 0 ||
						scrollState.firstVisibleItemScrollOffset > jumpThreshold
			}
		}

		JumpToBottom(
			// Only show if the scroller is not at the bottom
			enabled = jumpToBottomButtonEnabled,
			onClicked = {
				scope.launch {
					scrollState.animateScrollToItem(0)
				}
			},
			modifier = Modifier.align(Alignment.BottomCenter)
		)
	}
}