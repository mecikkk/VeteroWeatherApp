package com.met.vetero.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.met.vetero.R
import kotlinx.android.synthetic.main.fragment_connection_failed.*


class ConnectionFailedFragment() : DialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = ConnectionFailedFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_NoActionBar_TranslucentDecor)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection_failed, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        wifi_icon.animate()
                .alpha(1.0f)
                .setDuration(350)
                .setStartDelay(500)
                .start()
        no_wifi_text.animate()
                .alpha(1.0f)
                .setDuration(350)
                .setStartDelay(500)
                .start()
    }


}
