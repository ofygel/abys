package com.example.abys.ui.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import com.example.abys.logic.CitySearchViewModel
import com.example.abys.net.NominatimPlace
import com.example.abys.R

data class CitySuggestion(
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CityPickerSheet(
    onPick: (CitySuggestion) -> Unit,
    vm: CitySearchViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val results by vm.results.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)

    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.city_search_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                vm.query(it)
            },
            placeholder = { Text(stringResource(id = R.string.city_search_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        if (query.isBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(id = R.string.city_search_featured), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                featuredCities.fastForEach { suggestion ->
                    AssistChip(
                        onClick = { onPick(suggestion) },
                        label = { Text(suggestion.title) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        val listModifier = Modifier.heightIn(max = 360.dp)
        when {
            loading -> {
                Box(listModifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            }
            results.isEmpty() && query.isNotBlank() -> {
                Box(listModifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.city_search_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(listModifier) {
                    items(results) { place ->
                        val suggestion = place.toSuggestion()
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(suggestion) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = suggestion.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = suggestion.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun NominatimPlace.toSuggestion(): CitySuggestion {
    val primary = display_name.substringBefore(",").trim()
    return CitySuggestion(
        title = primary.ifBlank { display_name },
        subtitle = display_name,
        latitude = lat.toDoubleOrNull() ?: 0.0,
        longitude = lon.toDoubleOrNull() ?: 0.0
    )
}

private val featuredCities = listOf(
    CitySuggestion("Almaty", "Kazakhstan", 43.238949, 76.889709),
    CitySuggestion("Astana", "Kazakhstan", 51.160523, 71.47036),
    CitySuggestion("Shymkent", "Kazakhstan", 42.34174, 69.5901),
    CitySuggestion("Karaganda", "Kazakhstan", 49.804687, 73.109382),
    CitySuggestion("Turkistan", "Kazakhstan", 43.297332, 68.251747)
)
