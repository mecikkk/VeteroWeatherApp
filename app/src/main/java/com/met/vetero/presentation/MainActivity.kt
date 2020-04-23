package com.met.vetero.presentation

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.met.vetero.R
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.entities.City
import com.met.vetero.utils.*
import com.met.vetero.utils.Const.LOCATION_REQUEST
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

@ExperimentalStdlibApi
class MainActivity : AppCompatActivity(), SearchCityFragment.OnCitySelectListener {

    private lateinit var networkConnection: NetworkConnection
    private val vm: MainActivityViewModel by viewModel()
    private val forecastAdapter: ForecastAdapter by inject()
    private val networkFailedFragment = ConnectionFailedFragment.newInstance()
    private val searchCityFragment: SearchCityFragment by lazy { SearchCityFragment.newInstance() }
    private val locationManager: LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        initErrorObserver()
        initWeatherObserver()
        initGpsAlertDialog()
        initLocationSourceObserver()
        swipeRefreshListener()

        main_activity_ready_layout.gone()
        main_content_progress_bar.visible()

        networkConnection = NetworkConnection(this, setNetworkCallback())
        vm.connectedToInternet = networkConnection.forceCheckNetworkState()

        if (!vm.connectedToInternet) networkFailedFragment.show(supportFragmentManager.beginTransaction(), networkFailedFragment.tag)

        initRecyclerView()

    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
        else registerLocationListener()
    }

    private fun setNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            vm.connectedToInternet = true
            if (networkFailedFragment.isVisible)
                networkFailedFragment.dismiss()

            checkLocationPermission()
        }

        override fun onLost(network: Network) {
            vm.connectedToInternet = false
            networkFailedFragment.show(supportFragmentManager.beginTransaction(), networkFailedFragment.tag)

        }

        override fun onUnavailable() {
            vm.connectedToInternet = false
            networkFailedFragment.show(supportFragmentManager.beginTransaction(), networkFailedFragment.tag)
        }
    }

    private fun locationListener(): LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                MainScope().launch {
                    my_location_image_view.visible()
                }
                vm.fetchWeatherForecast(location.latitude.toString(), location.longitude.toString())
            }
        }

        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?
        ) {

        }

        override fun onProviderEnabled(provider: String?) {
            MainScope().launch { main_content_progress_bar.visible() }
        }

        override fun onProviderDisabled(provider: String?) {
            showSnackbar(R.string.no_access_to_gps)
            MainScope().launch { main_content_progress_bar.gone() }
            getStoredCityOrShowSearchFragment()
        }

    }

    private fun showSnackbar(stringId: Int) {
        val snackbar = Snackbar.make(main_activity_container, getString(stringId), Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
        snackbar.show()
    }

    private fun getStoredCityOrShowSearchFragment() {
        val cityId = vm.getStoredLocation(getPreferences(Context.MODE_PRIVATE))
        if (cityId == -1) showOrHideNoGPSInfo(false)
        else {
            MainScope().launch {
                main_content_progress_bar.visible()
            }
            vm.fetchWeatherForecast(cityId)
        }
    }

    private fun showOrHideNoGPSInfo(hide: Boolean) {

        MainScope().launch {
            if (!hide) {
                gps_disabled_layout.visible()
                main_activity_ready_layout.fadeOut(Runnable {
                    main_activity_ready_layout.gone()
                })
                gps_disabled_layout.fadeIn()
            } else {
                main_activity_ready_layout.visible()
                gps_disabled_layout.fadeOut(Runnable {
                    gps_disabled_layout.gone()
                })
                main_activity_ready_layout.fadeIn()
            }
        }

    }

    private fun initGpsAlertDialog() {
        search_city_button.setOnClickListener {
            searchCityFragment.show(supportFragmentManager, searchCityFragment.tag)
        }
    }


    private fun initLocationSourceObserver(){
        vm.locationFromGps.observe(this, Observer {
            if(it) my_location_image_view.visible()
            else my_location_image_view.gone()
        })
    }

    private fun initErrorObserver() {
        vm.apiError.observe(this, Observer {
            swipe_refresh_layout.isRefreshing = false
            showSnackbar(R.string.getting_forecast_error)
        })
    }

    private fun initWeatherObserver() {
        vm.weatherResponse.observe(this, Observer {
            showOrHideNoGPSInfo(true)
            swipe_refresh_layout.isRefreshing = false
            updateUi(it)
        })
    }

    private fun initRecyclerView() {
        forecast_recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = forecastAdapter
        }
    }

    private fun swipeRefreshListener() {
        swipe_refresh_layout.setOnRefreshListener {
            if (locationManager.isGpsEnabled()) registerLocationListener()
            else swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun updateUi(weather: WeatherResponse) {
        main_content_progress_bar.gone()

        if (networkFailedFragment.isVisible) networkFailedFragment.dismiss()

        city_name.text = weather.city.name
        temperature.text = String.format("%.0f°C", weather.forecastList[0].temperature.current)
        weather_name.text = weather.forecastList[0].weather[0].description.capitalize(Locale.getDefault())
        temp_feels_like.text = String.format("%s : %.0f°C", getString(R.string.feels_like), weather.forecastList[0].temperature.feelsLike)
        pressure.text = String.format("%s : %s hPa", getString(R.string.pressure), weather.forecastList[0].temperature.pressure.toString())
        humidity.text = String.format("%s : %s %%", getString(R.string.humidity), weather.forecastList[0].temperature.humidity.toString())
        cloudiness.text =
            String.format("%s : %s %%", getString(R.string.cloudiness), weather.forecastList[0].clouds.cloudinessLevel.toString())

        Glide.with(this)
                .load("https://openweathermap.org/img/wn/${weather.forecastList[0].weather[0].iconId}@2x.png")
                .into(icon_weather)

        forecastAdapter.forecast = weather.forecastList
        forecastAdapter.notifyDataSetChanged()

        showOrHideNoGPSInfo(true)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(networkFailedFragment.isVisible)
                        networkFailedFragment.dismiss()

                    registerLocationListener()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.find_city_menu_item -> searchCityFragment.show(supportFragmentManager, searchCityFragment.tag)

        }
        return true
    }

    override fun onStart() {
        super.onStart()
        networkConnection.registerCallback()
    }

    private fun registerLocationListener() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10000.0f, locationListener())
        }
    }

    override fun onStop() {
        super.onStop()
        networkConnection.unregisterCallback()
        locationManager.removeUpdates(locationListener())
    }

    override fun onCitySelected(city: City) {
        main_content_progress_bar.visible()
        main_activity_ready_layout.visible()
        vm.fetchWeatherForecast(city.id)
        vm.storeLocationInSharedPreferences(getPreferences(Context.MODE_PRIVATE), city.id)
        showOrHideNoGPSInfo(true)
        searchCityFragment.dismiss()
    }
}
