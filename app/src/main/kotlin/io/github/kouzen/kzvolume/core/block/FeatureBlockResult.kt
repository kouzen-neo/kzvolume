package io.github.kouzen.kzvolume.core.block

import androidx.annotation.StringRes

sealed class FeatureBlockResult {
    object NotBlocked : FeatureBlockResult()
    data class Blocked(@StringRes val reasonRes: Int) : FeatureBlockResult()
}
