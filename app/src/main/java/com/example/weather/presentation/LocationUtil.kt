package com.example.weather.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.example.weather.data.WeatherApi
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class LocationUtil {

    val dataLoaded = mutableStateOf(false)
    val data = mutableStateOf(AppCardData(weatherDescription = "", time = "", name = "", temp = 0.0))

    fun createLocationRequest(context: Context,fusedLocationClient: FusedLocationProviderClient) {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }


        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    CoroutineScope(Dispatchers.IO).launch {
                        val weatherDTO = WeatherApi
                            .apiInstance
                            .getWeatherDetails(location.latitude,
                                location.longitude,
                                context.resources.getString(com.example.weather.R.string.open_weather_api_key))
                        dataLoaded.value = true;
                        data.value = AppCardData(name = weatherDTO.name,
                            time = "${(weatherDTO.main.temp - 273).roundToInt()}°C",
                            weatherDescription = weatherDTO.weather[0].description,
                            temp = (weatherDTO.main.temp - 273).roundToInt().toDouble()
                        )
                    }
                }
            }
        }, Looper.getMainLooper())
    }
}