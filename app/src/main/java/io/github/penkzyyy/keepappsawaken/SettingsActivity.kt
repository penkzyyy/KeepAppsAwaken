package io.github.penkzyyy.keepappsawaken

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private lateinit var permStatus: TextView
    private lateinit var brightSwitch: Switch
    private lateinit var brightStatus: TextView

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

        // Brilho
        root.addView(section("Brilho"))
        brightSwitch = Switch(this).apply { text = "Desativar brilho automático" }
        root.addView(brightSwitch)
        root.addView(TextView(this).apply {
            textSize = 13f
            text = "Enquanto o serviço estiver ativo e uma aplicação escolhida estiver aberta, " +
                    "desliga o brilho automático e põe o brilho no máximo. " +
                    "Ao sair da aplicação, volta ao que tinhas antes."
        })
        brightStatus = TextView(this).apply { textSize = 13f }
        root.addView(brightStatus)

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
        bindBrightnessSwitch()
    }

    private fun bindBrightnessSwitch() {
        val canWrite = Settings.System.canWrite(this)
        brightSwitch.setOnCheckedChangeListener(null)
        brightSwitch.isChecked = Prefs.isBrightnessEnabled(this) && canWrite
        brightSwitch.setOnCheckedChangeListener { _, on ->
            if (on && !Settings.System.canWrite(this)) {
                brightSwitch.isChecked = false
                Toast.makeText(
                    this, "Concede a permissão e volta a ligar", Toast.LENGTH_LONG
                ).show()
                startActivity(
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                )
            } else {
                Prefs.setBrightnessEnabled(this, on)
            }
        }
        brightStatus.text = "Modificar definições do sistema: " +
                if (canWrite) "OK" else "em falta"
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