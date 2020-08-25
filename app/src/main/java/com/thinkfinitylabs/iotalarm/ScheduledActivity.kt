package com.thinkfinitylabs.iotalarm

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_scheduled.*


class ScheduledActivity : AppCompatActivity() {
    companion object {
        var allKeysOfUid = mutableListOf<String>()
        lateinit var keyOfChildCompanionObj:String
    }
    private var mValueEventListener: ValueEventListener? = null
    lateinit var uid:String
    lateinit var query:DatabaseReference
    lateinit var customList:ListViewAdapter
    var customListData = ArrayList<ListViewAlarms>()
    lateinit var progressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled)
        uid = FirebaseAuth.getInstance().uid.toString()
        query = FirebaseDatabase.getInstance().reference.child("alarms_users/$uid")

        customList = ListViewAdapter(this,customListData)
        //customListData.add(ListViewAlarms("10:40 AM","Monday,Tuesday,Wednesday,Sunday\n,Thursday,Friday,Saturday",true))
        list_view_view.adapter = customList

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait while we load your alarms")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()


        list_view_view.onItemClickListener = AdapterView.OnItemClickListener{
            parent, view, position, id ->
            keyOfChildCompanionObj = allKeysOfUid[id.toInt()]
            startActivity(Intent(this@ScheduledActivity,EditAlarmActivity::class.java).putExtra("keyOfAlarm",allKeysOfUid[id.toInt()]))
        }

        list_view_view.onItemLongClickListener = OnItemLongClickListener {parent, view, position, id ->
            confirmDelete(id.toInt())
            true
        }

        on_backPressedBtn.setOnClickListener {
            finish()
        }

        add_alarm_button.setOnClickListener {
            startActivity(Intent(this@ScheduledActivity,SetAlarm::class.java))
        }


    }



    override fun onStart() {
        super.onStart()
        // add an event listener to watch changes in firebase database
        attachValueEventListener()
    }

    private fun detachValueEventListener() {
        if (mValueEventListener != null) {
            query.removeEventListener(mValueEventListener!!)
            mValueEventListener = null
        }
    }

    private fun confirmDelete(id:Int) {
        // Set up the alert builder
        val builder = AlertDialog.Builder(this@ScheduledActivity)
        builder.setTitle("Delete the alarm ?")
        builder.setMessage("You can not reverse the action.")
        // Add OK and Cancel buttons
        builder.setPositiveButton("Delete") { dialog, which ->
            // The user clicked OK
            progressDialog.setMessage("Deleting alarm on the cloud. please hang on for few seconds...")
            progressDialog.show()
            query.child(allKeysOfUid[id]).removeValue()
                .addOnSuccessListener {
                    //customListData.removeAt(id)
                    //allKeysOfUid.removeAt(id)
                    customList.notifyDataSetChanged()
                    progressDialog.dismiss()
                    Toasty.success(this@ScheduledActivity,"Alarm deleted Successfully",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toasty.error(this@ScheduledActivity,"Failed to delete Alarm",Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancel") {dialog, which ->

        }

        // Create and show the alert dialog
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun attachValueEventListener(){
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    customListData.clear()
                    customList.notifyDataSetChanged()
                    if (p0.hasChildren()) {
                        for (i in p0.children) {
                            allKeysOfUid.add(i.key.toString())
                            var repeatEverytime = i.child("everytime").value.toString()
                            var hours = i.child("hours").value.toString()
                            var mins = i.child("minutes").value.toString()
                            if(hours.length<2){
                                hours = "0$hours"
                            }
                            if(mins.length<2){
                                mins = "0$mins"
                            }
                            val time = "$hours:$mins"
                            val arrayDays = i.child("days_selected")
                            val switchState = i.child("play").value.toString()
                            var state = false
                            if (switchState == "true") {
                                state = true
                            }
                            val arrayOfDays = mutableListOf<String>()
                            for (j in arrayDays.children) {
                                arrayOfDays.add(j.value.toString())
                            }
                            var days =  arrayOfDays.toString().substring(1,arrayOfDays.toString().length -1)
                            if(repeatEverytime == "true"){
                                days = "Repeats Every $days"
                            }
                            customListData.add(ListViewAlarms(time,days,state))
                            customList.notifyDataSetChanged()
                        }
                        progressDialog.dismiss()
                    }
                }else{
                    progressDialog.dismiss()
                    Toasty.info(this@ScheduledActivity,"No alarms found",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("DEBUGGING","Data cancelled in profile fragment")
            }
        })
    }


    override fun onBackPressed() {
        finish()
    }
}