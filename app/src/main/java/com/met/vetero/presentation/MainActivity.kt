package com.met.vetero.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.met.vetero.BuildConfig
import com.met.vetero.R
import com.met.vetero.data.api.WeatherResponse
import com.met.vetero.data.entities.City
import com.met.vetero.utils.Const.LOCATION_REQUEST
import com.met.vetero.utils.NetworkConnection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), SearchCityFragment.OnCitySelectListener {

    private val TAG = javaClass.simpleName
    private lateinit var networkConnection: NetworkConnection
    private val vm: MainActivityViewModel by viewModel()
    private val forecastAdapter: ForecastAdapter by inject()
    private val dialogFragment = ConnectionFailedFragment.newInstance()
    private val searchCityFragment : SearchCityFragment by lazy { SearchCityFragment.newInstance(vm) }

    private val noGpsAlert : AlertDialog by lazy { initGpsAlertDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        initErrorObserver()
        initWeatherObserver()
        initGpsAlertDialog()

        main_activity_ready_layout.visibility = View.GONE

        networkConnection = NetworkConnection(this, setNetworkCallback())
        vm.connectedToInternet = networkConnection.forceCheckNetworkState()

        if (!vm.connectedToInternet) dialogFragment.show(supportFragmentManager.beginTransaction(), dialogFragment.tag)
        Log.d(TAG, "Connected to internet : ${vm.connectedToInternet}")

        initRecyclerView()


    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) getLocation()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
    }

    private fun setNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network Available")
            vm.connectedToInternet = true
            if(dialogFragment.isVisible)
                dialogFragment.dismiss()
            checkLocationPermission()
        }

        override fun onLost(network: Network) {
            Log.e(TAG, "Lost connection...")
            vm.connectedToInternet = false

            dialogFragment.show(supportFragmentManager.beginTransaction(), dialogFragment.tag)

        }

        override fun onUnavailable() {
            Log.e(TAG, "No connection available...")
            vm.connectedToInternet = false

            dialogFragment.show(supportFragmentManager.beginTransaction(), dialogFragment.tag)

        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            Log.e(TAG, "Losing connection...")

        }
    }


    private fun getLocation() {
        Log.d(TAG, "Getting localization")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)

            var lastLocation: Location? = null


            providers.forEach {
                val location: Location? = locationManager.getLastKnownLocation(it)

                if (location != null && (lastLocation == null || (location.accuracy < lastLocation!!.accuracy))) lastLocation = location

            }

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                vm.fetchWeatherForecast(lastLocation?.latitude.toString(), lastLocation?.longitude.toString(), BuildConfig.API_KEY)
            else
                showNoGPSInfo()

        } else
            showNoGPSInfo()


    }

    private fun showNoGPSInfo() {
        Log.e(TAG, "Showing no gps info")

        if(!noGpsAlert.isShowing)
            noGpsAlert.show()

    }

    private fun initGpsAlertDialog() : AlertDialog {
        val alertDialog = AlertDialog.Builder(this)
        val alertView = layoutInflater.inflate(R.layout.gps_disabled_alert, null)
        alertDialog.setView(alertView)

        alertView.findViewById<LottieAnimationView>(R.id.lottie_location_anim)
                .playAnimation()

        val noGpsAlert = alertDialog.create()

        searchCityFragment.onCitySelectListener = this
        searchCityFragment.thisActivity = this@MainActivity

        alertView.findViewById<MaterialButton>(R.id.search_city_button)
                .setOnClickListener {
                    searchCityFragment.show(supportFragmentManager, searchCityFragment.tag)
                    noGpsAlert.dismiss()
                }


        noGpsAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        noGpsAlert.setCanceledOnTouchOutside(false)
        noGpsAlert.setOnCancelListener {
            finish()
            System.exit(0)
        }
        return noGpsAlert
    }


    @SuppressLint("ShowToast")
    private fun initErrorObserver() {
        vm.apiError.observe(this, Observer {
            Toast.makeText(this@MainActivity, "Getting forecast error...", Toast.LENGTH_LONG)
                    .show()
            Log.e(this@MainActivity.javaClass.simpleName, "Error : $it")
        })
    }

    private fun initWeatherObserver() {
        vm.weatherResponse.observe(this, Observer {
            updateUi(it)
        })
    }

    private fun initRecyclerView() {
        forecast_recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = forecastAdapter
        }
    }

    private fun updateUi(weather: WeatherResponse) {
        city_name.text = weather.city.name
        temperature.text = String.format("%.0f°C", weather.forecastList[0].temperature.current)
        weather_name.text = weather.forecastList[0].weather[0].description.capitalize()
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

        main_activity_ready_layout.visibility = View.VISIBLE

        main_activity_ready_layout.animate()
                .alpha(1f)
                .setDuration(250)
                .start()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) getLocation()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.find_city_menu_item -> searchCityFragment.show(supportFragmentManager, searchCityFragment.tag)

        }
        return true
    }

    override fun onResume() {
        super.onResume()
        networkConnection.registerCallback()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "CLICKED BACK BUTTON")
        finish()
        System.exit(0)
    }

    override fun onPause() {
        super.onPause()
        //dialogFragment.dismiss()
        //noGpsAlert.dismiss()
        networkConnection.unregisterCallback()
    }

    override fun onCitySelected(city: City) {
        Log.d(TAG, "Received city : ${city.name}")
        main_activity_ready_layout.visibility = View.VISIBLE
        vm.fetchWeatherForecast(city.id, BuildConfig.API_KEY)
        noGpsAlert.dismiss()
        searchCityFragment.dismiss()
    }
}
