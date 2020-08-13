package com.thinkfinitylabs.iotalarm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scheduled.*

class ScheduledActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled)
        val customListData = ArrayList<ListViewAlarms>()
        val customList = ListViewAdapter(this,customListData)
        customListData.add(ListViewAlarms("10:40 AM","Monday,Tuesday,Saturday",true))
        customListData.add(ListViewAlarms("9:50 PM","Sunday,Tuesday,Wednesday",false))
        customListData.add(ListViewAlarms("10:40 AM","Monday,Tuesday,Wednesday,Sunday\n,Thursday,Friday,Saturday",true))
        customListData.add(ListViewAlarms("11:50 AM","Sunday,Tuesday,Wednesday",false))
        list_view_view.adapter = customList
        list_view_view.onItemClickListener = AdapterView.OnItemClickListener{
            parent, view, position, id ->
            Log.d("IAM","I AM HERE")
            Toast.makeText(this@ScheduledActivity,"Item short clicked :- $id",Toast.LENGTH_SHORT).show()
        }
        list_view_view.onItemLongClickListener = OnItemLongClickListener {parent, view, position, id ->
            Toast.makeText(this@ScheduledActivity,"Item long clicked :- $id",Toast.LENGTH_SHORT).show()
            true
        }

        on_backPressedBtn.setOnClickListener {
            finish()
        }

        add_alarm_button.setOnClickListener {
            startActivity(Intent(this@ScheduledActivity,SetAlarm::class.java))
        }
    }







    override fun onBackPressed() {
        finish()
    }
}