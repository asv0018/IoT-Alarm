package com.thinkfinitylabs.iotalarm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat

class MyAlarms : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_alarms)


    }


    override fun onBackPressed() {
        finish()
    }
}

class ListViewAlarms(var time:String, var days:String, var switchState:Boolean)

class ListViewAdapter(private val getContext:Context,private val customListItem:ArrayList<ListViewAlarms>):ArrayAdapter<ListViewAlarms>(getContext,0,customListItem){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listLayout = convertView
        val holder:ViewHolder
        if(listLayout == null){
            val inflateList = (getContext as Activity).layoutInflater
            listLayout = inflateList.inflate(R.layout.item_vieww_for_my_alarm,parent,false)
            holder = ViewHolder()
            holder.buttonState = listLayout.findViewById(R.id.set_on_off)
            holder.alarmDays = listLayout.findViewById(R.id.show_days)
            holder.timerAlarm = listLayout.findViewById(R.id.show_time)
            listLayout.setTag(holder)
        }else{
            holder = listLayout.tag as ViewHolder
        }
        val listItem = customListItem[position]
        holder.timerAlarm?.text = listItem.time
        holder.alarmDays?.text = listItem.days
        holder.buttonState?.isChecked = listItem.switchState
        holder.buttonState?.setOnClickListener { 
            Toast.makeText(getContext,"thisv $position",Toast.LENGTH_SHORT).show()
        }
        return listLayout!!
    }

    class ViewHolder{
        internal var timerAlarm:TextView? = null
        internal var alarmDays:TextView? = null
        internal var buttonState: Switch? = null
    }
}