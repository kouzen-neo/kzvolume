package io.github.kouzen.kzvolume.features.floating

import android.app.Activity
import android.content.Context
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.core.feature.FeatureController
import io.github.kouzen.kzvolume.core.feature.FeatureSpec
import io.github.kouzen.kzvolume.core.feature.PermissionRequirement
import io.github.kouzen.kzvolume.core.preferences.AppPreferences

object FloatingPowerFeatureController : FeatureController {

    override val spec = FeatureSpec(
        featureId = Constants.FEATURE_ID_FLOATING_POWER,
        nameRes = R.string.feature_floating_power_title,
        summaryRes = R.string.feature_floating_power_subtitle,
        supportsToggle = true,
        supportsConfig = false,
        requiredPermissions = setOf(
            PermissionRequirement.Overlay,
        ),
        displayOrder = 3
    )

    override fun isConfigured(context: Context): Boolean = true

    override fun isEnabled(context: Context): Boolean {
        return AppPreferences.isFloatingPowerEnabled(context)
    }

    override fun setEnabled(context: Context, enabled: Boolean) {
        AppPreferences.setFloatingPowerEnabled(context, enabled)
        FloatingButtonRuntime.sync(context)
    }

    override fun openConfig(activity: Activity) {
        // No configuration screen needed
    }

    override fun synchronize(context: Context) {
        FloatingButtonRuntime.sync(context)
    }
}
