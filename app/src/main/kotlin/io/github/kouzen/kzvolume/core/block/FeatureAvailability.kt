package io.github.kouzen.kzvolume.core.block

import io.github.kouzen.kzvolume.core.feature.PermissionRequirement

data class FeatureAvailability(
    val blockResult: FeatureBlockResult,
    val missingPermissions: Set<PermissionRequirement>
) {
    val isAvailable: Boolean
        get() = blockResult is FeatureBlockResult.NotBlocked && missingPermissions.isEmpty()
}
