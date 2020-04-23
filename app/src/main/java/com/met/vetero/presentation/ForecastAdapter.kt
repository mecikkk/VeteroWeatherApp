package com.met.vetero.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.met.vetero.R
import com.met.vetero.data.entities.WeatherInfo
import kotlinx.android.synthetic.main.forecast_adapter_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    var forecast: List<WeatherInfo> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ForecastViewHolder = ForecastViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.forecast_adapter_item, parent, false))


    override fun getItemCount(): Int = forecast.size

    override fun onBindViewHolder(
        holder: ForecastViewHolder,
        position: Int
    ) {
        if (position + 1 < forecast.size) holder.bind(forecast[position + 1])
    }


    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @OptIn(ExperimentalStdlibApi::class)
        fun bind(weatherInfo: WeatherInfo) {

            val dateFormat = SimpleDateFormat("E dd MMM (HH:mm)", Locale.getDefault())
            val weekDay = dateFormat.format(weatherInfo.date)

            itemView.temperature.text = String.format("%.0f°C", weatherInfo.temperature.current)
            itemView.day.text = weekDay

            Glide.with(itemView)
                    .load("https://openweathermap.org/img/wn/${weatherInfo.weather[0].iconId}@2x.png")
                    .into(itemView.icon_weather)

            itemView.weather_name.text = weatherInfo.weather[0].description.capitalize(Locale.getDefault())

        }

    }
}

