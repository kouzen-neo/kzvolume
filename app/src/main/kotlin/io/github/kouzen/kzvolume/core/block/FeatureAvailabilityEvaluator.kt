package io.github.kouzen.kzvolume.core.block

import android.content.Context
import android.util.Log
import io.github.kouzen.kzvolume.core.feature.FeatureController
import io.github.kouzen.kzvolume.core.permission.PermissionPolicy

object FeatureAvailabilityEvaluator {

    private const val TAG = "FeatureAvailabilityEval"

    fun evaluate(context: Context, controller: FeatureController): FeatureAvailability {
        val blockResult = controller.blockCondition.evaluate(context)
        val missingPermissions = PermissionPolicy.missingPermissions(
            context,
            controller.spec.requiredPermissions
        )
        return FeatureAvailability(blockResult, missingPermissions)
    }

    fun enforce(context: Context, controller: FeatureController): FeatureAvailability {
        val availability = evaluate(context, controller)
        if (availability.blockResult is FeatureBlockResult.Blocked &&
            controller.spec.supportsToggle &&
            controller.isEnabled(context)
        ) {
            runCatching {
                controller.setEnabled(context, false)
            }.onFailure { e ->
                Log.e(TAG, "Failed to force-disable feature ${controller.spec.featureId}", e)
            }
        }
        return evaluate(context, controller)
    }
}
