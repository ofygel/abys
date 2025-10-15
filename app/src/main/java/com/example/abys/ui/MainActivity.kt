package com.example.abys.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.abys.R
import com.example.abys.data.FallbackContent
import com.example.abys.logic.CitySearchViewModel
import com.example.abys.logic.MainViewModel
import com.example.abys.logic.SettingsStore
import com.example.abys.logic.TimeHelper
import com.example.abys.ui.background.SlideshowBackground
import com.example.abys.ui.city.CityPickerSheet
import com.example.abys.ui.components.NightTimeline
import com.example.abys.ui.components.PrayerBoard
import com.example.abys.ui.components.TopOverlay
import com.example.abys.ui.effects.SeasonalParticles
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.time.Duration

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel
    private lateinit var cityVm: CitySearchViewModel
    private lateinit var tvCity: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvNextPrayer: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var listContainer: LinearLayout
    private lateinit var btnRequestLocation: MaterialButton
    private lateinit var btnManualCity: MaterialButton
    private lateinit var toggleAsr: MaterialButtonToggleGroup
    private lateinit var btnAsrStd: MaterialButton
    private lateinit var btnAsrHan: MaterialButton

    private val uiHandler = Handler(Looper.getMainLooper())
    private var ticker: Runnable? = null
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            vm.loadByLocation(this)
        } else {
            showManualCityDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this)[MainViewModel::class.java]
        cityVm = ViewModelProvider(this)[CitySearchViewModel::class.java]

        tvCity = findViewById(R.id.tvCity)
        tvDate = findViewById(R.id.tvDate)
        tvNextPrayer = findViewById(R.id.tvNextPrayer)
        tvCountdown = findViewById(R.id.tvCountdown)
        listContainer = findViewById(R.id.listContainer)
        btnRequestLocation = findViewById(R.id.btnRequestLocation)
        btnManualCity = findViewById(R.id.btnManualCity)
        toggleAsr = findViewById(R.id.toggleAsr)
        btnAsrStd = findViewById(R.id.btnAsrStd)
        btnAsrHan = findViewById(R.id.btnAsrHan)

        val fallbackDate = FallbackContent.prayerTimes.readableDate ?: TimeHelper.todayHuman()
        val initialSchool = vm.school.value ?: 0
        val initialNext = FallbackContent.nextPrayer(initialSchool)
        tvCity.text = FallbackContent.cityLabel
        tvDate.text = fallbackDate
        tvNextPrayer.text = initialNext?.let {
            getString(R.string.next_prayer_time_format, it.first, it.second)
        } ?: ""
        startTicker(initialNext?.second, FallbackContent.uiTimings.tz)
        renderTimings(
            FallbackContent.uiTimings.toDisplayList(initialSchool),
            initialNext?.first,
            fallback = true
        )

        vm.city.observe(this) { tvCity.text = it ?: FallbackContent.cityLabel }

        vm.timings.observe(this) { t ->
            val sel = vm.school.value ?: 0
            val displayTimings = t?.toDisplayList(sel) ?: FallbackContent.uiTimings.toDisplayList(sel)
            val fallbackNext = FallbackContent.nextPrayer(sel)
            val next = t?.nextPrayer(sel) ?: fallbackNext
            renderTimings(displayTimings, next?.first, fallback = t == null)
            tvNextPrayer.text = next?.let {
                getString(R.string.next_prayer_time_format, it.first, it.second)
            } ?: fallbackNext?.let {
                getString(R.string.next_prayer_time_format, it.first, it.second)
            } ?: ""
            tvDate.text = TimeHelper.todayHuman()
            val zone = t?.tz ?: FallbackContent.uiTimings.tz
            startTicker(next?.second ?: fallbackNext?.second, zone)
        vm.city.observe(this) { tvCity.text = it ?: getString(R.string.placeholder_dash) }

        vm.timings.observe(this) { t ->
            val sel = vm.school.value ?: 0
            renderTimings(t?.toDisplayList(sel).orEmpty(), t?.nextPrayer(sel)?.first)
            val next = t?.nextPrayer(sel)
            tvNextPrayer.text = next?.let {
                getString(R.string.next_prayer_time_format, it.first, it.second)
            } ?: getString(R.string.next_prayer_placeholder)
            startTicker(next?.second, t?.tz)
        }
        vm.school.observe(this) { s ->
            when (s) {
                0 -> toggleAsr.check(btnAsrStd.id)
                else -> toggleAsr.check(btnAsrHan.id)
            }
            if (vm.timings.value == null) {
                val demoNext = FallbackContent.nextPrayer(s)
                renderTimings(
                    FallbackContent.uiTimings.toDisplayList(s),
                    demoNext?.first,
                    fallback = true
                )
                tvNextPrayer.text = demoNext?.let {
                    getString(R.string.next_prayer_time_format, it.first, it.second)
                } ?: tvNextPrayer.text
                startTicker(demoNext?.second, FallbackContent.uiTimings.tz)
            }
        }

        toggleAsr.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val s = if (checkedId == btnAsrHan.id) 1 else 0
            vm.setSchool(s, reload = true, ctx = this)
        }

        btnRequestLocation.setOnClickListener { requestLocationPermissionOrLoad() }
        btnManualCity.setOnClickListener { showManualCityDialog() }

        // Поднимем сохранённую школу и попробуем авто-загрузку
        vm.loadSavedSchool(this)

        // --- ComposeView интеграция ---
        val composeBg = findViewById<ComposeView>(R.id.composeBg)
        composeBg.setContent {
            SlideshowBackground()
        }

        val composeParticles = findViewById<ComposeView>(R.id.composeParticles)
        composeParticles.setContent { SeasonalParticles() }

        val composeTop = findViewById<ComposeView>(R.id.composeTop)
        composeTop.setContent {
            val city by vm.city.observeAsState(FallbackContent.cityLabel)
            val hijri by vm.hijri.observeAsState(FallbackContent.hijriLabel)
            MaterialTheme {
                TopOverlay(city = city, hijri = hijri)
            }
        }

        val composeHero = findViewById<ComposeView>(R.id.composeHero)
        composeHero.setContent {
            val t by vm.timings.observeAsState()
            val sel by vm.school.observeAsState(0)
            MaterialTheme {
                val source = t ?: FallbackContent.uiTimings
                PrayerBoard(source, selectedSchool = sel)
                if (t != null) {
                    PrayerBoard(t!!, selectedSchool = sel)
                } else {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .heightIn(min = 180.dp),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Text(stringResource(R.string.placeholder_dash), color = Color.White)
                    }
                }
            }
        }

        val composeNight = findViewById<ComposeView>(R.id.composeNight)
        composeNight.setContent {
            val t by vm.timings.observeAsState()
            val source = t ?: FallbackContent.uiTimings
            NightTimeline(maghrib = source.maghrib, fajr = source.fajr, zone = source.tz)
        }
        // --- конец блока ComposeView ---
    }

    private fun requestLocationPermissionOrLoad() {
        if (hasLocationPermission()) {
            vm.loadByLocation(this)
        } else {
            launchLocationPermissionRequest()
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun launchLocationPermissionRequest() {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }

    private fun showManualCityDialog() {
        val sheet = BottomSheetDialog(this)
        val cv = ComposeView(this)
        cv.setContent {
            MaterialTheme {
                CityPickerSheet(onPick = { picked ->
                    lifecycleScope.launch {
                        SettingsStore.setCity(this@MainActivity, picked.title)
                        SettingsStore.setLastCoordinates(this@MainActivity, picked.latitude, picked.longitude)
                    }
                    vm.loadByCity(picked.title)
                    sheet.dismiss()
                }, vm = cityVm)
            }
        }
        sheet.setContentView(cv)
        sheet.show()
    }

    private fun renderTimings(items: List<Pair<String, String>>, nextName: String?, fallback: Boolean) {
        listContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        items.forEach { (name, time) ->
            val card = inflater.inflate(R.layout.item_prayer_card, listContainer, false) as MaterialCardView
            val tvName = card.findViewById<TextView>(R.id.tvName)
            val tvTime = card.findViewById<TextView>(R.id.tvTime)
            tvName.text = name
            tvTime.text = time
            if (name == nextName) {
                card.isChecked = true
                card.strokeWidth = (2 * resources.displayMetrics.density).toInt()
            }
            listContainer.addView(card)
        }
        if (fallback) {
            FallbackContent.actionTips.forEach { tip ->
                val tipCard = inflater.inflate(R.layout.item_action_tip, listContainer, false) as MaterialCardView
                val tvTitle = tipCard.findViewById<TextView>(R.id.tvTipTitle)
                val tvBody = tipCard.findViewById<TextView>(R.id.tvTipDescription)
                val btn = tipCard.findViewById<MaterialButton>(R.id.btnTip)
                tvTitle.text = tip.title
                tvBody.text = tip.description
                btn.text = tip.cta
                btn.setOnClickListener {
                    when (tip.action) {
                        FallbackContent.TipAction.LOCATION -> requestLocationPermissionOrLoad()
                        FallbackContent.TipAction.CITY -> showManualCityDialog()
                        FallbackContent.TipAction.REMINDER -> Toast.makeText(
                            this,
                            R.string.fallback_reminder_toast,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                listContainer.addView(tipCard)
            }

            val inspirationCard = inflater.inflate(R.layout.item_inspiration_card, listContainer, false) as MaterialCardView
            val container = inspirationCard.findViewById<LinearLayout>(R.id.tipContainer)
            FallbackContent.inspiration.forEach { line ->
                val tv = inflater.inflate(R.layout.view_inspiration_row, container, false) as TextView
                tv.text = line
                container.addView(tv)
            }
            listContainer.addView(inspirationCard)
        }
        listContainer.visibility = if (items.isEmpty() && !fallback) View.GONE else View.VISIBLE
    }

    private fun startTicker(nextTime: String?, zoneId: java.time.ZoneId?) {
        stopTicker()
        val fallbackTime = FallbackContent.nextPrayer(vm.school.value ?: 0)?.second
            ?: FallbackContent.uiTimings.dhuhr
        val targetTime = nextTime ?: fallbackTime
        val targetZone = zoneId ?: FallbackContent.uiTimings.tz
        fun formatDuration(d: Duration?): String = d?.let {
            val h = it.toHours()
            val m = (it.toMinutes() % 60)
            val s = (it.seconds % 60)
            getString(R.string.countdown_time_format, h, m, s)
        } ?: getString(R.string.countdown_time_format, 0, 0, 0)

        tvCountdown.text = formatDuration(TimeHelper.untilNowTo(targetTime, targetZone))
        ticker = object : Runnable {
            override fun run() {
                tvCountdown.text = formatDuration(TimeHelper.untilNowTo(targetTime, targetZone))
        if (nextTime == null || zoneId == null) {
            tvCountdown.text = getString(R.string.countdown_placeholder)
            return
        }
        ticker = object : Runnable {
            override fun run() {
                val d: Duration? = TimeHelper.untilNowTo(nextTime, zoneId)
                tvCountdown.text = d?.let {
                    val h = it.toHours()
                    val m = (it.toMinutes() % 60)
                    val s = (it.seconds % 60)
                    getString(R.string.countdown_time_format, h, m, s)
                } ?: getString(R.string.countdown_placeholder)
                uiHandler.postDelayed(this, 1000)
            }
        }
        uiHandler.postDelayed(ticker!!, 1000)
    }

    private fun stopTicker() {
        ticker?.let { uiHandler.removeCallbacks(it) }
        ticker = null
    }

    override fun onDestroy() {
        stopTicker()
        super.onDestroy()
    }
}