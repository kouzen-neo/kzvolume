package io.github.kouzen.kzvolume.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class KZVolumeAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: KZVolumeAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        fun lockScreen(): Boolean {
            val service = instance ?: return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                false
            }
        }

        fun openPowerDialog(): Boolean {
            val service = instance ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: performGlobalAction only
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }
}
