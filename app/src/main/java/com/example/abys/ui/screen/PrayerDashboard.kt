@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abys.R
import com.example.abys.logic.NightIntervals
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.ui.util.backdropBlur

internal object GlassDefaults {
    val top: Color
        @Composable get() = Tokens.Colors.overlayTop.copy(alpha = 0.9f)
    val bottom: Color
        @Composable get() = Tokens.Colors.overlayCard.copy(alpha = 0.86f)
    val stroke: Color
        @Composable get() = Color.White.copy(alpha = 0.32f)
    val glow: Color
        @Composable get() = Color.White.copy(alpha = 0.18f)
    val blur
        @Composable get() = (10f * Dimens.s()).dp
    val elevation
        @Composable get() = (24f * Dimens.s()).dp
    val bgScrim = Color.Black.copy(alpha = 0.25f)
}

internal object TypeTone {
    val primary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.94f)
    val secondary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.72f)
    val divider: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.18f)
    val tick: Color
        @Composable get() = Tokens.Colors.tickFull
    val indicator: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.32f)
}

private fun scaledSp(basePx: Int, scale: Float) = (basePx * scale).sp

internal object TypeScale {
    val city = scaledSp(Tokens.TypographyPx.city, 0.68f)
    val timeNow = scaledSp(Tokens.TypographyPx.timeNow, 0.68f)
    val prayerTime = scaledSp(Tokens.TypographyPx.label, 0.62f)
    val prayerName = scaledSp(Tokens.TypographyPx.label, 0.54f)
    val subLabel = scaledSp(Tokens.TypographyPx.subLabel, 0.48f)
    val timeline = scaledSp(Tokens.TypographyPx.timeline, 0.5f)
}

private fun TextUnit.lineHeight(multiplier: Float) = (value * multiplier).sp

internal object TypeLeading {
    val city = TypeScale.city.lineHeight(1.18f)
    val timeNow = TypeScale.timeNow.lineHeight(1.18f)
    val prayerTime = TypeScale.prayerTime.lineHeight(1.26f)
    val prayerName = TypeScale.prayerName.lineHeight(1.26f)
    val subLabel = TypeScale.subLabel.lineHeight(1.24f)
    val timeline = TypeScale.timeline.lineHeight(1.22f)
}

private const val EMPTY_TIME_PLACEHOLDER = "—:—"

private fun sanitizeTime(value: String): String {
    val trimmed = value.trim()
    return trimmed.takeIf { it.any(Char::isDigit) } ?: EMPTY_TIME_PLACEHOLDER
}

private const val TABULAR_FEATURE = "'tnum'"

private val TabularFeatureStyle = TextStyle(fontFeatureSettings = TABULAR_FEATURE)

@Composable
internal fun TabularText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        style = LocalTextStyle.current.merge(TabularFeatureStyle)
    )
}

@Composable
internal fun ThinDivider(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        color = TypeTone.divider,
        thickness = 1.dp
    )
}

@Composable
internal fun PrayerDashboard(
    city: String,
    now: String,
    prayerTimes: Map<String, String>,
    thirds: NightIntervals,
    onCityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sy = Dimens.sy()
    val sx = Dimens.sx()
    val shape = RoundedCornerShape(Tokens.Radii.card())
    val cardHorizontalPad = Dimens.scaledX(R.dimen.abys_card_pad_h)
    val cardTopPad = Dimens.scaledY(R.dimen.abys_card_pad_top)
    val cardBottomPad = Dimens.scaledY(R.dimen.abys_card_pad_bottom)
    val sectionSpacing = (36f * sy).dp
    val headerSpacing = (32f * sy).dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(headerSpacing)
    ) {
        CityHeaderPill(
            city = city,
            now = now,
            onTap = onCityClick,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            Modifier
                .fillMaxWidth()
                .shadow(elevation = GlassDefaults.elevation, shape = shape, clip = false)
                .clip(shape)
                .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha }
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .backdropBlur(GlassDefaults.blur)
                    .background(Brush.verticalGradient(listOf(GlassDefaults.top, GlassDefaults.bottom)))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(0f to GlassDefaults.stroke, 1f to GlassDefaults.glow),
                        shape = shape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = cardHorizontalPad,
                        end = cardHorizontalPad,
                        top = cardTopPad,
                        bottom = cardBottomPad
                    ),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                PrayerSchedule(
                    times = prayerTimes,
                    modifier = Modifier.fillMaxWidth()
                )

                ThinDivider()

                NightTimeline(
                    thirds = thirds,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CityHeaderPill(city: String, now: String, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val sy = Dimens.sy()
    val sx = Dimens.sx()
    val shape = RoundedCornerShape(Tokens.Radii.pill())
    val padHorizontal = Dimens.scaledX(R.dimen.abys_pill_pad_h)
    val padVertical = Dimens.scaledY(R.dimen.abys_pill_pad_v)
    val underlineThickness = (3f * sy).dp
    val underlineOffset = (2f * sy).dp

    Box(
        modifier
            .semantics {
                contentDescription = "Открыть выбор города"
                role = Role.Button
            }
            .clip(shape)
            .backdropBlur(GlassDefaults.blur)
            .background(Brush.verticalGradient(listOf(GlassDefaults.top, GlassDefaults.bottom)))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(0f to GlassDefaults.stroke, 1f to GlassDefaults.glow),
                shape = shape
            )
            .clickable(onClick = onTap)
            .padding(horizontal = padHorizontal, vertical = padVertical)
    ) {
        val primaryColor = TypeTone.primary

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val cityText = city.ifBlank { "—" }
            Text(
                text = cityText,
                fontSize = TypeScale.city,
                lineHeight = TypeLeading.city,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = primaryColor,
                fontFamily = AbysFonts.inter,
                modifier = Modifier
                    .weight(1f)
                    .drawBehind {
                        if (cityText.isNotBlank()) {
                            val thickness = underlineThickness.toPx()
                            val offset = underlineOffset.toPx()
                            drawRect(
                                color = primaryColor,
                                topLeft = Offset(0f, size.height + offset - thickness),
                                size = Size(size.width, thickness)
                            )
                        }
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width((20f * sx).dp))

                TabularText(
                    text = sanitizeTime(now),
                    fontSize = TypeScale.timeNow,
                    lineHeight = TypeLeading.timeNow,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Right,
                    fontFamily = AbysFonts.inter,
                    modifier = Modifier.widthIn(min = 0.dp)
                )
        }
    }
}

@Composable
private fun PrayerSchedule(times: Map<String, String>, modifier: Modifier = Modifier) {
    val rowSpacing = Dimens.scaledY(R.dimen.abys_row_step)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        PrayerRow(label = "Фаджр", value = times["Fajr"].orEmpty())
        PrayerRow(label = "Восход", value = times["Sunrise"].orEmpty())
        PrayerRow(label = "Зухр", value = times["Dhuhr"].orEmpty())
        AsrSection(
            standard = times["AsrStd"].orEmpty(),
            hanafi = times["AsrHana"].orEmpty()
        )
        PrayerRow(label = "Магриб", value = times["Maghrib"].orEmpty())
        PrayerRow(label = "Иша", value = times["Isha"].orEmpty())
    }
}

@Composable
private fun PrayerRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = TypeScale.prayerName,
            lineHeight = TypeLeading.prayerName,
            fontWeight = FontWeight.Medium,
            color = TypeTone.primary,
            fontFamily = AbysFonts.inter,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        TabularText(
            text = sanitizeTime(value),
            fontSize = TypeScale.prayerTime,
            lineHeight = TypeLeading.prayerTime,
            fontWeight = FontWeight.Bold,
            color = TypeTone.primary,
            textAlign = TextAlign.Right,
            fontFamily = AbysFonts.inter,
            modifier = Modifier.widthIn(min = 0.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun AsrSection(standard: String, hanafi: String) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val subSpacing = (18f * sy).dp
    val indicatorWidth = (64f * sx).dp
    val indicatorHeight = (4f * sy).dp
    val indicatorShape = RoundedCornerShape((2f * Dimens.s()).dp)
    val indicatorSpacing = (16f * sx).dp

    Column(verticalArrangement = Arrangement.spacedBy(subSpacing)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Аср",
                fontSize = TypeScale.prayerName,
                lineHeight = TypeLeading.prayerName,
                fontWeight = FontWeight.Medium,
                color = TypeTone.primary,
                fontFamily = AbysFonts.inter,
                modifier = Modifier.weight(1f)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy((12f * sy).dp)) {
            AsrSubRow(
                label = "стандарт",
                value = standard,
                indicatorWidth = indicatorWidth,
                indicatorHeight = indicatorHeight,
                indicatorShape = indicatorShape,
                indicatorSpacing = indicatorSpacing
            )
            AsrSubRow(
                label = "ханафитский",
                value = hanafi,
                indicatorWidth = indicatorWidth,
                indicatorHeight = indicatorHeight,
                indicatorShape = indicatorShape,
                indicatorSpacing = indicatorSpacing
            )
        }
    }
}

@Composable
private fun AsrSubRow(
    label: String,
    value: String,
    indicatorWidth: androidx.compose.ui.unit.Dp,
    indicatorHeight: androidx.compose.ui.unit.Dp,
    indicatorShape: RoundedCornerShape,
    indicatorSpacing: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = TypeScale.subLabel,
            lineHeight = TypeLeading.subLabel,
            fontWeight = FontWeight.Medium,
            color = TypeTone.secondary,
            fontFamily = AbysFonts.inter,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(indicatorHeight)
                .clip(indicatorShape)
                .background(TypeTone.indicator)
        )

        Spacer(Modifier.width(indicatorSpacing))

        TabularText(
            text = sanitizeTime(value),
            fontSize = TypeScale.subLabel,
            lineHeight = TypeLeading.subLabel,
            fontWeight = FontWeight.Bold,
            color = TypeTone.primary,
            textAlign = TextAlign.Right,
            fontFamily = AbysFonts.inter,
            modifier = Modifier.widthIn(min = 0.dp)
        )
    }
}

@Composable
private fun NightTimeline(thirds: NightIntervals, modifier: Modifier = Modifier) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val tickWidth = (3f * sx).dp
    val sideHeight = (49f * sy).dp
    val centreHeight = (57f * sy).dp
    val tickSpacing = (6f * sx).dp
    val labelSpacing = (16f * sy).dp

    val labels = listOf(
        sanitizeTime(thirds.first.first),
        sanitizeTime(thirds.second.first),
        sanitizeTime(thirds.third.first)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        labels.forEach { label ->
            NightTimelineGroup(
                label = label,
                tickWidth = tickWidth,
                sideHeight = sideHeight,
                centreHeight = centreHeight,
                tickSpacing = tickSpacing,
                labelSpacing = labelSpacing
            )
        }
    }
}

@Composable
private fun NightTimelineGroup(
    label: String,
    tickWidth: androidx.compose.ui.unit.Dp,
    sideHeight: androidx.compose.ui.unit.Dp,
    centreHeight: androidx.compose.ui.unit.Dp,
    tickSpacing: androidx.compose.ui.unit.Dp,
    labelSpacing: androidx.compose.ui.unit.Dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(tickSpacing)
        ) {
            TimelineTick(width = tickWidth, height = sideHeight)
            TimelineTick(width = tickWidth, height = centreHeight)
            TimelineTick(width = tickWidth, height = sideHeight)
        }

        Spacer(Modifier.height(labelSpacing))

        TabularText(
            text = label,
            fontSize = TypeScale.timeline,
            lineHeight = TypeLeading.timeline,
            fontWeight = FontWeight.Medium,
            color = TypeTone.primary,
            textAlign = TextAlign.Center,
            fontFamily = AbysFonts.inter
        )
    }
}

@Composable
private fun TimelineTick(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    val shape = RoundedCornerShape((Dimens.s() * 1.5f).dp)
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(TypeTone.tick)
    )
}
