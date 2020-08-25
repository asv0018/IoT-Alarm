package com.thinkfinitylabs.iotalarm

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.toasty.Toasty


class ListViewAlarms(var time:String, var days:String, var switchState:Boolean)

class ListViewAdapter(private val getContext: Context, private val customListItem:ArrayList<ListViewAlarms>):
    ArrayAdapter<ListViewAlarms>(getContext,0,customListItem){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listLayout = convertView
        val holder:ViewHolder
        val progressDialog = ProgressDialog(getContext)
        if(listLayout == null){
            val inflateList = (getContext as Activity).layoutInflater
            listLayout = inflateList.inflate(R.layout.item_vieww_for_my_alarm,parent,false)
            holder = ViewHolder()
            holder.buttonState = listLayout.findViewById(R.id.set_on_off)
            holder.alarmDays = listLayout.findViewById(R.id.show_days)
            holder.timerAlarm = listLayout.findViewById(R.id.show_time)
            listLayout.tag = holder
        }else{
            holder = listLayout.tag as ViewHolder
        }
        val listItem = customListItem[position]
        holder.timerAlarm?.text = listItem.time
        holder.alarmDays?.text = listItem.days
        holder.buttonState?.isChecked = listItem.switchState
        holder.buttonState?.setOnClickListener {
            progressDialog.setMessage("Updating, hang on for a second...")
            progressDialog.show()
            val uid = FirebaseAuth.getInstance().uid.toString()
            val db = FirebaseDatabase.getInstance().reference.child("alarms_users/$uid").child(
                ScheduledActivity.allKeysOfUid[position])
            val hashMap = HashMap<String,Any?>()
            val prevCondition = holder.buttonState?.isChecked
            hashMap["play"] = holder.buttonState?.isChecked
            db.updateChildren(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toasty.error(getContext,"Unable to set the alarm to $prevCondition, try after sometime.",
                        Toast.LENGTH_SHORT).show()
                    holder.buttonState?.isChecked = !prevCondition!!
                }
        }
        return listLayout!!
    }

    class ViewHolder{
        internal var timerAlarm: TextView? = null
        internal var alarmDays: TextView? = null
        internal var buttonState: Switch? = null
    }
}