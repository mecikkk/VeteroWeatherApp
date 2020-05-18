package com.met.vetero.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.met.vetero.R
import com.met.vetero.data.room.City
import kotlinx.android.synthetic.main.search_item.view.*

class SearchAdapter() : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    var cities: MutableList<City> = mutableListOf()
    lateinit var onCityClickListener: OnCityClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        return SearchViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.search_item, parent, false), this)
    }

    override fun getItemCount(): Int = cities.size

    override fun onBindViewHolder(
        holder: SearchViewHolder,
        position: Int
    ) {
        holder.bind(cities[position])
    }


    class SearchViewHolder(
        itemView: View,
        private val adapter: SearchAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val TAG = javaClass.simpleName
        lateinit var currentCity: City

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(city: City) {
            currentCity = city
            itemView.search_item_city_name.text = city.name
            itemView.search_item_country_name.text = city.country
        }

        override fun onClick(v: View) {
            try {
                adapter.onCityClickListener.onCityClick(currentCity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    interface OnCityClickListener {
        fun onCityClick(city: City)
    }
}