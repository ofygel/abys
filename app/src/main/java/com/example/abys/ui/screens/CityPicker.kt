package com.example.abys.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CityPicker(
    show: Boolean,
    onDismiss: () -> Unit,
    onCityChosen: (String, Double, Double) -> Unit
) {
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.large
        ) {
            var query by remember { mutableStateOf("") }
            var results by remember { mutableStateOf(listOf<City>()) }

            LaunchedEffect(query) {
                results = CityDb.search(query)
            }

            Column(Modifier.padding(24.dp)) {
                Text("Укажите город", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Spacer(Modifier.height(12.dp))
                results.take(6).forEach { city ->
                    Text(
                        text = city.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCityChosen(city.name, city.lat, city.lon)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/** ======= ultra-tiny офлайн-«справочник» ======= */
private data class City(val name: String, val lat: Double, val lon: Double)
private object CityDb {
    private val list = listOf(
        City("Almaty",   43.238949, 76.889709),
        City("Astana",   51.160523, 71.470360),
        City("Shymkent", 42.341740, 69.590100),
        City("Karaganda",49.804687, 73.109382)
    )

    fun search(q: String): List<City> =
        if (q.isBlank()) emptyList()
        else list.filter { it.name.contains(q, ignoreCase = true) }
}
