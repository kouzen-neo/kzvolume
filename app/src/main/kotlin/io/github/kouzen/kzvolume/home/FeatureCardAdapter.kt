package io.github.kouzen.kzvolume.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.databinding.ItemFeatureCardBinding

class FeatureCardAdapter(
    private val onConfigClick: (featureId: String) -> Unit,
    private val onToggleChanged: (featureId: String, isEnabled: Boolean) -> Unit
) : ListAdapter<FeatureCardUiModel, FeatureCardAdapter.FeatureViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val binding = ItemFeatureCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeatureViewHolder(
        private val binding: ItemFeatureCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeatureCardUiModel) {
            val (iconRes, configIconRes) = when (item.featureId) {
                Constants.FEATURE_ID_FLOATING_BUTTON -> Pair(R.drawable.ic_volume_floating, R.drawable.ic_1_tune)
                Constants.FEATURE_ID_FLOATING_POWER -> Pair(R.drawable.ic_power_settings, R.drawable.ic_1_tune)
                Constants.FEATURE_ID_EDGE_SWIPE -> Pair(R.drawable.ic_volume_edge_swipe, R.drawable.ic_2_adjust)
                Constants.FEATURE_ID_QUICK_TILE -> Pair(R.drawable.ic_quick_settings_tile, R.drawable.ic_1_tune)
                else -> Pair(R.drawable.ic_volume_floating, R.drawable.ic_1_tune)
            }
            binding.featureIcon.setImageResource(iconRes)
            binding.featureConfigButton.setIconResource(configIconRes)

            binding.featureTitleText.text = item.title
            binding.featureDescriptionText.text = item.description

            // Switch
            binding.featureToggleSwitch.setOnCheckedChangeListener(null)
            binding.featureToggleSwitch.isVisible = item.showToggle
            binding.featureToggleSwitch.isEnabled = item.toggleEnabled
            binding.featureToggleSwitch.isChecked = item.toggledOn

            binding.featureToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggleChanged(item.featureId, isChecked)
            }

            // Notice / Alert Box
            if (!item.noticeText.isNullOrBlank()) {
                binding.featureNoticeContainer.isVisible = true
                binding.featureNoticeText.text = item.noticeText
                if (!item.noticeActionText.isNullOrBlank()) {
                    binding.featureNoticeActionButton.isVisible = true
                    binding.featureNoticeActionButton.text = item.noticeActionText
                    binding.featureNoticeActionButton.setOnClickListener {
                        item.noticeAction?.invoke()
                    }
                } else {
                    binding.featureNoticeActionButton.isVisible = false
                }
            } else {
                binding.featureNoticeContainer.isVisible = false
            }

            // Bottom action row
            binding.bottomActionRow.isVisible = item.showConfig
            binding.featureConfigButton.isVisible = item.showConfig
            binding.featureConfigButton.isEnabled = item.configEnabled
            binding.featureConfigButton.text = item.configButtonText
            binding.featureConfigButton.setOnClickListener {
                onConfigClick(item.featureId)
            }

            // Card click behavior
            binding.root.setOnClickListener {
                if (item.showToggle && item.toggleEnabled) {
                    binding.featureToggleSwitch.toggle()
                } else if (item.showConfig && item.configEnabled) {
                    onConfigClick(item.featureId)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FeatureCardUiModel>() {
        override fun areItemsTheSame(oldItem: FeatureCardUiModel, newItem: FeatureCardUiModel): Boolean {
            return oldItem.featureId == newItem.featureId
        }

        override fun areContentsTheSame(oldItem: FeatureCardUiModel, newItem: FeatureCardUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
