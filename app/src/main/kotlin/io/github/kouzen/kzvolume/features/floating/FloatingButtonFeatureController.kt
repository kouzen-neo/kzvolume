package io.github.kouzen.kzvolume.features.floating

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.core.feature.FeatureController
import io.github.kouzen.kzvolume.core.feature.FeatureSpec
import io.github.kouzen.kzvolume.core.feature.PermissionRequirement
import io.github.kouzen.kzvolume.core.preferences.AppPreferences

object FloatingButtonFeatureController : FeatureController {

    override val spec = FeatureSpec(
        featureId = Constants.FEATURE_ID_FLOATING_BUTTON,
        nameRes = R.string.feature_floating_title,
        summaryRes = R.string.feature_floating_description,
        supportsToggle = true,
        supportsConfig = true,
        requiredPermissions = setOf(
            PermissionRequirement.Overlay,
        ),
        displayOrder = 2
    )

    override fun isConfigured(context: Context): Boolean {
        return true
    }

    override fun isEnabled(context: Context): Boolean {
        return AppPreferences.isFloatingEnabled(context)
    }

    override fun setEnabled(context: Context, enabled: Boolean) {
        AppPreferences.setFloatingEnabled(context, enabled)
        FloatingButtonRuntime.sync(context)
    }

    override fun openConfig(activity: Activity) {
        activity.startActivity(Intent(activity, FloatingButtonConfigActivity::class.java))
    }

    override fun synchronize(context: Context) {
        FloatingButtonRuntime.sync(context)
    }
}
