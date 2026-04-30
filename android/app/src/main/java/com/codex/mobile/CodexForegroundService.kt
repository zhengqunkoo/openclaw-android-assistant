package com.codex.mobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class CodexForegroundService : Service() {

    companion object {
        private const val TAG = "CodexForegroundService"
        private const val CHANNEL_ID = "codex_running"
        private const val NOTIFICATION_ID = 1

        /** Extra flag set by [BootReceiver] to indicate a post-boot start. */
        const val EXTRA_BOOT_START = "boot_start"

        /** Interval between watchdog health checks (ms). */
        private const val WATCHDOG_INTERVAL_MS = 30_000L
    }

    @Volatile
    private var watchdogRunning = false
    private var watchdogThread: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isBootStart = intent?.getBooleanExtra(EXTRA_BOOT_START, false) ?: false

        if (isBootStart) {
            Log.i(TAG, "Boot-initiated start — launching gateway restart if installed")
            Thread { bootRestartGateway() }.apply { name = "boot-gateway-restart" }.start()
        }

        if (!watchdogRunning) {
            startWatchdog()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        watchdogRunning = false
        watchdogThread?.interrupt()
        watchdogThread = null
    }

    /**
     * On boot, if OpenClaw is already installed and configured, restart
     * only the gateway (skip the full setup flow which requires user
     * interaction for login / API key entry).
     */
    private fun bootRestartGateway() {
        try {
            val mgr = CodexServerManager.instance
                ?: CodexServerManager.setInstance(CodexServerManager(applicationContext))

            if (!mgr.isOpenClawInstalled()) {
                Log.i(TAG, "OpenClaw not installed — skipping boot gateway restart")
                return
            }

            Log.i(TAG, "Restarting OpenClaw gateway after boot")
            mgr.configureOpenClawAuth()
            mgr.startOpenClawGateway()
            mgr.startOpenClawControlUiServer()
        } catch (e: Exception) {
            Log.e(TAG, "Boot gateway restart failed: ${e.message}")
        }
    }

    /**
     * Watchdog thread: every [WATCHDOG_INTERVAL_MS] ms, check whether the
     * OpenClaw gateway process is still alive. If it has died and OpenClaw
     * is installed, restart it automatically.
     */
    private fun startWatchdog() {
        watchdogRunning = true
        watchdogThread = Thread {
            Log.i(TAG, "Watchdog started")
            while (watchdogRunning) {
                try {
                    Thread.sleep(WATCHDOG_INTERVAL_MS)
                } catch (_: InterruptedException) {
                    break
                }

                try {
                    val mgr = CodexServerManager.instance ?: continue
                    if (!mgr.isGatewayRunning && mgr.isOpenClawInstalled()) {
                        Log.w(TAG, "Watchdog: OpenClaw gateway died — restarting")
                        mgr.configureOpenClawAuth()
                        mgr.startOpenClawGateway()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Watchdog iteration failed: ${e.message}")
                }
            }
            Log.i(TAG, "Watchdog stopped")
        }.apply { isDaemon = true; name = "openclaw-watchdog" }
        watchdogThread!!.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AnyClaw Running",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Keeps Codex server running in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("AnyClaw is running")
            .setContentText("Server active in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}

