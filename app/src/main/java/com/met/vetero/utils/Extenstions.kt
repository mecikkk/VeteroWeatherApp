package com.met.vetero.utils

import android.location.LocationManager
import android.view.View
import androidx.annotation.Nullable

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.fadeIn() {
    this.animate()
            .alpha(1f)
            .setDuration(250)
            .start()
}

fun View.fadeOut(@Nullable runnable: Runnable){
    val animator = this.animate()

    animator.alpha(0f)
            .setDuration(100)
            .withEndAction {
                if(runnable != null)
                    runnable.run()
            }
    animator.start()
}

fun LocationManager.isGpsEnabled() : Boolean = this.isProviderEnabled(LocationManager.GPS_PROVIDER)
