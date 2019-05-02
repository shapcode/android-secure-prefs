package com.shapcode.prefs.demo

import android.arch.lifecycle.*
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import com.shapcode.prefs.SecurePreferences
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var securePreferences: SharedPreferences
    lateinit var sharedPreferences: SharedPreferences

    lateinit var securePreferenceEntries: SharedPreferenceEntriesLiveData
    lateinit var sharedPreferenceEntries: SharedPreferenceEntriesLiveData

    var viewDecrypted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("demo", Context.MODE_PRIVATE)
        securePreferences = SecurePreferences(this, "demo")

        sharedPreferenceEntries = SharedPreferenceEntriesLiveData(sharedPreferences)
        securePreferenceEntries = SharedPreferenceEntriesLiveData(securePreferences)

        val recycler = findViewById<RecyclerView>(R.id.entries)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = Adapter(sharedPreferences.all.entries.toList())

        securePreferenceEntries.observe(this, Observer {
            if (viewDecrypted) {
                recycler.adapter = Adapter(it?.entries?.toList() ?: listOf())
            }
        })

        sharedPreferenceEntries.observe(this, Observer {
            if (!viewDecrypted) {
                recycler.adapter = Adapter(it?.entries?.toList() ?: listOf())
            }
        })

        add.setOnClickListener {
            securePreferences.edit()
                .putString(input_key.text.toString(), input_value.text.toString())
                .apply()
        }

        view_decrypted.setOnCheckedChangeListener { button: CompoundButton, isChecked: Boolean ->
            viewDecrypted = isChecked
            if (viewDecrypted) {
                recycler.adapter = Adapter(securePreferences.all.entries.toList())
            } else {
                recycler.adapter = Adapter(sharedPreferences.all.entries.toList())
            }
        }

    }

}

class Adapter(val items: List<Map.Entry<String, *>>) : RecyclerView.Adapter<Adapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1 = itemView.findViewById<TextView>(R.id.text1)
        val text2 = itemView.findViewById<TextView>(R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(viewHolder: VH, position: Int) {
        val item = items[position]
        viewHolder.text1.text = item.key
        viewHolder.text2.text = item.value as String
    }

}

class SharedPreferenceEntriesLiveData(private val sharedPreferences: SharedPreferences) : LiveData<Map<String, *>>() {

    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        postValue(sharedPreferences.all)
    }

    override fun onActive() {
        super.onActive()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    override fun onInactive() {
        super.onInactive()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

}