package io.github.kouzen.kzvolume.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.core.block.FeatureAvailabilityEvaluator
import io.github.kouzen.kzvolume.core.block.FeatureBlockResult
import io.github.kouzen.kzvolume.core.feature.FeatureController
import io.github.kouzen.kzvolume.core.feature.FeatureRegistry
import io.github.kouzen.kzvolume.core.permission.PermissionPolicy
import io.github.kouzen.kzvolume.core.preferences.AppPreferences
import io.github.kouzen.kzvolume.databinding.ActivityHomeBinding
import io.github.kouzen.kzvolume.permission.PermissionHubActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val audioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var isUpdatingSlider = false

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                syncVolumeUi()
            }
        }
    }

    private val adapter by lazy {
        FeatureCardAdapter(
            onConfigClick = ::handleConfigClick,
            onToggleChanged = ::handleToggleChanged
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val baseHorizontal = (16 * resources.displayMetrics.density).toInt()
            val baseTop = (16 * resources.displayMetrics.density).toInt()
            val baseBottom = (16 * resources.displayMetrics.density).toInt()
            v.setPadding(
                systemBars.left + baseHorizontal,
                systemBars.top + baseTop,
                systemBars.right + baseHorizontal,
                systemBars.bottom + baseBottom
            )
            insets
        }

        AppPreferences.ensureMigration(this)

        binding.featureRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.featureRecyclerView.adapter = adapter
        binding.homeInfoButton.setOnClickListener {
            showAboutDialog()
        }

        setupVolumeHeroCard()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.app_info_what_does_it_do)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setupVolumeHeroCard() {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        binding.heroVolumeSlider.valueFrom = 0f
        binding.heroVolumeSlider.valueTo = maxVol.toFloat()
        binding.heroVolumeSlider.stepSize = 1f

        binding.heroVolumeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser && !isUpdatingSlider) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value.toInt(), 0)
                updateVolumePercentText(value.toInt(), maxVol)
                performHapticIfEnabled(HapticFeedbackConstants.CLOCK_TICK)
            }
        }

        binding.heroVolumeDownButton.setOnClickListener {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0)
            syncVolumeUi()
            performHapticIfEnabled(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        binding.heroVolumeUpButton.setOnClickListener {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0)
            syncVolumeUi()
            performHapticIfEnabled(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        binding.volumeMuteToggle.setOnClickListener {
            runCatching {
                audioManager.adjustVolume(AudioManager.ADJUST_TOGGLE_MUTE, 0)
            }
            syncVolumeUi()
            performHapticIfEnabled(HapticFeedbackConstants.CONFIRM)
        }
    }

    private fun syncVolumeUi() {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).coerceIn(0, maxVol)

        isUpdatingSlider = true
        binding.heroVolumeSlider.value = currentVol.toFloat()
        updateVolumePercentText(currentVol, maxVol)

        val iconRes = when {
            currentVol == 0 -> R.drawable.ic_volume_mute_clean
            currentVol < maxVol / 2 -> R.drawable.ic_volume_low
            else -> R.drawable.ic_volume_high
        }
        binding.volumeMuteToggle.setImageResource(iconRes)
        isUpdatingSlider = false
    }

    private fun updateVolumePercentText(current: Int, max: Int) {
        val percent = (current * 100 / max).coerceIn(0, 100)
        binding.volumePercentText.text = "$percent%"
    }

    private fun performHapticIfEnabled(feedbackConstant: Int) {
        if (AppPreferences.isHapticEnabled(this)) {
            binding.root.performHapticFeedback(feedbackConstant)
        }
    }

    private fun synchronizeFeatures() {
        FeatureRegistry.all().forEach { feature ->
            try {
                feature.synchronize(this)
            } catch (exception: Exception) {
                Log.e(
                    "HomeActivity",
                    "Failed to synchronize feature ${feature.spec.featureId}",
                    exception
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        synchronizeFeatures()
        refreshFeatureCards()
        syncVolumeUi()

        runCatching {
            ContextCompat.registerReceiver(
                this,
                volumeReceiver,
                IntentFilter("android.media.VOLUME_CHANGED_ACTION"),
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    override fun onPause() {
        runCatching {
            unregisterReceiver(volumeReceiver)
        }
        super.onPause()
    }

    private fun refreshFeatureCards() {
        val controllers = FeatureRegistry.all()

        controllers.forEach { controller ->
            runCatching {
                FeatureAvailabilityEvaluator.enforce(this, controller)
            }.onFailure {
                Log.e(
                    "HomeActivity",
                    "Failed to enforce availability for ${controller.spec.featureId}",
                    it
                )
            }
        }

        val models = controllers.map { controller ->
            buildFeatureModel(controller)
        }

        adapter.submitList(models)
    }

    private fun buildFeatureModel(controller: FeatureController): FeatureCardUiModel {
        val spec = controller.spec

        return runCatching {
            val availability = FeatureAvailabilityEvaluator.evaluate(this, controller)
            val configured = controller.isConfigured(this)
            val enabled = controller.isEnabled(this)
            val blockResult = availability.blockResult
            val isBlocked = blockResult is FeatureBlockResult.Blocked
            val missingPermissions = PermissionPolicy.missingPermissions(this, spec.requiredPermissions)
            val hasPermissions = missingPermissions.isEmpty()

            val noticeText: String?
            val noticeActionText: String?
            val noticeAction: (() -> Unit)?

            when {
                isBlocked -> {
                    noticeText = getString((blockResult as FeatureBlockResult.Blocked).reasonRes)
                    noticeActionText = "Open Navigation Settings"
                    noticeAction = { openNavigationSettings(this) }
                }

                !hasPermissions -> {
                    noticeText = getString(R.string.permission_screen_subtitle)
                    noticeActionText = "Grant Permissions"
                    noticeAction = {
                        val intent = Intent(this, PermissionHubActivity::class.java).apply {
                            putExtra(Constants.EXTRA_FEATURE_ID, spec.featureId)
                        }
                        startActivity(intent)
                    }
                }

                spec.supportsToggle && !configured -> {
                    noticeText = "Initial configuration required before enabling."
                    noticeActionText = "Setup Now"
                    noticeAction = { handleConfigClick(spec.featureId) }
                }

                else -> {
                    noticeText = null
                    noticeActionText = null
                    noticeAction = null
                }
            }

            FeatureCardUiModel(
                featureId = spec.featureId,
                title = getString(spec.nameRes),
                description = getString(spec.summaryRes),
                noticeText = noticeText,
                noticeActionText = noticeActionText,
                noticeAction = noticeAction,
                showToggle = spec.supportsToggle,
                toggleEnabled = spec.supportsToggle && !isBlocked,
                toggledOn = spec.supportsToggle && enabled,
                showConfig = spec.supportsConfig,
                configEnabled = !isBlocked,
                configButtonText = getString(spec.configActionRes)
            )
        }.getOrElse {
            FeatureCardUiModel(
                featureId = spec.featureId,
                title = getString(spec.nameRes),
                description = getString(spec.summaryRes),
                noticeText = null,
                noticeActionText = null,
                noticeAction = null,
                showToggle = spec.supportsToggle,
                toggleEnabled = false,
                toggledOn = false,
                showConfig = spec.supportsConfig,
                configEnabled = false,
                configButtonText = getString(spec.configActionRes)
            )
        }
    }

    private fun openNavigationSettings(context: Context) {
        val intents = listOf(
            Intent("com.miui.action.SYSTEM_NAVIGATION"),
            Intent("android.settings.NAVIGATION_MODE_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                runCatching { context.startActivity(intent); return }
            }
        }
        runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    private fun handleConfigClick(featureId: String) {
        val controller = FeatureRegistry.findById(featureId) ?: return

        val blockResult = controller.blockCondition.evaluate(this)
        if (blockResult is FeatureBlockResult.Blocked) {
            return
        }

        val missingPermissions = PermissionPolicy.missingPermissions(
            this,
            controller.spec.requiredPermissions
        )

        if (missingPermissions.isNotEmpty()) {
            val intent = Intent(this, PermissionHubActivity::class.java)
                .putExtra(Constants.EXTRA_FEATURE_ID, featureId)

            startActivity(intent)
            return
        }

        runCatching {
            controller.openConfig(this)
        }.onFailure {
            Toast.makeText(this, R.string.feature_action_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleToggleChanged(featureId: String, isEnabled: Boolean) {
        val controller = FeatureRegistry.findById(featureId) ?: return

        if (!controller.spec.supportsToggle) {
            return
        }

        val blockResult = controller.blockCondition.evaluate(this)
        if (blockResult is FeatureBlockResult.Blocked) {
            refreshFeatureCards()
            return
        }

        if (isEnabled) {
            val missingPermissions = PermissionPolicy.missingPermissions(
                this,
                controller.spec.requiredPermissions
            )

            if (missingPermissions.isNotEmpty()) {
                handleConfigClick(featureId)
                revertToggle(featureId)
                return
            }

            if (controller.spec.supportsConfig && !controller.isConfigured(this)) {
                handleConfigClick(featureId)
                revertToggle(featureId)
                return
            }
        }

        runCatching {
            controller.setEnabled(this, isEnabled)
        }.onFailure {
            Toast.makeText(this, R.string.feature_action_failed, Toast.LENGTH_SHORT)
                .show()
        }

        refreshFeatureCards()
    }

    private fun revertToggle(featureId: String) {
        val index = adapter.currentList.indexOfFirst { it.featureId == featureId }
        if (index != -1) {
            binding.root.post {
                adapter.notifyItemChanged(index)
            }
        }
    }
}
