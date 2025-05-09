package app.sjk.hello.country.representation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun LanguageMenu(
    onLanguageSelected: (Locale, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Place the floating button at the top right.
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Select Language"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onLanguageSelected(Locale.ENGLISH, "English")
                    expanded = false
                },
                text = { Text("English") },
            )
            DropdownMenuItem(
                onClick = {
                    onLanguageSelected(Locale.FRENCH, "Français")
                    expanded = false
                },
                text = { Text("Français") },
            )
            DropdownMenuItem(
                onClick = {
                    // For Chinese, you can use Locale.CHINA or Locale.SIMPLIFIED_CHINESE
                    onLanguageSelected(Locale.SIMPLIFIED_CHINESE, "中文")
                    expanded = false
                },
                text = { Text("中文") },
            )
            /*DropdownMenuItem(
                onClick = {
                    // For Spanish, using the Spain locale.
                    onLanguageSelected(Locale("es", "ES"), "Español")
                    expanded = false
                },
                text = { Text("Español") }
            )*/
        }
    }
}
