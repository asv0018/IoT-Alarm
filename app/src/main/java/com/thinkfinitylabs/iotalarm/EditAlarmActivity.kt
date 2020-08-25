package com.thinkfinitylabs.iotalarm

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.thinkfinitylabs.iotalarm.ScheduledActivity.Companion.keyOfChildCompanionObj
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_edit_alarm.*
import kotlinx.android.synthetic.main.activity_set_alarm.*
import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

class EditAlarmActivity : AppCompatActivity() {
    var defaultSong = "https://firebasestorage.googleapis.com/v0/b/iot-alarm-cf8df.appspot.com/o/music_files%2Fdefault.mp3?alt=media&token=4d59a0ae-2b4b-459c-bd57-b5b5c7bdf47c"
    lateinit var query: DatabaseReference
    lateinit var uri: Uri
    lateinit var uid:String
    var musicSelected = false
    lateinit var progressDialog: ProgressDialog
    var setEverytime = false
    var setOnce = false
    val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val checkedItems = booleanArrayOf(false, false, false, false, false, false, false)
    lateinit var alreadySelectedSong:String
    var selectedDays : MutableList<String> =  mutableListOf<String>()
    val REQ_CODE_PICK_SOUNDFILE = 1011
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)
        var keyOfChild = intent.getStringExtra("keyOfAlarm").toString()
        if(keyOfChild == null){
            keyOfChild = keyOfChildCompanionObj
        }
        time_picker_edit.setIs24HourView(true)
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(false)
        uid = FirebaseAuth.getInstance().uid.toString()
        query = FirebaseDatabase.getInstance().reference.child("alarms_users/$uid/$keyOfChild")
        attachValueEventListener()
        goBackButton_edit.setOnClickListener {
            confirmToGoBack()
        }
        selectDaysButton_edit.setOnClickListener {
            selectDays()
        }
        setMusicButton_edit.setOnClickListener {
            progressDialog.setMessage("Please wait while we open file manager...")
            progressDialog.show()
            setMusic()
        }

        save_and_close_edit.setOnClickListener {
            val name = name_of_alarm_edit.text.toString().trimStart().trimEnd()
            if(name.isBlank()){
                Toasty.warning(this@EditAlarmActivity,"The Label cannot be left blank or filled with spaces",Toast.LENGTH_SHORT).show()
            }else{
                var everytime = false
                if(setEverytime) {
                    everytime = true
                }
                val hours = time_picker_edit.hour
                val mins = time_picker_edit.minute
                if(!musicSelected){
                    if(selectedDays.size < 1){
                        Toasty.warning(this@EditAlarmActivity,"You cant leave days empty, select the days.",Toast.LENGTH_SHORT).show()
                    }else {
                        progressDialog.setMessage("Please wait while we update cloud. hang on for a moment please...")
                        progressDialog.show()
                        var db =
                            FirebaseDatabase.getInstance().reference.child("alarms_users/$uid/$keyOfChild")
                        var hashmap = HashMap<String, Any>()
                        hashmap["name"] = name
                        hashmap["music"] = alreadySelectedSong
                        hashmap["hours"] = hours
                        hashmap["minutes"] = mins
                        hashmap["days_selected"] = selectedDays
                        hashmap["everytime"] = everytime
                        hashmap["play"] = true
                        hashmap["snooze"] = false
                        db.updateChildren(hashmap)
                            .addOnCompleteListener {
                                progressDialog.dismiss()
                                finish()
                            }
                            .addOnFailureListener {
                                Toasty.error(
                                    this,
                                    "Please try again, after sometime",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressDialog.dismiss()
                            }
                    }
                }else{
                    if(selectedDays.size < 1){
                        Toasty.warning(this@EditAlarmActivity,"You cant leave days empty, select the days.",Toast.LENGTH_SHORT).show()
                    }else{
                        progressDialog.setTitle("Please wait while we update cloud.")
                        progressDialog.setMessage("we are working on it. hang on for a moment please...")
                        progressDialog.show()
                        val storageRef = FirebaseStorage.getInstance().reference.child("alarms_users/$uid/$keyOfChild")
                        val uploadTask = storageRef.putFile(uri)
                        val task = uploadTask.continueWithTask{
                            val downloadUrl = it.result
                            val uri = downloadUrl.storage.downloadUrl
                            while(!uri.isComplete);
                            val url = uri.result.toString()
                            var db = FirebaseDatabase.getInstance().reference.child("alarms_users/$uid/$keyOfChild")
                            var hashmap = HashMap<String,Any>()
                            hashmap["name"] = name
                            hashmap["music"] = url
                            hashmap["hours"] = hours
                            hashmap["minutes"] = mins
                            hashmap["days_selected"] = selectedDays
                            hashmap["everytime"] = everytime
                            hashmap["play"] = true
                            hashmap["snooze"] = false
                            db.updateChildren(hashmap)
                                .addOnCompleteListener {
                                    progressDialog.dismiss()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toasty.error(this,"Please try again, after sometime",Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()
                                }
                        }
                    }

                }
            }
        }
    }

    override fun onBackPressed() {
        confirmToGoBack()
    }

    private fun setMusic() {
        val intent: Intent
        intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "audio/*"
        startActivityForResult(
            Intent.createChooser(
                intent,"Select any music file less than 4MB"
            ), REQ_CODE_PICK_SOUNDFILE
        )
    }

    private fun attachValueEventListener(){
        progressDialog.setMessage("Hang on, we are fetching alarm...")
        query.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    val temp = p0.child("name").value.toString().trimEnd()
                    name_of_alarm_edit.setText(temp)
                    for (j in p0.child("days_selected").children) {
                        checkedItems[days.indexOf(j.value.toString())] = true
                        selectedDays.add(j.value.toString())
                    }
                    Log.d("MONITER","selected days "+selectedDays.toString())
                    Log.d("MONITER","the truth value "+checkedItems.toString())
                    Log.d("MONITER","The table key is "+days.toString())
                    val data:HashMap<String,Long> = p0.value as HashMap<String, Long>
                    time_picker_edit.hour = (data["hours"] as Long).toInt()
                    time_picker_edit.minute = (data["minutes"] as Long).toInt()
                    alreadySelectedSong = p0.child("music").value.toString()
                }else{
                    progressDialog.dismiss()
                    Toasty.error(this@EditAlarmActivity,"Sorry, alarm data not found...",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("DEBUGGING","Data cancelled in profile fragment")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        progressDialog.dismiss()
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val audioFileUri: Uri? = data.data

                // Now you can use that Uri to get the file path, or upload it, ...
                if (audioFileUri != null) {
                    val file = File(audioFileUri.toString())
                    val bytes = file.length()
                    if(bytes < (4*1024*1024)){
                        //Toast.makeText(this@EditAlarmActivity,audioFileUri.toString(),Toast.LENGTH_LONG).show()
                        //Toasty.warning(this,"The size of file is $bytes",Toast.LENGTH_SHORT).show()
                        uri = audioFileUri
                        musicSelected = true
                    }else{
                        //Toasty.warning(this,"Please select a music file lesser than 4MB",Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }

    private fun confirmToGoBack() {
        // Set up the alert builder
        val builder = AlertDialog.Builder(this@EditAlarmActivity)
        builder.setTitle("Dont want to update alarm ?")
        builder.setMessage("If you wish to discard unsaved settings, you can press Discard button.")
        // Add OK and Cancel buttons
        builder.setPositiveButton("Discard") { dialog, which ->
            // The user clicked OK
            finish()
        }
        builder.setNegativeButton("Cancel") {dialog, which ->

        }

        // Create and show the alert dialog
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun selectDays() {
        // Set up the alert builder
        val builder = AlertDialog.Builder(this@EditAlarmActivity)
        builder.setTitle("Select Days")

        // Add a checkbox list

        builder.setMultiChoiceItems(days, checkedItems) { dialog, which, isChecked ->
            // The user checked or unchecked a box
            if(isChecked){
                selectedDays.add(days[which])
            }else if(selectedDays.contains(days[which])){
                selectedDays.removeAt(selectedDays.indexOf(days[which]))
            }
        }

        // Add OK and Cancel buttons
        builder.setPositiveButton("Set once") { dialog, which ->
            // The user clicked OK
            Toasty.info(this@EditAlarmActivity,"Set to ring once", Toast.LENGTH_LONG).show()
            setOnce = true
            setEverytime = false
        }
        builder.setNegativeButton("Set every time") {dialog, which ->
            Toasty.info(this@EditAlarmActivity,"Set to ring every time", Toast.LENGTH_LONG).show()
            setEverytime = true
            setOnce = false
        }

        // Create and show the alert dialog
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun RandomNumber(): String {
        return LocalDateTime.now().toString().slice(0..18)
    }
}