package io.github.kouzen.kzvolume

import android.app.Application
import com.google.android.material.color.DynamicColors

class KZVolumeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
