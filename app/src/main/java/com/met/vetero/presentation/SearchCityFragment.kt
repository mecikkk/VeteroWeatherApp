package com.met.vetero.presentation

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.met.vetero.R
import com.met.vetero.data.room.City
import com.met.vetero.utils.SimpleTextWatcher
import kotlinx.android.synthetic.main.fragment_search_city.*
import kotlinx.android.synthetic.main.fragment_search_city.view.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class SearchCityFragment() : BottomSheetDialogFragment() {

    private val vModel: MainActivityViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance() = SearchCityFragment()
    }

    private lateinit var bottomSheetView: View
    private lateinit var behavior: BottomSheetBehavior<View>
    private val searchAdapter: SearchAdapter by inject()
    private var onCitySelectListener: OnCitySelectListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet: BottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetView = View.inflate(context, R.layout.fragment_search_city, null)
        bottomSheet.setContentView(bottomSheetView)

        behavior = BottomSheetBehavior.from(bottomSheetView.parent as View)

        behavior.peekHeight = ((Resources.getSystem().displayMetrics.heightPixels) / 1.2).toInt()

        bottomSheetView.apply {
            minimumHeight = ((Resources.getSystem().displayMetrics.heightPixels) / 2)
            layoutParams.height = (Resources.getSystem().displayMetrics.heightPixels)
        }

        bottomSheetView.progress_bar.visibility = View.GONE

        bottomSheet.search_text_input_layout.requestFocus()
        bottomSheet.search_text_input_edit_text.addTextChangedListener(searchTextWatcher())

        initResponseObserver()
        initCitiesObserver()
        initRecyclerView()
        return bottomSheet
    }


    private fun searchTextWatcher() = object : SimpleTextWatcher() {
        var job: Job? = null

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            super.onTextChanged(s, start, before, count)

            if (!s.isNullOrBlank() && count > 2) {
                job?.cancel()
                bottomSheetView.progress_bar.visibility = View.VISIBLE
                job = CoroutineScope(Dispatchers.Default).launch {
                    delay(500)
                    vModel.findCityInJsonFile(requireActivity(), s.toString())
                }
            }
        }
    }

    private fun initResponseObserver() {
        vModel.city.observe(this, Observer {
            searchAdapter.cities = mutableListOf(it)
            searchAdapter.notifyDataSetChanged()
        })
    }

    private fun initCitiesObserver() {
        vModel.allCities.observe(this, Observer {
            searchAdapter.cities = it.toMutableList()
            searchAdapter.notifyDataSetChanged()
            bottomSheetView.progress_bar.visibility = View.GONE
        })
    }

    private fun initRecyclerView() {
        bottomSheetView.search_cities_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = searchAdapter
        }

        searchAdapter.onCityClickListener = object : SearchAdapter.OnCityClickListener {
            override fun onCityClick(city: City) {
                onCitySelectListener?.onCitySelected(city)
                searchAdapter.cities.clear()
            }

        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_DayNight_DarkActionBar)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCitySelectListener = context as OnCitySelectListener
    }

    override fun onDetach() {
        super.onDetach()
        onCitySelectListener = null
    }

    interface OnCitySelectListener {
        fun onCitySelected(city: City)
    }
}
