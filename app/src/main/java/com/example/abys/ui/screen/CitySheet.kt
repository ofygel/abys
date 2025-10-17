@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.abys.R
import com.example.abys.data.CityEntry
import com.example.abys.data.CityRepository
import com.example.abys.logic.CitySheetTab
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.ui.util.backdropBlur

@Composable
fun CitySheet(
    city: String,
    hadith: String,
    cities: List<CityEntry>,
    activeTab: CitySheetTab,
    onCityChipTap: () -> Unit,
    onTabSelected: (CitySheetTab) -> Unit,
    onCityChosen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()
    val navPadding = WindowInsets.navigationBars.asPaddingValues()
    val blurSupported = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
    val backgroundTarget = if (activeTab == CitySheetTab.Wheel) {
        if (blurSupported) Tokens.Colors.glassPickerBlur else Tokens.Colors.glassPickerOpaque
    } else {
        if (blurSupported) Tokens.Colors.glassSheetBlur else Tokens.Colors.glassSheetOpaque
    }
    val backgroundColor by animateColorAsState(
        targetValue = backgroundTarget,
        animationSpec = tween(durationMillis = 220),
        label = "sheet-glass-color"
    )

    val shape = RoundedCornerShape((32f * s).dp)

    Box(
        modifier
            .fillMaxSize()
            .padding(horizontal = (28f * sx).dp, vertical = (28f * sy).dp)
    ) {
        Box(
            Modifier
                .matchParentSize()
                .shadow(elevation = (36f * sy).dp, shape = shape, clip = false)
                .clip(shape)
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .backdropBlur(8.dp)
                    .background(backgroundColor)
            )
            Column(
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .padding(bottom = navPadding.calculateBottomPadding())
            ) {
                CityNameChip(
                    city = city,
                    modifier = Modifier
                        .padding(top = (60f * sy).dp, start = (64f * sx).dp, end = (64f * sx).dp)
                        .pointerInput(Unit) { detectTapGestures { onCityChipTap() } }
                )

                Spacer(Modifier.height((48f * sy).dp))

                Box(
                    Modifier
                        .padding(horizontal = (72f * sx).dp)
                        .fillMaxWidth()
                ) {
                    HadithFrame(
                        text = hadith,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .align(Alignment.Center)
                    )
                }

                Spacer(Modifier.height((36f * sy).dp))

                CitySheetTabs(activeTab = activeTab, onTabSelected = onTabSelected)

                Spacer(Modifier.height((24f * sy).dp))

                Crossfade(
                    targetState = activeTab,
                    animationSpec = tween(durationMillis = 220),
                    label = "city-sheet-tab"
                ) { tab ->
                    when (tab) {
                        CitySheetTab.Wheel -> {
                            CityPickerWheel(
                                cities = cities,
                                currentCity = city,
                                onChosen = onCityChosen,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = (24f * sx).dp)
                            )
                        }

                        CitySheetTab.Search -> {
                            CitySearchPane(
                                cities = cities,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = (24f * sx).dp),
                                onCityChosen = onCityChosen
                            )
                        }
                    }
                }

                Spacer(Modifier.height((32f * sy).dp))
            }
        }
    }
}

@Composable
private fun CityNameChip(city: String, modifier: Modifier = Modifier) {
    val s = Dimens.s()
    val shape = RoundedCornerShape((24f * s).dp)
    val chipSize = ((36f * s).coerceIn(24f, 36f)).sp

    Box(
        modifier
            .fillMaxWidth()
            .sizeIn(minHeight = (64f * s).dp)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f), shape = shape)
            .padding(horizontal = (18f * s).dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            city,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AbysFonts.inter,
                fontSize = chipSize,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = Tokens.Colors.text,
                shadow = Shadow(
                    Tokens.Colors.tickDark.copy(alpha = 0.35f),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f
                )
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
private fun CitySheetTabs(activeTab: CitySheetTab, onTabSelected: (CitySheetTab) -> Unit) {
    val tabs = listOf(CitySheetTab.Wheel, CitySheetTab.Search)
    TabRow(
        selectedTabIndex = tabs.indexOf(activeTab),
        containerColor = Color.Transparent,
        contentColor = Tokens.Colors.text,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[tabs.indexOf(activeTab)])
                    .height(2.dp),
                color = Tokens.Colors.text
            )
        }
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == activeTab,
                onClick = { onTabSelected(tab) },
                text = {
                    val label = when (tab) {
                        CitySheetTab.Wheel -> stringResource(R.string.city_tab_wheel)
                        CitySheetTab.Search -> stringResource(R.string.city_tab_search)
                    }
                    Text(text = label, fontWeight = FontWeight.SemiBold)
                }
            )
        }
    }
}

@Composable
private fun CitySearchPane(
    cities: List<CityEntry>,
    modifier: Modifier = Modifier,
    onCityChosen: (String) -> Unit
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()
    val navPadding = WindowInsets.navigationBars.asPaddingValues()
    var query by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val trimmed = query.trim()
    val canSearch = trimmed.length >= 3

    val results = remember(trimmed, submitted, cities) {
        if (submitted && canSearch) {
            val ids = CityRepository.search(trimmed).map { it.id }.toSet()
            cities.filter { it.id in ids }
        } else {
            emptyList()
        }
    }
    val featured = remember(cities) {
        val featuredIds = CityRepository.featured().map { it.id }
        cities.filter { it.id in featuredIds }
            .sortedBy { featuredIds.indexOf(it.id) }
    }

    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = (16f * sy).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.trim().length < 3) submitted = false
                },
                placeholder = { Text(stringResource(R.string.city_search_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = (12f * sx).dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (canSearch) submitted = true
                })
            )
            Button(
                onClick = { submitted = true },
                enabled = canSearch,
            ) {
                Text(stringResource(R.string.city_search_button))
            }
        }

        val list = when {
            results.isNotEmpty() -> results
            submitted && canSearch -> emptyList()
            else -> featured
        }

        val listTitle = when {
            results.isNotEmpty() -> stringResource(R.string.city_search_results)
            submitted && canSearch -> stringResource(R.string.city_search_results)
            else -> stringResource(R.string.city_search_featured)
        }

        Text(
            text = listTitle,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AbysFonts.inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = (24f * s).sp,
                color = Tokens.Colors.text
            )
        )

        if (submitted && canSearch && list.isEmpty()) {
            Spacer(Modifier.height((32f * sy).dp))
            Text(
                text = stringResource(R.string.city_search_empty),
                style = MaterialTheme.typography.bodyMedium.copy(color = Tokens.Colors.text.copy(alpha = 0.72f))
            )
            return
        }

        Spacer(Modifier.height((16f * sy).dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = navPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy((12f * sy).dp)
        ) {
            items(list, key = { it.id }) { entry ->
                CitySearchRow(entry = entry, onCityChosen = onCityChosen)
            }
        }
    }
}

@Composable
private fun CitySearchRow(entry: CityEntry, onCityChosen: (String) -> Unit) {
    val s = Dimens.s()
    val shape = RoundedCornerShape((18f * s).dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Tokens.Colors.tickDark.copy(alpha = 0.08f))
            .pointerInput(entry.id) { detectTapGestures { onCityChosen(entry.display) } }
            .padding(vertical = (18f * s).dp, horizontal = (20f * s).dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = entry.display,
                fontFamily = AbysFonts.inter,
                fontSize = (28f * s).sp,
                fontWeight = FontWeight.SemiBold,
                color = Tokens.Colors.text
            )
            val secondary = entry.aliases
                .drop(1)
                .take(3)
                .joinToString(separator = " • ")
            if (secondary.isNotBlank()) {
                Text(
                    text = secondary,
                    fontFamily = AbysFonts.inter,
                    fontSize = (18f * s).sp,
                    color = Tokens.Colors.text.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun HadithFrame(
    text: String,
    modifier: Modifier = Modifier
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()
    val shape = RoundedCornerShape((46f * s).dp)
    val borderColor = Color.White.copy(alpha = 0.12f)
    Box(
        modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(Tokens.Colors.tickDark.copy(alpha = 0.08f))
            .padding(horizontal = (32f * sx).dp, vertical = (28f * sy).dp)
    ) {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
            if (text.isBlank()) {
                HadithPlaceholder()
            } else {
                val textSize = ((26f * s).coerceIn(18f, 26f)).sp
                BasicText(
                    text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AbysFonts.inter,
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        color = Tokens.Colors.text,
                        lineHeight = 1.42.em,
                        textAlign = TextAlign.Start,
                        shadow = Shadow(
                            Tokens.Colors.tickDark.copy(alpha = 0.35f),
                            offset = Offset(0f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun HadithPlaceholder(modifier: Modifier = Modifier) {
    val sy = Dimens.sy()
    val s = Dimens.s()
    val transition = rememberInfiniteTransition(label = "hadith-shimmer")
    val shimmerShift by transition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "hadith-shimmer"
    )

    val base = Tokens.Colors.tickDark.copy(alpha = 0.18f)
    val highlight = Color.White.copy(alpha = 0.35f)
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(shimmerShift, 0f),
        end = Offset(shimmerShift + 200f, 0f)
    )

    val lineHeights = listOf(28f, 28f, 28f, 24f)
    val widths = listOf(1f, 0.92f, 0.78f, 0.64f)

    Column(modifier) {
        lineHeights.zip(widths).forEachIndexed { index, (heightPx, widthFraction) ->
            Box(
                Modifier
                    .fillMaxWidth(widthFraction)
                    .height((heightPx * sy).dp)
                    .clip(RoundedCornerShape((14f * s).dp))
                    .background(brush)
            )
            if (index != lineHeights.lastIndex) {
                Spacer(Modifier.height((18f * sy).dp))
            }
        }
    }
}
