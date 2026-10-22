
package io.github.kouzen.kzvolume.features.floating

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.R
import io.github.kouzen.kzvolume.core.feature.PermissionRequirement
import io.github.kouzen.kzvolume.core.permission.PermissionPolicy
import io.github.kouzen.kzvolume.core.preferences.AppPreferences
import kotlin.math.abs

class FloatingButtonService : Service() {

    private val windowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private val audioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannelIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_STOP_FLOATING_SERVICE -> {
                AppPreferences.setFloatingEnabled(this, false)
                stopSelf()
                return START_NOT_STICKY
            }
            Constants.ACTION_VOLUME_DOWN -> {
                adjustVolumeDirect(AudioManager.ADJUST_LOWER)
                return START_STICKY
            }
            Constants.ACTION_VOLUME_UP -> {
                adjustVolumeDirect(AudioManager.ADJUST_RAISE)
                return START_STICKY
            }
            Constants.ACTION_VOLUME_MUTE -> {
                toggleMute()
                return START_STICKY
            }
        }

        // Set to specialUse type to match VolumeOverlayService and since it's an overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.NOTIFICATION_ID_FLOATING,
                buildForegroundNotification()
            )
        } else {
            startForeground(
                Constants.NOTIFICATION_ID_FLOATING,
                buildForegroundNotification()
            )
        }

        updateFloatingButton()
        return START_STICKY
    }

    override fun onDestroy() {
        detachFloatingButton()
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateFloatingButton() {
        if (!PermissionPolicy.isGranted(this, PermissionRequirement.Overlay)) {
            stopSelf()
            return
        }

        val iconName = AppPreferences.getFloatingIconName(this)
        val icon = OverlayIconRegistry.getIconByName(iconName)
        val iconResId = icon.resId
        val iconColor = AppPreferences.getFloatingIconColor(this)

        val tintColor = if (iconColor == 0) {
            getColor(R.color.colorPrimary)
        } else {
            iconColor
        }

        val opacity = AppPreferences.getFloatingOpacity(this)
        val isFixedPosition = AppPreferences.isFloatingFixedPosition(this)
        val sizeDp = AppPreferences.getFloatingSizeDp(this)
        val iconSize = dpToPx(sizeDp)

        if (floatingView != null) {
            (floatingView as? ImageView)?.let {
                it.setImageResource(iconResId)
                it.setColorFilter(tintColor)
                it.alpha = opacity
                it.setOnTouchListener(FloatingTouchListener(isFixedPosition))
                params?.let { p ->
                    if (p.width != iconSize || p.height != iconSize) {
                        p.width = iconSize
                        p.height = iconSize
                        windowManager.updateViewLayout(it, p)
                    }
                }
            }
            return
        }

        val isLandscape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val position = AppPreferences.getFloatingPosition(this, isLandscape)

        // Using FLAG_LAYOUT_NO_LIMITS to fix edge bounds clipping
        val layoutParams = WindowManager.LayoutParams(
            iconSize,
            iconSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START

            val bounds = getScreenBounds(isLandscape)
            val screenWidth = bounds.first
            val screenHeight = bounds.second

            x = if (position != null) {
                (position.first * (screenWidth - iconSize))
                    .toInt()
                    .coerceIn(0, (screenWidth - iconSize).coerceAtLeast(0))
            } else {
                100
            }

            y = if (position != null) {
                (position.second * (screenHeight - iconSize))
                    .toInt()
                    .coerceIn(0, (screenHeight - iconSize).coerceAtLeast(0))
            } else {
                100
            }
        }

        params = layoutParams

        val imageView = ImageView(this).apply {
            setImageResource(iconResId)
            setColorFilter(tintColor)
            alpha = opacity
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            elevation = dpToPx(4).toFloat()
            setOnTouchListener(FloatingTouchListener(isFixedPosition))
        }

        floatingView = imageView

        runCatching {
            windowManager.addView(imageView, layoutParams)
        }.onFailure {
            stopSelf()
        }
    }

    private inner class FloatingTouchListener(
        private val isFixedPosition: Boolean
    ) : View.OnTouchListener {

        private var initialX: Int = 0
        private var initialY: Int = 0
        private var initialTouchX: Float = 0f
        private var initialTouchY: Float = 0f

        private var touchStartTime: Long = 0
        private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
        private var isLongPressing = false
        private var lastTapTime = 0L
        private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
        private val touchSlop =
            ViewConfiguration.get(this@FloatingButtonService).scaledTouchSlop

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val layoutParams = params ?: return false

            if (isFixedPosition) {
                if (event.action == MotionEvent.ACTION_UP) {
                    showNativeVolumePanel()
                    performHaptic(view, HapticFeedbackConstants.VIRTUAL_KEY)
                    return true
                }
                return event.action == MotionEvent.ACTION_DOWN
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    isLongPressing = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY

                    if (!isLongPressing && (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)) {
                        if (System.currentTimeMillis() - touchStartTime > longPressTimeout) {
                            isLongPressing = true
                            performHaptic(view, HapticFeedbackConstants.LONG_PRESS)
                        }
                    }

                    if (isLongPressing) {
                        val bounds = getScreenBounds()
                        val screenWidth = bounds.first
                        val screenHeight = bounds.second

                        val maxX = (screenWidth - view.width).coerceAtLeast(0)
                        val maxY = (screenHeight - view.height).coerceAtLeast(0)

                        val newX = (initialX + deltaX.toInt()).coerceIn(0, maxX)
                        val newY = (initialY + deltaY.toInt()).coerceIn(0, maxY)

                        layoutParams.x = newX
                        layoutParams.y = newY

                        windowManager.updateViewLayout(view, layoutParams)
                    }

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY

                    if (isLongPressing) {
                        val isLandscape =
                            resources.configuration.orientation ==
                                Configuration.ORIENTATION_LANDSCAPE
                        val bounds = getScreenBounds(isLandscape)
                        val screenWidth = bounds.first
                        val screenHeight = bounds.second

                        val maxX = (screenWidth - view.width).coerceAtLeast(0)
                        val maxY = (screenHeight - view.height).coerceAtLeast(0)

                        val finalX = layoutParams.x.coerceIn(0, maxX)
                        val finalY = layoutParams.y.coerceIn(0, maxY)

                        val percentX = finalX.toFloat() / maxX.coerceAtLeast(1)
                        val percentY = finalY.toFloat() / maxY.coerceAtLeast(1)

                        AppPreferences.setFloatingPosition(
                            this@FloatingButtonService,
                            percentX,
                            percentY,
                            isLandscape
                        )
                    } else if (abs(deltaX) <= touchSlop && abs(deltaY) <= touchSlop) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < doubleTapTimeout) {
                            toggleMute()
                            performHaptic(view, HapticFeedbackConstants.CONFIRM)
                            lastTapTime = 0L
                        } else {
                            lastTapTime = now
                            showNativeVolumePanel()
                            performHaptic(view, HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }

                    isLongPressing = false
                    return true
                }

                else -> return false
            }
        }
    }

    private fun toggleMute() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustVolume(
                    AudioManager.ADJUST_TOGGLE_MUTE,
                    AudioManager.FLAG_SHOW_UI
                )
            } else {
                @Suppress("DEPRECATION")
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_TOGGLE_MUTE,
                    AudioManager.FLAG_SHOW_UI
                )
            }
        }
    }

    private fun adjustVolumeDirect(direction: Int) {
        runCatching {
            audioManager.adjustVolume(
                direction,
                AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun getScreenBounds(
        isLandscape: Boolean = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    ): Pair<Int, Int> {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            val metrics = resources.displayMetrics
            Pair(metrics.widthPixels, metrics.heightPixels)
        }
        val minDim = minOf(raw.first, raw.second)
        val maxDim = maxOf(raw.first, raw.second)
        return if (isLandscape) Pair(maxDim, minDim) else Pair(minDim, maxDim)
    }

    private fun detachFloatingButton() {
        floatingView?.let {
            runCatching {
                windowManager.removeView(it)
            }
        }

        floatingView = null
    }

    private fun performHaptic(view: View, constant: Int) {
        if (AppPreferences.isHapticEnabled(this)) {
            view.performHapticFeedback(constant)
        }
    }

    private fun showNativeVolumePanel() {
        runCatching {
            audioManager.adjustVolume(
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun buildForegroundNotification(): Notification {
        val downIntent = PendingIntent.getService(
            this, 101, Intent(this, FloatingButtonService::class.java).apply { action = Constants.ACTION_VOLUME_DOWN },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val muteIntent = PendingIntent.getService(
            this, 102, Intent(this, FloatingButtonService::class.java).apply { action = Constants.ACTION_VOLUME_MUTE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val upIntent = PendingIntent.getService(
            this, 103, Intent(this, FloatingButtonService::class.java).apply { action = Constants.ACTION_VOLUME_UP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID_FLOATING
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title_floating))
            .setContentText(getString(R.string.notification_text_floating))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_6_volume_down, getString(R.string.notification_volume_down), downIntent)
            .addAction(R.drawable.ic_3_awareness_sound, getString(R.string.notification_volume_mute), muteIntent)
            .addAction(R.drawable.ic_4_mobile_speaker, getString(R.string.notification_volume_up), upIntent)
            .build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID_FLOATING,
            getString(R.string.notification_channel_name_floating),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = getString(R.string.notification_channel_description_floating)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        floatingView?.post {
            applySavedPosition(isLandscape)
        } ?: applySavedPosition(isLandscape)
    }

    private fun applySavedPosition(isLandscape: Boolean) {
        val view = floatingView ?: return
        val currentParams = params ?: return
        val position = AppPreferences.getFloatingPosition(this, isLandscape)

        val bounds = getScreenBounds(isLandscape)
        val screenWidth = bounds.first
        val screenHeight = bounds.second
        val sizeDp = AppPreferences.getFloatingSizeDp(this)
        val iconSizePx = dpToPx(sizeDp)

        val maxX = (screenWidth - iconSizePx).coerceAtLeast(0)
        val maxY = (screenHeight - iconSizePx).coerceAtLeast(0)

        if (position != null) {
            currentParams.x =
                (position.first * maxX).toInt().coerceIn(0, maxX)
            currentParams.y =
                (position.second * maxY).toInt().coerceIn(0, maxY)
        } else {
            currentParams.x = currentParams.x.coerceIn(0, maxX)
            currentParams.y = currentParams.y.coerceIn(0, maxY)
        }

        windowManager.updateViewLayout(view, currentParams)
    }
}
