@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.em
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens

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
    val backgroundColor = Tokens.Colors.glassLight

    Box(
        modifier
            .padding((28f * sx).dp, (28f * sy).dp)
            .fillMaxSize()
            .clip(RoundedCornerShape((32f * s).dp))
            .background(backgroundColor)
            .blur(8.dp)
    ) {
        Box(
            Modifier
                .padding(horizontal = (56f * sx).dp, vertical = (64f * sy).dp)
                .height((64f * s).dp)
                .fillMaxWidth()
                .border(
                    width = 3.dp,
                    color = Color.White.copy(alpha = 0.36f),
                    shape = RoundedCornerShape((24f * s).dp)
                )
                .pointerInput(Unit) { detectTapGestures { onCityChipTap() } },
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                city,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = (36f * s).sp,
                    fontStyle  = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color      = Tokens.Colors.text,
                    shadow     = Shadow(Color.Black.copy(alpha = 0.35f), offset = Offset(0f, 2f), blurRadius = 4f)
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
                    cities      = cities,
                    currentCity = city,
                    onChosen    = onCityChosen,
                    modifier    = Modifier.fillMaxSize()
                )
            } else {
                HadithFrame(
                    text = hadith,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start  = (100f * sx).dp,
                            end    = (100f * sx).dp,
                            top    = (292f * sy).dp,
                            bottom = (120f * sy).dp
                        )
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
    val shape = RoundedCornerShape(64.dp)
    Box(
        modifier
            .clip(shape)
            .border(5.dp, Color.Black.copy(alpha = 0.85f), shape)
            .padding(horizontal = 36.dp, vertical = 32.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
            BasicText(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = Tokens.TypographySp.timeline,
                    fontWeight = FontWeight.Bold,
                    color      = Tokens.Colors.text,
                    lineHeight = 1.42.em,
                    shadow     = Shadow(Color.Black.copy(alpha = 0.35f), offset = Offset(0f, 2f), blurRadius = 6f)
                ),
                textAlign = TextAlign.Start
            )
        }
    }
}
