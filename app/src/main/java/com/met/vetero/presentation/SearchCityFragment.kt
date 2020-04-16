package com.met.vetero.presentation

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.met.vetero.BuildConfig

import com.met.vetero.R
import com.met.vetero.data.entities.City
import com.met.vetero.utils.SimpleTextWatcher
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search_city.*
import kotlinx.android.synthetic.main.fragment_search_city.view.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject


class SearchCityFragment(val viewModel: MainActivityViewModel) : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(viewModel: MainActivityViewModel) = SearchCityFragment(viewModel)
    }

    private val TAG = javaClass.simpleName
    private lateinit var bottomSheetView : View
    private lateinit var behavior : BottomSheetBehavior<View>
    private val searchAdapter : SearchAdapter by inject()
    lateinit var onCitySelectListener: OnCitySelectListener
    lateinit var thisActivity : Activity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet : BottomSheetDialog =  super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetView = View.inflate(context, R.layout.fragment_search_city, null)

        bottomSheet.setContentView(bottomSheetView)
        behavior = BottomSheetBehavior.from(bottomSheetView.parent as View)

        behavior.peekHeight = ((Resources.getSystem().displayMetrics.heightPixels)/1.2).toInt()

        bottomSheetView.minimumHeight = ((Resources.getSystem().displayMetrics.heightPixels) / 2)
        bottomSheetView.layoutParams.height = (Resources.getSystem().displayMetrics.heightPixels)

        bottomSheetView.progress_bar.visibility = View.GONE

        bottomSheet.search_text_input_layout.requestFocus()
        bottomSheet.search_text_input_edit_text.addTextChangedListener(searchTextWatcher())

        initResponseObserver()
        initCitiesObserver()
        initRecyclerView()
        return bottomSheet
    }


    private fun searchTextWatcher() = object : SimpleTextWatcher() {
        var scope = CoroutineScope(Dispatchers.Default)
        var job : Job? = null
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)

            Log.d(TAG, "OnTextChanged : ${s.toString()} | start : $start | before : $before | count $count")
            if (!s.isNullOrBlank() && count > 2) {
                Log.d(TAG, "Running scope")

                job?.cancel()
                Log.d(TAG, "IsJobCanceled : ${job?.isCancelled}")
                bottomSheetView.progress_bar.visibility = View.VISIBLE
                job = scope.launch {
                    viewModel.findCityInJsonFile(thisActivity, s.toString())
                }

            }

//                viewModel.fetchWeatherForecast(s.toString(), BuildConfig.API_KEY)
        }

    }

    fun initResponseObserver() {
        viewModel.city.observe(this, Observer {
            searchAdapter.cities = mutableListOf(it)
            searchAdapter.notifyDataSetChanged()
        })
    }

    fun initCitiesObserver(){
        viewModel.allCities.observe(this, Observer {
            searchAdapter.cities = it.toMutableList()
            searchAdapter.notifyDataSetChanged()
            bottomSheetView.progress_bar.visibility = View.GONE
        })
    }

    private fun initRecyclerView(){
        bottomSheetView.search_cities_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = searchAdapter
        }

        searchAdapter.onCityClickListener = object : SearchAdapter.OnCityClickListener {
            override fun onCityClick(city: City) {
                Log.d(TAG, "Received city : ${city.name}. Yuppie !")
                onCitySelectListener.onCitySelected(city)
                searchAdapter.cities.clear()
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_DayNight_DarkActionBar)
    }

    interface OnCitySelectListener {
        fun onCitySelected(city: City)
    }
}
