package com.thinkfinitylabs.iotalarm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.thinkfinitylabs.iotalarm.ScheduledActivity.Companion.allKeysOfUid
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_my_alarms.*

class MyAlarms : AppCompatActivity() {
    lateinit var query :FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_alarms)


    }

    private fun noAlarmPresent(){
        no_alarm.visibility = View.VISIBLE
        no_alarm_text.visibility = View.VISIBLE
        timr_clock.visibility = View.INVISIBLE
        turnoff_alarm.visibility = View.INVISIBLE
        snooze_alarm.visibility = View.INVISIBLE
    }

    private fun alarmPresent(){
        no_alarm.visibility = View.INVISIBLE
        no_alarm_text.visibility = View.INVISIBLE
        timr_clock.visibility = View.VISIBLE
        turnoff_alarm.visibility = View.VISIBLE
        snooze_alarm.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        finish()
    }
}