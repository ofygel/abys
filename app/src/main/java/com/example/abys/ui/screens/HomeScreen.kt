package com.example.abys.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.abys.R
import com.example.abys.logic.CitySearchViewModel
import com.example.abys.logic.SettingsStore
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import com.example.abys.ui.PrayerViewModel
import com.example.abys.ui.city.CityPickerSheet
import com.example.abys.ui.components.DayProgress
import com.example.abys.ui.components.EffectCarousel
import com.example.abys.ui.components.GlassCard
import com.example.abys.ui.components.InfoPill
import com.example.abys.ui.components.NextPrayerChip
import com.example.abys.ui.components.NightTimeline
import com.example.abys.ui.components.PrayerTable
import com.example.abys.ui.effects.EffectLayer
import com.example.abys.ui.effects.LocalWind
import com.example.abys.ui.effects.WindState
import com.example.abys.ui.effects.ProvideWind
import com.example.abys.ui.effects.ThemeSpec
import com.example.abys.ui.effects.THEMES
import com.example.abys.ui.effects.themeById
import com.example.abys.ui.effects.windJitter
import com.example.abys.ui.effects.windParallax
import com.example.abys.ui.effects.windSway
import com.example.abys.ui.background.SlideshowBackground
import com.example.abys.util.LocationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.time.Duration
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val citySearchVm: CitySearchViewModel = viewModel()

    var hasGpsPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        hasGpsPerm = granted
    }

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        val savedCoords = SettingsStore.getLastCoordinates(context)
        val savedCity = SettingsStore.getCity(context)
        if (savedCoords != null && state.timings == null) {
            viewModel.load(savedCoords.first, savedCoords.second, savedCity)
        }
        if (!hasGpsPerm) {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    var showCitySheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showCitySheet) {
        if (!showCitySheet) {
            citySearchVm.search("")
        }
    }

    LaunchedEffect(hasGpsPerm) {
        if (hasGpsPerm) {
            val loc = LocationHelper.getLastBestLocation(context)
            if (loc != null) {
                val label = context.getString(R.string.location_current)
                viewModel.load(loc.first, loc.second, label)
                coroutineScope.launch {
                    SettingsStore.setLastCoordinates(context, loc.first, loc.second)
                    SettingsStore.setCity(context, label)
                }
            } else if (state.timings == null) {
                showCitySheet = true
            }
        } else if (state.timings == null) {
            showCitySheet = true
        }
    }

    val themes = remember { THEMES }
    var appliedTheme by remember { mutableStateOf(themeById("leaves")) }
    var focusedTheme by remember { mutableStateOf<ThemeSpec>(appliedTheme) }
    var carouselCollapsed by remember { mutableStateOf(false) }
    var applyFlash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(if (applyFlash) 0.3f else 0f, label = "applyFlash")

    val savedThemeContext = LocalContext.current
    LaunchedEffect(Unit) {
        val saved = SettingsStore.getThemeId(savedThemeContext)
        if (saved != null) {
            val restored = themeById(saved)
            appliedTheme = restored
            focusedTheme = restored
        }
    }

    LaunchedEffect(applyFlash) {
        if (applyFlash) {
            delay(240)
            applyFlash = false
        }
    }

    val backgrounds = appliedTheme.backgrounds.ifEmpty { STATIC_BACKGROUNDS }

    ProvideWind(theme = appliedTheme) {
        val wind = LocalWind.current
        val windEnabled = appliedTheme.supportsWindSwayEffective && wind != null

        Box(modifier = Modifier.fillMaxSize()) {
            SlideshowBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .let { base -> wind?.let { base.windParallax(it, depth = -1f) } ?: base },
                images = backgrounds
            )

            EffectLayer(Modifier.fillMaxSize(), theme = appliedTheme)

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.68f)
                        .let { base -> if (windEnabled) wind?.let { base.windSway(it) } ?: base else base }
                ) {
                    HomeContent(
                        state = state,
                        onCityChange = {
                            showCitySheet = true
                        },
                        onToggleSchool = { newSchool ->
                            viewModel.updateSchool(context, newSchool)
                        },
                        remindersState = rememberToggleState(initial = true),
                        countdownState = rememberToggleState(initial = true)
                    )
                }
            }

            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                val applyTheme: (ThemeSpec) -> Unit = { spec ->
                    appliedTheme = spec
                    carouselCollapsed = true
                    focusedTheme = spec
                    applyFlash = true
                    coroutineScope.launch { SettingsStore.setThemeId(savedThemeContext, spec.id) }
                }

                EffectCarousel(
                    themes = themes,
                    selectedThemeId = appliedTheme.id,
                    collapsed = carouselCollapsed,
                    onCollapsedChange = { carouselCollapsed = it },
                    onThemeSnapped = { focusedTheme = it },
                    onDoubleTapApply = applyTheme,
                    onTapCenterApply = applyTheme
                )
            }

            if (flashAlpha > 0f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flashAlpha))
                )
            }
        }
    }

    if (showCitySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            sheetState = sheetState
        ) {
            CityPickerSheet(
                vm = citySearchVm,
                onPick = { suggestion ->
                    showCitySheet = false
                    viewModel.load(suggestion.latitude, suggestion.longitude, suggestion.title)
                    coroutineScope.launch {
                        SettingsStore.setCity(context, suggestion.title)
                        SettingsStore.setLastCoordinates(context, suggestion.latitude, suggestion.longitude)
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun rememberToggleState(initial: Boolean = false): MutableState<Boolean> = rememberSaveable { mutableStateOf(initial) }

@Composable
private fun HomeContent(
    state: PrayerViewModel.PrayerUiState,
    onCityChange: () -> Unit,
    onToggleSchool: (Int) -> Unit,
    remindersState: MutableState<Boolean>,
    countdownState: MutableState<Boolean>
) {
    val wind = LocalWind.current
    val windEnabled = wind != null
    val timings = state.timings
    val zone = timings?.timezone ?: ZoneId.systemDefault()
    val nextPrayer = timings?.next(school = state.selectedSchool)
    val highlightKey = when (nextPrayer?.first) {
        "Sunrise" -> "Shuruq"
        else -> nextPrayer?.first
    }

    val remaining: Duration? by produceState(initialValue = null as Duration?, key1 = nextPrayer?.second, key2 = zone) {
        val target = nextPrayer?.second
        if (target == null) {
            value = null
        } else {
            while (true) {
                value = TimeHelper.untilNowTo(target, zone)
                delay(1000)
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(state = state, onCityChange = onCityChange, wind = wind, windEnabled = windEnabled)

        if (state.errorMessage != null) {
            OutlinedCard(border = ButtonDefaults.outlinedButtonBorder) {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (timings == null) {
            if (state.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = stringResource(id = R.string.prayer_loading_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            AnimatedVisibility(visible = countdownState.value && nextPrayer != null) {
                NextPrayerChip(
                    title = stringResource(R.string.next_prayer_label),
                    name = nextPrayer?.first ?: "",
                    time = nextPrayer?.second ?: "",
                    remaining = remaining
                )
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            AsrSelector(
                selected = state.selectedSchool,
                onToggleSchool = onToggleSchool
            )

            ReminderToggleRow(remindersState = remindersState, countdownState = countdownState)

            val uiTimings = UiTimings(
                fajr = timings.fajr,
                sunrise = timings.sunrise,
                dhuhr = timings.dhuhr,
                asrStd = timings.asrStandard,
                asrHan = timings.asrHanafi,
                maghrib = timings.maghrib,
                isha = timings.isha,
                tz = zone
            )

            Text(
                text = stringResource(id = R.string.prayer_schedule_today),
                style = MaterialTheme.typography.titleMedium
            )

            PrayerTable(
                t = uiTimings,
                selectedSchool = state.selectedSchool,
                highlightKey = highlightKey
            )

            FlowSection(timings = timings)

            DayProgress(
                fajr = timings.fajr,
                sunrise = timings.sunrise,
                dhuhr = timings.dhuhr,
                asr = timings.asr(state.selectedSchool),
                maghrib = timings.maghrib,
                isha = timings.isha,
                zone = zone
            )

            NightTimeline(maghrib = timings.maghrib, fajr = timings.fajr, zone = zone)

            MethodSection(timings = timings)
        }
    }
}

@Composable
private fun HeaderSection(
    state: PrayerViewModel.PrayerUiState,
    onCityChange: () -> Unit,
    wind: WindState?,
    windEnabled: Boolean
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = state.locationLabel ?: stringResource(R.string.location_placeholder),
                style = MaterialTheme.typography.titleLarge,
                modifier = if (windEnabled && wind != null) Modifier.windJitter(wind, amplitudePx = 0.5f, seed = 1) else Modifier,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val gregorian = state.timings?.readableDate
            val hijri = listOfNotNull(state.timings?.hijriMonth, state.timings?.hijriYear)
                .joinToString(" ").ifBlank { null }
            if (!gregorian.isNullOrBlank()) {
                Text(
                    gregorian,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = if (windEnabled && wind != null) Modifier.windJitter(wind, amplitudePx = 0.35f, seed = 2) else Modifier
                )
            }
            if (!hijri.isNullOrBlank()) {
                Text(
                    hijri,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = if (windEnabled && wind != null) Modifier.windJitter(wind, amplitudePx = 0.35f, seed = 3) else Modifier
                )
            }
        }
        FilledTonalButton(onClick = onCityChange) {
            Text(stringResource(id = R.string.action_change_city))
        }
    }
}

@Composable
private fun AsrSelector(selected: Int, onToggleSchool: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(id = R.string.asr_school_label), style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = selected == 0,
                onClick = { onToggleSchool(0) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(stringResource(id = R.string.asr_school_standard))
            }
            SegmentedButton(
                selected = selected == 1,
                onClick = { onToggleSchool(1) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(stringResource(id = R.string.asr_school_hanafi))
            }
        }
    }
}

@Composable
private fun ReminderToggleRow(
    remindersState: MutableState<Boolean>,
    countdownState: MutableState<Boolean>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(id = R.string.reminder_toggle_label), fontWeight = FontWeight.Medium)
            Switch(
                checked = remindersState.value,
                onCheckedChange = { remindersState.value = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
        AnimatedVisibility(visible = remindersState.value) {
            Text(
                text = stringResource(id = R.string.reminder_enabled),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        }
        AnimatedVisibility(visible = !remindersState.value) {
            Text(
                text = stringResource(id = R.string.reminder_disabled),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(id = R.string.countdown_toggle_label))
            Switch(
                checked = countdownState.value,
                onCheckedChange = { countdownState.value = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowSection(timings: com.example.abys.data.model.PrayerTimes) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(id = R.string.prayer_extras_title), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            timings.imsak?.let {
                InfoPill(title = stringResource(id = R.string.prayer_meta_imsak), value = it, badge = "🌙")
            }
            timings.sunset?.let {
                InfoPill(title = stringResource(id = R.string.prayer_meta_sunset), value = it, badge = "🌇")
            }
            timings.midnight?.let {
                InfoPill(title = stringResource(id = R.string.prayer_meta_midnight), value = it, badge = "🕛")
            }
            timings.lastThird?.let {
                InfoPill(title = stringResource(id = R.string.prayer_meta_last_third), value = it, badge = "🌌")
            }
            timings.firstThird?.let {
                InfoPill(title = stringResource(id = R.string.prayer_meta_first_third), value = it, badge = "🌠")
            }
        }
    }
}

@Composable
private fun MethodSection(timings: com.example.abys.data.model.PrayerTimes) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = timings.methodName?.let { stringResource(id = R.string.prayer_meta_method, it) }
                ?: stringResource(id = R.string.prayer_meta_method_unknown),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(id = R.string.prayer_meta_timezone, timings.timezone.id.replace('_', ' ')),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val STATIC_BACKGROUNDS = listOf(
    R.drawable.slide_01,
    R.drawable.slide_02,
    R.drawable.slide_03,
    R.drawable.slide_04,
    R.drawable.slide_05,
    R.drawable.slide_06,
    R.drawable.slide_07,
    R.drawable.slide_08
)
