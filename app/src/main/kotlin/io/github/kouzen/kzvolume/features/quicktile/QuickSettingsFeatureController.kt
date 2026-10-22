package io.github.kouzen.kzvolume.features.quicktile

import android.app.Activity
import android.content.Context
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.core.feature.FeatureController
import io.github.kouzen.kzvolume.core.feature.FeatureSpec

object QuickSettingsFeatureController : FeatureController {

    override val spec = FeatureSpec(
        featureId = Constants.FEATURE_ID_QUICK_TILE,
        nameRes = R.string.feature_quick_tile_title,
        summaryRes = R.string.feature_quick_tile_description,
        supportsToggle = false,
        supportsConfig = false,
        requiredPermissions = emptySet(),
        displayOrder = 1
    )

    override fun isConfigured(context: Context): Boolean {
        return true
    }

    override fun isEnabled(context: Context): Boolean {
        return true
    }

    override fun setEnabled(context: Context, enabled: Boolean) {
        // This feature intentionally has no on/off state.
    }

    override fun openConfig(activity: Activity) {
        // No configuration screen needed.
    }

    override fun synchronize(context: Context) {
        // No background state to synchronize.
    }
}
