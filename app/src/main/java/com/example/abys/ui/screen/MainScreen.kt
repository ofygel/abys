@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.logic.MainViewModel
import com.example.abys.ui.background.SlideshowBackground
import com.example.abys.ui.theme.Tokens
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/* -------------------------- публичный composable -------------------------- */

@Composable
fun MainApp(vm: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    // --- state из VM (у тебя уже есть MainViewModel, просто добираемся к данным) ---
    val city      by vm.city.observeAsState("Almaty")
    val times     by vm.prayerTimes.observeAsState(emptyMap())      // Map<String,String>
    val thirds    by vm.thirds.observeAsState(Triple("21:12","00:59","4:50"))
    val now       by vm.clock.observeAsState(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    val effects   = listOf(                                         // превью из drawable-nodpi
        R.drawable.thumb_leaves,
        R.drawable.thumb_sunset_snow,
        R.drawable.thumb_night
    )

    /* -------------------- корневой слой: фон + всё остальное -------------------- */
    Box(Modifier.fillMaxSize()) {
        SlideshowBackground()        // уже была в проекте

        MainScreen(
            city          = city,
            now           = now,
            prayerTimes   = times,
            thirds        = thirds,
            effects       = effects,
            onCityClick   = { /* открыть sheet с хадисами или picker – твоя логика */ },
            onEffectClick = vm::setEffect
        )
    }
}

/* ------------------------------ сам экран ------------------------------ */

@Composable
fun MainScreen(
    city:           String,
    now:            String,
    prayerTimes:    Map<String,String>,
    thirds:         Triple<String,String,String>,
    effects:        List<Int>,
    onCityClick:    () -> Unit,
    onEffectClick:  (Int) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        /* --- шапка (город + текущее время) --- */
        HeaderPill(
            city   = city,
            now    = now,
            onTap  = onCityClick,
            modifier = Modifier
                .padding(start = 51.dp, top = 79.dp, end = 51.dp)
                .height(102.dp)
        )

        /* --- карточка намазов --- */
        PrayerCard(
            times   = prayerTimes,
            thirds  = thirds,
            modifier = Modifier
                .padding(start = 64.dp, end = 64.dp, top = 226.dp)
                .height(611.dp)
        )

        /* --- нижняя карусель эффектов --- */
        EffectCarousel(
            items   = effects,
            onTap   = onEffectClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

/* ---------------------------------------------------------------------- */
/* ------------------------------- детали ------------------------------- */
/* ---------------------------------------------------------------------- */

@Composable
private fun HeaderPill(
    city: String,
    now:  String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) = Box(
    modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(Tokens.Radii.pill))
        .background(Tokens.Colors.overlayTop)
        .clickable { onTap() }
        .padding(horizontal = 16.dp),
    contentAlignment = Alignment.CenterStart
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text            = city,
            fontSize        = Tokens.TypographySp.city,
            fontWeight      = FontWeight.SemiBold,
            fontStyle       = FontStyle.Italic,
            color           = Tokens.Colors.text,
            textDecoration  = TextDecoration.Underline,
            modifier        = Modifier.weight(1f)
        )
        Text(
            text       = now,
            fontSize   = Tokens.TypographySp.timeNow,
            fontWeight = FontWeight.Bold,
            color      = Tokens.Colors.text
        )
    }
}

@Composable
private fun PrayerCard(
    times:   Map<String,String>,
    thirds:  Triple<String,String,String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.Radii.card))
            .background(Tokens.Colors.overlayCard)
            .padding(start = 44.dp, end = 44.dp, top = 45.dp, bottom = 40.dp)
    ) {
        fun RowItem(label: String, value: String) = Row(
            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label, Modifier.weight(1f),
                fontSize = Tokens.TypographySp.label,
                fontWeight = FontWeight.Bold,
                color = Tokens.Colors.text
            )
            Text(
                value,
                fontSize = Tokens.TypographySp.label,
                fontWeight = FontWeight.Bold,
                color = Tokens.Colors.text
            )
        }

        RowItem("Фаджр",    times["Fajr"   ] ?: "--:--")
        Spacer(Modifier.height(29.dp))
        RowItem("Восход",   times["Sunrise"] ?: "--:--")
        Spacer(Modifier.height(29.dp))
        RowItem("Зухр",     times["Dhuhr"  ] ?: "--:--")
        Spacer(Modifier.height(29.dp))

        Text("Аср:", fontSize = Tokens.TypographySp.label,
             fontWeight = FontWeight.Bold, color = Tokens.Colors.text)
        Spacer(Modifier.height(4.dp))
        AsrSub("стандарт", times["AsrStd"] ?: "--:--")
        Spacer(Modifier.height(29.dp))
        AsrSub("Ханафи",   times["AsrHana"] ?: "--:--")
        Spacer(Modifier.height(29.dp))

        RowItem("Магриб",  times["Maghrib"] ?: "--:--")
        Spacer(Modifier.height(29.dp))
        RowItem("Иша",     times["Isha"   ] ?: "--:--")

        Spacer(Modifier.height(24.dp))
        Timeline(thirds)
    }
}

@Composable
private fun AsrSub(label: String, value: String) = Row(
    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
) {
    Text(
        label, Modifier.weight(1f),
        fontSize = Tokens.TypographySp.subLabel,
        fontWeight = FontWeight.Bold,
        color = Tokens.Colors.text
    )
    Box(
        Modifier.width(64.dp).height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Tokens.Colors.text)
    )
    Spacer(Modifier.width(8.dp))
    Text(
        value,
        fontSize = Tokens.TypographySp.subLabel,
        fontWeight = FontWeight.Bold,
        color = Tokens.Colors.text
    )
}

@Composable
private fun Timeline(thirds: Triple<String,String,String>) = Row(
    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
) {
    listOf(thirds.first, thirds.second, thirds.third).forEachIndexed { idx, t ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.width(3.dp)
                    .height(if (idx == 1) 57.dp else 49.dp)
                    .background(Tokens.Colors.text)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                t,
                fontSize   = Tokens.TypographySp.timeline,
                fontWeight = FontWeight.Bold,
                color      = Tokens.Colors.text
            )
        }
    }
}

@Composable
private fun EffectCarousel(
    items:   List<Int>,
    onTap:   (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // используем rememberLazyListState, чтобы держать центр по спеке
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(modifier) {
        LazyRow(
            state       = state,
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            contentPadding = PaddingValues(horizontal = 40.dp)
        ) {
            itemsIndexed(items) { index, res ->
                val center   = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    ?.let { (it.offset + it.size / 2f) to state.layoutInfo.viewportSize.width / 2f }
                val distance = center?.let { abs(it.first - it.second) } ?: Float.POSITIVE_INFINITY
                val (scale, alpha) = when {
                    distance < 40      -> 1.0f to 1.0f   // центральный
                    distance < 150     -> 0.85f to 0.7f  // соседи
                    else               -> 0.75f to 0.5f
                }

                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 121.dp, height = 153.dp)
                        .graphicsLayer { this.scaleX = scale; this.scaleY = scale; this.alpha = alpha }
                        .clip(RoundedCornerShape(Tokens.Radii.chip))
                        .clickable {
                            scope.launch {
                                // снап по центру
                                state.animateScrollBy(state.layoutInfo.viewportSize.width / 2f -
                                                      (center?.first ?: 0f))
                            }
                            onTap(res)
                        }
                )
            }
        }
    }
}
