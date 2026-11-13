package io.github.penkzyyy.keepappsawaken

import android.content.Context

object Prefs {
    private const val FILE = "keep_apps_awaken"
    private const val KEY_APPS = "selected_apps"
    private const val KEY_ENABLED = "service_enabled"

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
}