package dev.bitvictory.aeon.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.bitvictory.aeon.navigation.MenuItem

@Composable
fun LargeNavigationBar(items: List<MenuItem>) {
    var selectedItem by remember { mutableIntStateOf(0) }
    NavigationRail {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = {
                    Icon(
                        item.icon(selectedItem == index),
                        contentDescription = item.name
                    )
                },
                label = { Text(item.name) },
                selected = selectedItem == index,
                onClick = { selectedItem = index }
            )
        }
    }
}