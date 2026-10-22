package io.github.kouzen.kzvolume.home

data class FeatureCardUiModel(
    val featureId: String,
    val title: String,
    val description: String,
    val noticeText: String? = null,
    val noticeActionText: String? = null,
    val noticeAction: (() -> Unit)? = null,
    val showToggle: Boolean,
    val toggleEnabled: Boolean,
    val toggledOn: Boolean,
    val showConfig: Boolean,
    val configEnabled: Boolean,
    val configButtonText: String
)
