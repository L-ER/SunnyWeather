package com.sunnyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    lateinit var placeName: TextView
    lateinit var currentTemp: TextView
    lateinit var currentSky: TextView
    lateinit var currentAQI: TextView
    lateinit var nowLayout: RelativeLayout
    lateinit var forecastLayout: LinearLayout
    lateinit var coldRiskText: TextView
    lateinit var dressingText: TextView
    lateinit var ultravioletText: TextView
    lateinit var carWashingText: TextView
    lateinit var weatherLayout: ScrollView
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var navBtn: Button
    lateinit var drawerLayout: DrawerLayout


    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        navBtn = findViewById(R.id.navBtn)
        drawerLayout = findViewById(R.id.drawerLayout)
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) { }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }

            override fun onDrawerOpened(drawerView: View) { }

            override fun onDrawerClosed(drawerView: View) {
                val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            }
        })
        val decorView = window.decorView
        swipeRefresh = findViewById(R.id.swipeRefresh)
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            }else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing = false
        })
        swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName = findViewById(R.id.placeName)
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp = findViewById(R.id.currentTemp)
        currentTemp.text = currentTempText
        currentSky = findViewById(R.id.currentSky)
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI = findViewById(R.id.currentAQI)
        currentAQI.text = currentPM25Text
        nowLayout = findViewById(R.id.nowLayout)
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        forecastLayout = findViewById(R.id.forecastLayout)
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dataInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        val lifeIndex = daily.lifeIndex
        coldRiskText = findViewById(R.id.coldRiskText)
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText = findViewById(R.id.dressingText)
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText = findViewById(R.id.ultravioletText)
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText = findViewById(R.id.carWashingText)
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout = findViewById(R.id.weatherLayout)
        weatherLayout.visibility = View.VISIBLE
    }
}

