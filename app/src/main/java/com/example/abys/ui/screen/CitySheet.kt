@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.em
import androidx.compose.ui.graphics.CompositingStrategy
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.ui.util.backdropBlur

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CitySheet(
    city:           String,
    hadith:         String,
    cities:         List<String>,
    pickerVisible:  Boolean,
    onCityChipTap:  () -> Unit,
    onCityChosen:   (String) -> Unit,
    modifier:       Modifier = Modifier
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()
    val blurSupported = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
    val backgroundTarget = if (pickerVisible) {
        if (blurSupported) Tokens.Colors.glassPickerBlur else Tokens.Colors.glassPickerOpaque
    } else {
        if (blurSupported) Tokens.Colors.glassSheetBlur else Tokens.Colors.glassSheetOpaque
    }
    val backgroundColor by animateColorAsState(
        targetValue = backgroundTarget,
        animationSpec = tween(durationMillis = 220),
        label = "glassColor"
    )

    val shape = RoundedCornerShape((32f * s).dp)
    Box(
        modifier
            .fillMaxSize()
            .padding(
                horizontal = (28f * sx).dp,
                vertical = (28f * sy).dp
            )
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha }
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .backdropBlur(8.dp)
                    .background(backgroundColor)
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clip(shape)
            ) {
                val chipSize = ((36f * s).coerceIn(24f, 36f)).sp

                Box(
                    Modifier
                        .padding(horizontal = (56f * sx).dp, vertical = (64f * sy).dp)
                        .height((64f * s).dp)
                        .fillMaxWidth()
                        .border(
                            width = 3.dp,
                            color = Tokens.Colors.chipStroke,
                            shape = RoundedCornerShape((24f * s).dp)
                        )
                        .pointerInput(Unit) { detectTapGestures { onCityChipTap() } },
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        city,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AbysFonts.inter,
                            fontSize   = chipSize,
                            fontStyle  = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color      = Tokens.Colors.text,
                            shadow     = Shadow(
                                Tokens.Colors.tickDark.copy(alpha = 0.35f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                AnimatedContent(
                    targetState = pickerVisible,
                    transitionSpec = { fadeIn(tween(220)) with fadeOut(tween(180)) }
                ) { showPicker ->
                    if (showPicker) {
                        CityPickerWheel(
                            cities = cities,
                            currentCity = city,
                            onChosen = onCityChosen,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = (72f * sx).dp,
                                    vertical = (120f * sy).dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            HadithFrame(
                                text = hadith,
                                modifier = Modifier
                                    .fillMaxWidth(0.74f)
                                    .defaultMinSize(minHeight = (220f * sy).dp)
                            )
                        }
                    }
                }
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
                        fontSize   = textSize,
                        fontWeight = FontWeight.Bold,
                        color      = Tokens.Colors.text,
                        lineHeight = 1.42.em,
                        textAlign = TextAlign.Start,
                        shadow     = Shadow(
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
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hadith-shift"
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
