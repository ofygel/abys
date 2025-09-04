package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.abys.MainViewModel

@Composable
fun CitySearchBar(vm: MainViewModel, onSearch: (String) -> Unit, onGps: () -> Unit) {
    val q by vm.query.collectAsStateWithLifecycle()
    val sugg by vm.suggestions.collectAsStateWithLifecycle()

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = q,
                onValueChange = { vm.updateQuery(it) },
                placeholder = { Text("Город… (например, Almaty)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSearch(q) }) { Text("Найти") }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = onGps) { Text("GPS") }
        }
        if (sugg.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            ) {
                sugg.forEach { item ->
                    Text(
                        text = item.display_name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.selectSuggestion(item) }
                            .padding(12.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}
