package com.met.vetero.presentation

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
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
import org.koin.android.viewmodel.ext.android.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val vm: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initErrorObserver()
        initWeatherObserver()
        vm.fetchWeatherForecast("50.923337", "20.768535", this.getString(R.string.api_key))

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

    private fun updateUi(weather : WeatherResponse){
        city_name.text = weather.city.name
        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${weather.forecastList[0].weather[0].iconId}@2x.png")
            .into(icon_weather)
    }

}
