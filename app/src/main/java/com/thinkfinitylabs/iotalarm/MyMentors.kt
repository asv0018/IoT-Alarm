package com.thinkfinitylabs.iotalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MyMentors : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_mentors)
    }





    override fun onBackPressed() {
        finish()
    }
}