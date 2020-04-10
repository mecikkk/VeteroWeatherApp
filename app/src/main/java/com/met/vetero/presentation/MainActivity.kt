package com.met.vetero.presentation

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.met.vetero.BuildConfig
import com.met.vetero.R
import com.met.vetero.data.api.OpenWeatherApi
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.entities.WeatherInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val vm: MainActivityViewModel by viewModel()
    private val forecastAdapter : ForecastAdapter by inject()
    private val apiKey : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


        initErrorObserver()
        initWeatherObserver()
        vm.fetchWeatherForecast("50.923337", "20.768535", BuildConfig.API_KEY)
        initRecyclerView()
    }


    @SuppressLint("ShowToast")
    private fun initErrorObserver() {
        vm.error.observe(this, Observer {
            Toast.makeText(this@MainActivity, "Error...", Toast.LENGTH_LONG).show()
            Log.e(this@MainActivity.javaClass.simpleName, "Error : $it")
        })
    }

    private fun initWeatherObserver() {
        vm.weatherResponse.observe(this, Observer {
            Toast.makeText(this@MainActivity, "Success : Forecast for ${it.city.name}", Toast.LENGTH_LONG).show()
            updateUi(it)
        })
    }

    private fun initRecyclerView(){
        forecast_recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = forecastAdapter
        }
    }

    private fun updateUi(weather : WeatherResponse){
        city_name.text = weather.city.name
        temperature.text = String.format("%.0f°C", weather.forecastList[0].temperature.current)
        weather_name.text = weather.forecastList[0].weather[0].description.capitalize()
        temp_feels_like.text = String.format("Odczuwalna : %.0f°C", weather.forecastList[0].temperature.feelsLike)
        pressure.text = "Ciśnienie : " + weather.forecastList[0].temperature.pressure.toString() + "hPa"
        humidity.text = "Wilgotność : " + weather.forecastList[0].temperature.humidity.toString() + "%"
        cloudiness.text = "Zachmurzenie : " + weather.forecastList[0].clouds.cloudinessLevel.toString() + "%"
        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${weather.forecastList[0].weather[0].iconId}@2x.png")
            .into(icon_weather)

        forecastAdapter.forecast = weather.forecastList
        forecastAdapter.notifyDataSetChanged()
    }

}
