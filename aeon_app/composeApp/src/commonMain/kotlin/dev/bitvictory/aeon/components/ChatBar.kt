package dev.bitvictory.aeon.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap

@Composable
fun ChatBar(message: String, images: List<ByteArray>, changeMessage: (newMessage: String) -> Unit, submitMessage: () -> Unit, addImage: (image: ByteArray) -> Unit, deleteImage: () -> Unit) {

    val imagePickerScope = rememberCoroutineScope()

    val resizeOptions = ResizeOptions(
        compressionQuality = 0.5 // Adjust compression quality (0.0 to 1.0)
    )
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = imagePickerScope,
        resizeOptions = resizeOptions,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                addImage(it)
            }
        }
    )

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        images.firstOrNull()?.let {
            ImagePreview(it, deleteImage)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = changeMessage,
                maxLines = 500,
                placeholder = { Text("Ask aeon") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(
                        onClick = { singleImagePicker.launch() },
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = "Add image")
                    }
                }
            )
            FilledIconButton(
                onClick = submitMessage,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send query")
            }
        }
    }
}

@Composable
fun ImagePreview(image: ByteArray, deleteImage: () -> Unit) {
    Box(modifier = Modifier.padding(end = 2.dp)) {
        Box(modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
            Image(
                bitmap = image.toImageBitmap(),
                contentDescription = "Selected image preview",
                modifier = Modifier
                    .size(48.dp) // Adjust size as needed
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }
        FilledIconButton(
            onClick = { deleteImage() },
            modifier = Modifier.align(Alignment.TopEnd).background(MaterialTheme.colorScheme.secondary).size(18.dp),
            shape = CircleShape
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = "Remove Image", modifier = Modifier.size(12.dp))
        }
    }
}