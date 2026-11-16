package io.github.penkzyyy.keepappsawaken

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager

class AwakeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usm: UsageStatsManager
    private lateinit var pm: PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    private var currentForeground: String? = null
    private var lastEventTime = 0L

    private val poll = object : Runnable {
        override fun run() {
            check()
            handler.postDelayed(this, POLL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        lastEventTime = System.currentTimeMillis() - 60_000
        createChannel()
        Brightness.restore(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Prefs.setEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }
        startAsForeground()
        handler.removeCallbacks(poll)
        handler.post(poll)
        return START_STICKY
    }

    private fun check() {
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(lastEventTime, now)
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            when (e.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> currentForeground = e.packageName
                UsageEvents.Event.ACTIVITY_PAUSED ->
                    if (currentForeground == e.packageName) currentForeground = null
            }
        }
        lastEventTime = now

        val selected = Prefs.getSelected(this)
        val shouldStayAwake = pm.isInteractive &&
                currentForeground != null &&
                currentForeground in selected

        if (shouldStayAwake) acquire() else release()
        if (shouldStayAwake && Prefs.isBrightnessEnabled(this)) {
            Brightness.apply(this)
        } else {
            Brightness.restore(this)
        }
    }

    @Suppress("DEPRECATION")
    private fun acquire() {
        if (wakeLock?.isHeld == true) return
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "KeepAppsAwaken:screen"
        ).apply { acquire(WAKE_LOCK_TIMEOUT_MS) }
    }

    private fun release() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Serviço ativo",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Notificação silenciosa do serviço. Podes desativar este canal para esconder o ícone da barra de estado."
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }
        nm.createNotificationChannel(ch)
    }

    private fun startAsForeground() {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, AwakeService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Keep Apps Awaken")
            .setContentText("A vigiar as aplicações selecionadas")
            .setContentIntent(open)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .addAction(Notification.Action.Builder(null, "Parar", stop).build())
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(poll)
        release()
        Brightness.restore(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "keep_apps_awaken_service"
        const val NOTIF_ID = 1
        const val ACTION_STOP = "io.github.penkzyyy.keepappsawaken.STOP"
        private const val POLL_MS = 1500L
        private const val WAKE_LOCK_TIMEOUT_MS = 10_000L

        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, AwakeService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, AwakeService::class.java))
        }
    }
}