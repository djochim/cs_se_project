package dev.bitvictory.aeon.screens.chat

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.AuthorDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryIdDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.StringMessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest
import dev.bitvictory.aeon.service.IAdvisorService
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Chat screen.
 *
 * This ViewModel is responsible for managing the state of the chat UI,
 * handling user interactions, and communicating with the [IAdvisorService]
 * and [IUserService].
 *
 * @param advisorService The service responsible for handling advisory-related operations.
 * @param userService The service responsible for handling user-related operations.
 */
class ChatViewModel(private val advisorService: IAdvisorService, userService: IUserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(ChatUIState())
	val uiState: StateFlow<ChatUIState> = _uiState.asStateFlow()

	private val _snackbarEvent = MutableSharedFlow<String>()
	val snackbarEvent = _snackbarEvent.asSharedFlow()

	private var checkNewMessagesJob: Job? = null

	fun changeNewMessage(newMessageText: String) {
		_uiState.value = _uiState.value.copy(newMessage = newMessageText)
	}

	/**
	 * Launches a coroutine to initiate a new advisory.
	 */
	fun submitNewAdvisory(initialMessageText: String) {
		val message = MessageDTO(messageContents = listOf(StringMessageDTO(initialMessageText)), author = AuthorDTO.USER, creationDateTime = Clock.System.now())
		_uiState.value = _uiState.value.copy(assistantIsTyping = true, error = "", newMessage = "", messages = listOf(message))
		viewModelScope.launch {
			when (val advisory = advisorService.initiateAdvisory(AdvisoryMessageRequest(StringMessageDTO(initialMessageText)))) {
				is AeonSuccessResponse -> {
					_uiState.value =
						_uiState.value.copy(messages = advisory.data.messages, advisoryId = AdvisoryIdDTO(advisory.data.id))
				}

				is AeonErrorResponse   -> {
					_snackbarEvent.emit("Failed to initiate advisory")
					_uiState.value = _uiState.value.copy(assistantIsTyping = false, error = advisory.error.message)
				}
			}
		}
	}

	/**
	 * Launches a coroutine to login the user. During that time the UI state is updated with loading and afterwards back.
	 */
	fun submitMessage() {
		val messageContent = _uiState.value.newMessage
		if (messageContent.isBlank() || _uiState.value.assistantIsTyping) return
		val message =
			MessageDTO(
				id = "new",
				messageContents = listOf(StringMessageDTO(messageContent)),
				author = AuthorDTO.USER,
				creationDateTime = Clock.System.now()
			)
		viewModelScope.launch {
			when (val messageResponse =
				advisorService.addMessage(_uiState.value.advisoryId!!, AdvisoryMessageRequest(StringMessageDTO(messageContent)))) {
				is AeonSuccessResponse -> {

				}

				is AeonErrorResponse   -> {
					_snackbarEvent.emit("Failed to add message to advisory")
					_uiState.value = _uiState.value.copy(assistantIsTyping = false, error = messageResponse.error.message)
				}
			}
		}
		_uiState.value = _uiState.value.copy(assistantIsTyping = true, error = "", newMessage = "", messages = _uiState.value.messages.plus(message))
	}

	fun refreshChat() {
		if (_uiState.value.advisoryId == null) return
		viewModelScope.launch {
			_uiState.value = _uiState.value.copy(isRefreshing = true)
			_uiState.value = checkForNewMessages().copy(isRefreshing = false)
		}
	}

	fun onLifecycleOwner(owner: LifecycleOwner) {
		if (checkNewMessagesJob?.isActive == true) return
		checkNewMessagesJob = owner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
			println("Exception in check new messages ${throwable.message}")
		}) {
			while (isActive) {
				_uiState.value.advisoryId?.let {
					try {
						_uiState.value = checkForNewMessages()
					} catch (e: Exception) {
						println("Error retrieving messages: ${e.message}")
					}
				}
				delay(500.milliseconds)
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		checkNewMessagesJob?.cancel()
	}

	private suspend fun checkForNewMessages() = when (val advisoryResult = advisorService.getAdvisory(_uiState.value.advisoryId!!)) {
		is AeonSuccessResponse -> {
			_uiState.value.copy(
				messages = advisoryResult.data.messages,
				assistantIsTyping = advisoryResult.data.messages.maxByOrNull { it.creationDateTime }!!.author in listOf(
					AuthorDTO.USER,
				)
			)
		}

		is AeonErrorResponse   -> {
			val errorMessage = MessageDTO(
				messageContents = listOf(StringMessageDTO(advisoryResult.error.message)),
				author = AuthorDTO.ASSISTANT,
				creationDateTime = Clock.System.now()
			)
			_uiState.value.copy(messages = _uiState.value.messages.plus(errorMessage))
		}
	}

}
