package com.lsp.view.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.lsp.view.R
import java.util.*

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var name_sourceArray: Array<String>
    lateinit var url_sourceArray:Array<String>
    lateinit var configSp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        configSp =  getSharedPreferences("config", MODE_PRIVATE)

        name_sourceArray =  resources.getStringArray(R.array.pic_source)
        url_sourceArray = resources.getStringArray(R.array.url_source)

        val spinner = findViewById<Spinner>(R.id.source)
        //设置spinner列表
        val adapter = ArrayAdapter(this,R.layout.spinner_item_layout,name_sourceArray)
        adapter.setDropDownViewResource(R.layout.spinner_item_layout)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        val source =  configSp.getString("source",null)
        if (source!=null ){
            for ( (index,value) in url_sourceArray.withIndex()){
                if (value == source){
                    spinner.setSelection(index)
                }
            }
        }



    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        configSp.edit().putString("source",url_sourceArray[p2]).apply()
        if (p2==2){
            configSp.edit().putString("type","1").apply()
        }else{
            configSp.edit().putString("type","0").apply()

        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}