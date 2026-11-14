package io.github.penkzyyy.keepappsawaken

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private lateinit var permStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppTheme.apply(this)
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }

        root.addView(TextView(this).apply {
            text = "Definições"
            textSize = 22f
        })

        // Permissões
        root.addView(section("Permissões"))
        permStatus = TextView(this).apply { textSize = 13f }
        root.addView(permStatus)
        root.addView(Button(this).apply {
            text = "Conceder permissões"
            setOnClickListener {
                if (!Perms.requestNext(this@SettingsActivity)) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Todas as permissões já estão concedidas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        // Modo de ecrã
        root.addView(section("Modo de ecrã"))
        val group = RadioGroup(this)
        val options = listOf(
            Prefs.THEME_SYSTEM to "Seguir o sistema",
            Prefs.THEME_LIGHT to "Claro",
            Prefs.THEME_DARK to "Escuro"
        )
        val modeById = HashMap<Int, Int>()
        val current = Prefs.getTheme(this)
        options.forEach { (mode, label) ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = label
            }
            modeById[rb.id] = mode
            group.addView(rb)
            if (mode == current) group.check(rb.id)
        }
        group.setOnCheckedChangeListener { _, checkedId ->
            val mode = modeById[checkedId] ?: return@setOnCheckedChangeListener
            if (mode != Prefs.getTheme(this)) {
                Prefs.setTheme(this, mode)
                recreate()
            }
        }
        root.addView(group)

        // Barra de estado
        root.addView(section("Barra de estado"))
        root.addView(TextView(this).apply {
            textSize = 13f
            text = "O Android obriga o serviço a ter uma notificação. " +
                    "Abre as definições do canal e desativa-o: o ícone desaparece " +
                    "e o serviço continua a funcionar."
        })
        root.addView(Button(this).apply {
            text = "Esconder ícone da barra de estado"
            setOnClickListener { Notif.openChannelSettings(this@SettingsActivity) }
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        updatePermStatus()
    }

    private fun updatePermStatus() {
        fun mark(ok: Boolean) = if (ok) "OK" else "em falta"
        permStatus.text = "Estatísticas de uso: ${mark(Perms.hasUsageAccess(this))}\n" +
                "Notificações: ${mark(Perms.hasNotifications(this))}\n" +
                "Bateria sem restrições: ${mark(Perms.isBatteryUnrestricted(this))}"
    }

    private fun section(label: String) = TextView(this).apply {
        text = label
        textSize = 16f
        setPadding(0, dp(24), 0, dp(8))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}