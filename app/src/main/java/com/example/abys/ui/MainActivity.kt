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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.abys.R
import com.example.abys.logic.MainViewModel
import com.example.abys.logic.SettingsStore
import com.example.abys.logic.TimeHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import java.time.Duration

// Compose импорты
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ComposeView
import com.example.abys.ui.background.SlideshowBackground
import com.example.abys.ui.components.GlassCard
import com.example.abys.ui.components.NightTimeline
import com.example.abys.ui.components.SeasonalParticles
import com.example.abys.ui.components.PrayerBoard
import com.example.abys.ui.components.TopOverlay

// Импорт для BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel
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

        tvDate.text = TimeHelper.todayHuman()

        vm.city.observe(this) { tvCity.text = it ?: "—" }

        vm.timings.observe(this) { t ->
            val sel = vm.school.value ?: 0
            renderTimings(t?.toDisplayList(sel).orEmpty(), t?.nextPrayer(sel)?.first)
            val next = t?.nextPrayer(sel)
            tvNextPrayer.text = next?.let { "Следующий намаз — ${it.first} в ${it.second}" } ?: "—"
            startTicker(next?.second, t?.tz)
        }
        vm.school.observe(this) { s ->
            when (s) {
                0 -> toggleAsr.check(btnAsrStd.id)
                else -> toggleAsr.check(btnAsrHan.id)
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
        requestLocationPermissionOrLoad()

        // --- ComposeView интеграция ---
        val composeBg = findViewById<ComposeView>(R.id.composeBg)
        composeBg.setContent {
            SlideshowBackground()
        }

        val composeParticles = findViewById<ComposeView>(R.id.composeParticles)
        composeParticles.setContent { SeasonalParticles() }

        val composeTop = findViewById<ComposeView>(R.id.composeTop)
        composeTop.setContent {
            val city by vm.city.observeAsState()
            val hijri by vm.hijri.observeAsState()
            MaterialTheme {
                TopOverlay(city = city, hijri = hijri)
            }
        }

        val composeHero = findViewById<ComposeView>(R.id.composeHero)
        composeHero.setContent {
            val t by vm.timings.observeAsState()
            val sel by vm.school.observeAsState(0)
            MaterialTheme {
                if (t != null) {
                    PrayerBoard(t = t!!, selectedSchool = sel)
                } else {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .heightIn(min = 180.dp),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Text("—", color = Color.White)
                    }
                }
            }
        }

        val composeNight = findViewById<ComposeView>(R.id.composeNight)
        composeNight.setContent {
            val t by vm.timings.observeAsState()
            t?.let { NightTimeline(maghrib = it.maghrib, fajr = it.fajr, zone = it.tz) }
        }
        // --- конец блока ComposeView ---
    }

    private fun requestLocationPermissionOrLoad() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            vm.loadByLocation(this)
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        }
    }

    private fun showManualCityDialog() {
        val sheet = BottomSheetDialog(this)
        val cv = ComposeView(this)
        cv.setContent {
            MaterialTheme {
                com.example.abys.ui.city.CityPickerSheet { picked ->
                    lifecycleScope.launchWhenStarted {
                        SettingsStore.setCity(this@MainActivity, picked)
                    }
                    vm.loadByCity(picked)
                    sheet.dismiss()
                }
            }
        }
        sheet.setContentView(cv)
        sheet.show()
    }

    private fun renderTimings(items: List<Pair<String, String>>, nextName: String?) {
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
        listContainer.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun startTicker(nextTime: String?, zoneId: java.time.ZoneId?) {
        stopTicker()
        if (nextTime == null || zoneId == null) {
            tvCountdown.text = "До начала: —"
            return
        }
        ticker = object : Runnable {
            override fun run() {
                val d: Duration? = TimeHelper.untilNowTo(nextTime, zoneId)
                tvCountdown.text = d?.let {
                    val h = it.toHours()
                    val m = (it.toMinutes() % 60)
                    val s = (it.seconds % 60)
                    "До начала: %02d:%02d:%02d".format(h, m, s)
                } ?: "До начала: —"
                uiHandler.postDelayed(this, 1000)
            }
        }
        uiHandler.post(ticker!!)
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
