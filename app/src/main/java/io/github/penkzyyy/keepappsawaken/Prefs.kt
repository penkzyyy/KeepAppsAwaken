package io.github.penkzyyy.keepappsawaken

import android.content.Context

object Prefs {
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    private const val FILE = "keep_apps_awaken"
    private const val KEY_APPS = "selected_apps"
    private const val KEY_ENABLED = "service_enabled"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_BRIGHT = "brightness_feature"
    private const val KEY_BRIGHT_ACTIVE = "brightness_active"
    private const val KEY_BRIGHT_MODE = "brightness_prev_mode"
    private const val KEY_BRIGHT_VALUE = "brightness_prev_value"

    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getSelected(ctx: Context): Set<String> =
        sp(ctx).getStringSet(KEY_APPS, emptySet()) ?: emptySet()

    fun setSelected(ctx: Context, apps: Set<String>) {
        sp(ctx).edit().putStringSet(KEY_APPS, HashSet(apps)).apply()
    }

    fun isEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_ENABLED, false)

    fun setEnabled(ctx: Context, enabled: Boolean) {
        sp(ctx).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getTheme(ctx: Context): Int =
        sp(ctx).getInt(KEY_THEME, THEME_SYSTEM)

    fun setTheme(ctx: Context, mode: Int) {
        sp(ctx).edit().putInt(KEY_THEME, mode).apply()
    }

    fun isBrightnessEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_BRIGHT, false)

    fun setBrightnessEnabled(ctx: Context, on: Boolean) {
        sp(ctx).edit().putBoolean(KEY_BRIGHT, on).apply()
    }

    fun isBrightnessActive(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_BRIGHT_ACTIVE, false)

    fun saveBrightness(ctx: Context, mode: Int, value: Int) {
        sp(ctx).edit()
            .putBoolean(KEY_BRIGHT_ACTIVE, true)
            .putInt(KEY_BRIGHT_MODE, mode)
            .putInt(KEY_BRIGHT_VALUE, value)
            .apply()
    }

    fun prevBrightnessMode(ctx: Context): Int =
        sp(ctx).getInt(KEY_BRIGHT_MODE, 1)

    fun prevBrightnessValue(ctx: Context): Int =
        sp(ctx).getInt(KEY_BRIGHT_VALUE, 128)

    fun clearBrightness(ctx: Context) {
        sp(ctx).edit().putBoolean(KEY_BRIGHT_ACTIVE, false).apply()
    }
}