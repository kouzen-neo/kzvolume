package io.github.kouzen.kzvolume.core.block

import android.content.Context

fun interface FeatureBlockCondition {
    fun evaluate(context: Context): FeatureBlockResult
}
