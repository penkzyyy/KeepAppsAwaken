package io.github.penkzyyy.keepappsawaken

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object AppTheme {
    /** Chamar antes de super.onCreate() em cada Activity. */
    fun apply(activity: Activity) {
        val dark = when (Prefs.getTheme(activity)) {
            Prefs.THEME_DARK -> true
            Prefs.THEME_LIGHT -> false
            else -> {
                val night = activity.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK
                night == Configuration.UI_MODE_NIGHT_YES
            }
        }
        activity.setTheme(
            if (dark) android.R.style.Theme_Material_NoActionBar
            else android.R.style.Theme_Material_Light_NoActionBar
        )
    }
}

object Perms {
    fun hasUsageAccess(ctx: Context): Boolean {
        val ops = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        @Suppress("DEPRECATION")
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), ctx.packageName
            )
        } else {
            ops.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), ctx.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasNotifications(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
                ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    fun isBatteryUnrestricted(ctx: Context): Boolean {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    fun allGranted(ctx: Context): Boolean =
        hasUsageAccess(ctx) && hasNotifications(ctx) && isBatteryUnrestricted(ctx)

    /** Pede a próxima permissão em falta. Devolve false se já estava tudo concedido. */
    fun requestNext(activity: Activity): Boolean {
        if (!hasUsageAccess(activity)) {
            activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            return true
        }
        if (!hasNotifications(activity)) {
            activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            return true
        }
        if (!isBatteryUnrestricted(activity)) {
            activity.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${activity.packageName}"))
            )
            return true
        }
        return false
    }
}

object Notif {
    /** Garante que o canal existe (igual ao do AwakeService) para podermos abrir as suas definições. */
    fun ensureChannel(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(AwakeService.CHANNEL_ID) != null) return
        val ch = NotificationChannel(
            AwakeService.CHANNEL_ID,
            "Serviço ativo",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }
        nm.createNotificationChannel(ch)
    }

    fun openChannelSettings(activity: Activity) {
        ensureChannel(activity)
        activity.startActivity(
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, AwakeService.CHANNEL_ID)
        )
    }
}
object Brightness {
    /** Desliga o brilho automático e põe o brilho no máximo, guardando o estado anterior. */
    fun apply(ctx: Context) {
        if (Prefs.isBrightnessActive(ctx) || !Settings.System.canWrite(ctx)) return
        try {
            val cr = ctx.contentResolver
            val mode = Settings.System.getInt(
                cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
            val value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, 128)
            Prefs.saveBrightness(ctx, mode, value)
            Settings.System.putInt(
                cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, 255)
        } catch (e: Exception) {
            // Sem permissão ou erro do sistema: ignora
        }
    }

    /** Repõe o brilho e o modo que existiam antes de apply(). */
    fun restore(ctx: Context) {
        if (!Prefs.isBrightnessActive(ctx)) return
        try {
            if (Settings.System.canWrite(ctx)) {
                val cr = ctx.contentResolver
                Settings.System.putInt(
                    cr, Settings.System.SCREEN_BRIGHTNESS, Prefs.prevBrightnessValue(ctx)
                )
                Settings.System.putInt(
                    cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Prefs.prevBrightnessMode(ctx)
                )
            }
        } catch (e: Exception) {
            // ignora
        }
        Prefs.clearBrightness(ctx)
    }
}