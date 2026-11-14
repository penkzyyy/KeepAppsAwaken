package io.github.penkzyyy.keepappsawaken

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    data class AppItem(val label: String, val pkg: String, val icon: Drawable)

    private lateinit var adapter: AppAdapter
    private lateinit var allApps: List<AppItem>
    private lateinit var selected: MutableSet<String>
    private lateinit var statusText: TextView
    private lateinit var serviceSwitch: Switch
    private var appliedTheme = Prefs.THEME_SYSTEM

    override fun onCreate(savedInstanceState: Bundle?) {
        AppTheme.apply(this)
        super.onCreate(savedInstanceState)
        appliedTheme = Prefs.getTheme(this)
        selected = Prefs.getSelected(this).toMutableSet()
        allApps = loadApps()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(8))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "Keep Apps Awaken"
            textSize = 22f
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(Button(this).apply {
            text = "Definições"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        })
        root.addView(header)

        statusText = TextView(this).apply { textSize = 13f }
        root.addView(statusText)

        serviceSwitch = Switch(this).apply {
            text = "Serviço ativo"
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(serviceSwitch)

        val search = EditText(this).apply { hint = "Pesquisar aplicações" }
        root.addView(search)

        val list = ListView(this)
        adapter = AppAdapter(allApps)
        list.adapter = adapter
        root.addView(list, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        serviceSwitch.setOnCheckedChangeListener { _, on ->
            if (on) {
                if (Perms.hasUsageAccess(this)) {
                    Prefs.setEnabled(this, true)
                    AwakeService.start(this)
                } else {
                    serviceSwitch.isChecked = false
                    Perms.requestNext(this)
                }
            } else {
                Prefs.setEnabled(this, false)
                AwakeService.stop(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Prefs.getTheme(this) != appliedTheme) {
            recreate()
            return
        }
        serviceSwitch.isChecked = Prefs.isEnabled(this)
        updateStatus()
    }

    private fun updateStatus() {
        val perms = if (Perms.allGranted(this)) "OK" else "em falta (ver Definições)"
        statusText.text = "Permissões: $perms  |  Selecionadas: ${selected.size}"
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        return pm.getInstalledApplications(0)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .filter { it.packageName != packageName }
            .map { AppItem(it.loadLabel(pm).toString(), it.packageName, it.loadIcon(pm)) }
            .sortedBy { it.label.lowercase() }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    inner class AppAdapter(private val source: List<AppItem>) : BaseAdapter() {
        private var shown: List<AppItem> = source

        fun filter(q: String) {
            shown = if (q.isBlank()) source
            else source.filter { it.label.contains(q, true) || it.pkg.contains(q, true) }
            notifyDataSetChanged()
        }

        override fun getCount() = shown.size
        override fun getItem(position: Int) = shown[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = shown[position]
            val row = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(6), 0, dp(6))
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(ImageView(this@MainActivity).apply {
                setImageDrawable(item.icon)
            }, LinearLayout.LayoutParams(dp(40), dp(40)))

            val name = TextView(this@MainActivity).apply {
                text = item.label
                setPadding(dp(12), 0, 0, 0)
            }
            row.addView(name, LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            val cb = CheckBox(this@MainActivity).apply {
                isChecked = item.pkg in selected
                setOnCheckedChangeListener { _, on ->
                    if (on) selected.add(item.pkg) else selected.remove(item.pkg)
                    Prefs.setSelected(this@MainActivity, selected)
                    updateStatus()
                }
            }
            row.addView(cb)
            row.setOnClickListener { cb.toggle() }
            return row
        }
    }
}