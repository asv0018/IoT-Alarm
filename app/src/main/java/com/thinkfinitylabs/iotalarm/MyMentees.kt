package com.thinkfinitylabs.iotalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MyMentees : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_mentees)
    }



    override fun onBackPressed() {
        finish()
    }
}