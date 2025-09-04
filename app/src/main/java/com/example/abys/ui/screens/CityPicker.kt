package com.example.abys.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.abys.MainViewModel

@Composable
fun CityPickerModal(
    vm: MainViewModel,
    bgRes: Int,
    visible: Boolean,
    onDismiss: () -> Unit,
    onGps: () -> Unit,
    onChosen: () -> Unit
) {
    if (!visible) return
    BackHandler(enabled = true) { onDismiss() }

    Box(
        Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
            .background(Color.Black.copy(0.35f))
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Полное имя вместо импорта, чтобы исключить конфликт
            androidx.compose.foundation.Image(
                painter = painterResource(bgRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().blur(14.dp)
            )
            Box(Modifier.matchParentSize().background(Color(0x33FFFFFF)))

            Column(Modifier.padding(16.dp)) {
                val q by vm.query.collectAsStateWithLifecycle()
                val sugg by vm.suggestions.collectAsStateWithLifecycle()

                OutlinedTextField(
                    value = q,
                    onValueChange = { vm.updateQuery(it) },
                    singleLine = true,
                    placeholder = { Text("Введите город…") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row {
                    Button(onClick = { vm.fetchByCity(q); onChosen(); onDismiss() }) {
                        Text("Найти")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { onGps(); onDismiss() }) {
                        Text("GPS")
                    }
                }
                Spacer(Modifier.height(12.dp))
                sugg.forEach { item ->
                    Text(
                        text = item.display_name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.selectSuggestion(item)
                                onChosen()
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                    )
                    Divider(color = Color.White.copy(0.08f))
                }
            }
        }
    }
}
