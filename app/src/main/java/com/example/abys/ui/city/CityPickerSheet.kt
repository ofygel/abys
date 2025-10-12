package com.example.abys.ui.city

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.abys.logic.CitySearchViewModel
import com.example.abys.net.NominatimPlace

@Composable
fun CityPickerSheet(onPick: (String) -> Unit) {
    val vm: CitySearchViewModel = viewModel()
    var q by remember { mutableStateOf("") }
    val results by vm.results.observeAsState(emptyList())

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Выбор города", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = q,
            onValueChange = {
                q = it
                vm.query(q)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Almaty / Astana / Shymkent ...") }
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(results) { p: NominatimPlace ->
                Column(
                    Modifier.fillMaxWidth()
                        .clickable { onPick(extractCity(p.display_name)) }
                        .padding(vertical = 10.dp)
                ) {
                    Text(extractCity(p.display_name), style = MaterialTheme.typography.bodyLarge)
                    Text(p.display_name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Divider()
            }
        }
    }
}

private fun extractCity(display: String): String {
    // Берём первое поле до запятой как «город»
    return display.substringBefore(",").trim()
}
