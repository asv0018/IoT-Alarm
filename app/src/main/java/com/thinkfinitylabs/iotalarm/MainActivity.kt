package com.thinkfinitylabs.iotalarm

import MyMainActivityAdapter
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var progressDialog:ProgressDialog
    lateinit var vibrator: Vibrator
    lateinit var userUID: String
    lateinit var userAuth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var mStorageRef:FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait, Loading your data...")
        progressDialog.show()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        userAuth = FirebaseAuth.getInstance()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (userAuth.currentUser == null) {
            startActivity(Intent(this@MainActivity, Signin::class.java))
            finish()
        }else{
            userUID = FirebaseAuth.getInstance().currentUser?.uid.toString()
            database = FirebaseDatabase.getInstance()
            mStorageRef = FirebaseStorage.getInstance()
            CheckDBifUIDDataIsPresent(userUID)
        }

        val arrayListImage = ArrayList<Int>()
        arrayListImage.add(R.drawable.next_alarm)
        arrayListImage.add(R.drawable.my_alarms)
        arrayListImage.add(R.drawable.my_controllies)
        arrayListImage.add(R.drawable.my_controller)
        arrayListImage.add(R.drawable.edit_profile)
        arrayListImage.add(R.drawable.sign_out)

        val arrayListName = arrayOf(
            "IoT Device",
            "My Alarms",
            "My Mentees",
            "My Mentor",
            "Edit Profile",
            "Sign out"
        )
        grid_view.adapter = MyMainActivityAdapter(this@MainActivity,arrayListImage,arrayListName)

        grid_view.setOnItemClickListener { adapterView, parent, position, l ->
            vibrator.vibrate(60)
            when(arrayListName[position]){
                "IoT Device"->{
                    startActivity(Intent(this@MainActivity,MyAlarms::class.java))
                }
                "My Mentees"->{
                    startActivity(Intent(this@MainActivity,MyMentees::class.java))
                }
                "My Mentor"->{
                    startActivity(Intent(this@MainActivity,MyMentors::class.java))
                }
                "My Alarms"->{
                    startActivity(Intent(this@MainActivity,ScheduledActivity::class.java))
                }
                "Edit Profile"->{
                    startActivity(Intent(this@MainActivity,EditProfile::class.java))
                }
                "Sign out"->{
                    val alertBox = AlertDialog.Builder(this@MainActivity)
                    alertBox.setTitle("Are you sure you want to logout from the app?")
                    alertBox.setIcon(R.mipmap.ic_launcher)
                    alertBox.setMessage("By pressing on Logout, you have to sign in again inorder to use the app. Do you wish to continue ?")
                    alertBox.setCancelable(true)
                    alertBox.setPositiveButton("Logout") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        Toasty.success(
                            this@MainActivity,
                            "You have successfully logged out",
                            Toast.LENGTH_SHORT
                        ).show()
                        finishAffinity()
                        startActivity(Intent(this@MainActivity, Signin::class.java))
                        finish()
                    }
                    alertBox.setNegativeButton("stay here") { _, _ ->

                    }
                    alertBox.create().show()
                }
            }
        }
    }


    private fun CheckDBifUIDDataIsPresent(userUid:String){
        val refUsers = FirebaseDatabase.getInstance().reference
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                    if(p0.hasChild("user_account/$userUid")) {
                        val username = p0.child("user_account/$userUid/fullname").value.toString()
                        val url_for_profilePic = p0.child("user_account/$userUid/profile_pic_url").value.toString()
                        Toasty.success(this@MainActivity,"Welcome $username", Toast.LENGTH_SHORT).show()
                        Picasso.get().load(url_for_profilePic).into(user_image_logo)
                        welcome_user.text = "Hey $username"
                        progressDialog.dismiss()
                    }else{
                        progressDialog.dismiss()
                    startActivity(Intent(this@MainActivity,FillTheData::class.java))
                    finish()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("DEBUGGING","Data cancelled in profile fragment")

            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_signout->{
                val alertBox = AlertDialog.Builder(this@MainActivity)
                alertBox.setTitle("Are you sure you want to logout from the app?")
                alertBox.setIcon(R.mipmap.ic_launcher)
                alertBox.setMessage("You may have to login again to use the application. Do you wish to continue ?")
                alertBox.setCancelable(true)
                alertBox.setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    Toasty.success(
                        this@MainActivity,
                        "You have successfully logged out",
                        Toast.LENGTH_SHORT
                    ).show()
                    finishAffinity()
                    startActivity(Intent(this@MainActivity, Signin::class.java))
                    finish()
                }
                alertBox.setNegativeButton("stay here") { _, _ ->

                }
                alertBox.create().show()
            }
            R.id.action_exit->{
                val alertBox = AlertDialog.Builder(this@MainActivity)
                alertBox.setTitle("Do you wish to exit from the app?")
                alertBox.setIcon(R.mipmap.ic_launcher)
                alertBox.setMessage("By clicking Exit, the app will be closed")
                alertBox.setCancelable(true)
                alertBox.setPositiveButton("Exit") { _, _ ->
                    finish()
                }
                alertBox.setNegativeButton("No") { _, _ ->

                }
                alertBox.create().show()
            }
            }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val alertBox = AlertDialog.Builder(this@MainActivity)
        alertBox.setTitle("Do you wish to exit from the app?")
        alertBox.setIcon(R.mipmap.ic_launcher)
        alertBox.setMessage("By clicking Exit, the app will be closed")
        alertBox.setCancelable(true)
        alertBox.setPositiveButton("Exit") { _, _ ->
            finish()
        }
        alertBox.setNegativeButton("No") { _, _ ->

        }
        alertBox.create().show()
    }
}

