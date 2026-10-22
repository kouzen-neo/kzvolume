package io.github.kouzen.kzvolume.features.edge

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.core.block.FeatureBlockCondition
import io.github.kouzen.kzvolume.core.feature.FeatureController
import io.github.kouzen.kzvolume.core.feature.FeatureSpec
import io.github.kouzen.kzvolume.core.feature.PermissionRequirement
import io.github.kouzen.kzvolume.core.preferences.AppPreferences

object EdgeSwipeFeatureController : FeatureController {

    override val blockCondition: FeatureBlockCondition = EdgeSwipeBlockCondition

    override val spec = FeatureSpec(
        featureId = Constants.FEATURE_ID_EDGE_SWIPE,
        nameRes = R.string.feature_edge_title,
        summaryRes = R.string.feature_edge_description,
        supportsToggle = true,
        supportsConfig = true,
        requiredPermissions = setOf(
            PermissionRequirement.Overlay,
        ),
        displayOrder = 3
    )

    override fun isConfigured(context: Context): Boolean {
        return true
    }

    override fun isEnabled(context: Context): Boolean {
        return AppPreferences.isEdgeEnabled(context)
    }

    override fun setEnabled(context: Context, enabled: Boolean) {
        AppPreferences.setEdgeEnabled(context, enabled)
        EdgeOverlayRuntime.sync(context)
    }

    override fun openConfig(activity: Activity) {
        activity.startActivity(Intent(activity, EdgeSwipeSideSelectorActivity::class.java))
    }

    override fun synchronize(context: Context) {
        EdgeOverlayRuntime.sync(context)
    }
}
