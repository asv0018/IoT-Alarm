package com.thinkfinitylabs.iotalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_fill_the_data.*
import java.text.SimpleDateFormat
import java.util.*

class FillTheData : AppCompatActivity() {
    var mydate = ""
    var gender:String = ""
    private lateinit var database: DatabaseReference
    private lateinit var myref:DatabaseReference
    lateinit var progressDialog:ProgressDialog
    lateinit var vibrator: Vibrator
    lateinit var auth:FirebaseAuth
    private var choosen_image_uri: Uri? = null
    private var firebaseUserID: String  = ""
    private lateinit var refUsersChat: DatabaseReference
    var image_choosen = false
    private var mStorageRef: StorageReference? = null
    var button_date: Button? = null
    var textview_date: TextView? = null
    var cal = Calendar.getInstance()
    lateinit var myRef:DatabaseReference
    val default_image_url = "https://firebasestorage.googleapis.com/v0/b/iot-alarm-cf8df.appspot.com/o/user_profile_pictures%2Fgraavatar.png?alt=media&token=939d8e0b-b873-45e5-826a-02476b6decbc"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_the_data)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        //setSupportActionBar(toolbar_register)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        auth = FirebaseAuth.getInstance()
        phoneno_entry.text = FirebaseAuth.getInstance().currentUser?.email.toString()
        database = FirebaseDatabase.getInstance().getReference("user_account")
        mStorageRef = FirebaseStorage.getInstance().reference.child("user_profile_pictures")
        // THIS IS FOR RADIO BUTTON
        gender_radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.male -> {
                    gender = "male"
                }
                R.id.female -> {
                    gender = "female"
                }
                R.id.other -> {
                    gender = "other"
                }
            }
        }
        // THIS IS FOR SIGN UP BUTTON
        save_continue.setOnClickListener {
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Hold on, completing signup process for you ...")
            progressDialog.setCanceledOnTouchOutside(false)
            vibrator.vibrate(60)
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Hold on, getting a account for you ...")
            progressDialog.setCanceledOnTouchOutside(false)
            if((!firstname_entry.text.isNullOrBlank()) and (firstname_entry.text.toString()!=" ")){
                val firstname = firstname_entry.text.toString()
                if(!lastname_entry.text.isNullOrBlank() and (lastname_entry.text.toString()!=" ")){
                    val lastname = lastname_entry.text.toString()
                    val fullname = "$firstname $lastname"
                    if(gender != ""){
                        if(mydate != ""){
                            if(image_choosen){
                                progressDialog.show()
                                choosen_image_uri?.let { it1 ->
                                    CreateUserData(auth.uid.toString(),fullname,gender,
                                        auth.currentUser?.email.toString(),mydate, it1
                                    )
                                }
                            }else{
                                progressDialog.show()
                                val userInfo = NewUserInfo(auth.uid.toString(),fullname,gender,
                                    auth.currentUser?.email.toString(),mydate,default_image_url)
                                database.child(FirebaseAuth.getInstance().uid.toString()).setValue(userInfo)
                                    .addOnSuccessListener {
                                        // write was successful
                                        Log.d("FETCH_ERROR","data to asv db done")
                                        Log.d("DEBUG", "database created")
                                        progressDialog.dismiss()
                                        Toasty.success(
                                            this@FillTheData,
                                            "hey $fullname, your account is all set!, you are ready to go",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("FETCH_ERROR","GONNA INTeNT TO LOGIN ACTIVITY")
                                        startActivity(Intent(this@FillTheData, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        // write was failure
                                        Log.d("FETCH_ERROR","failed here,,,,,, chack this")
                                        progressDialog.dismiss()
                                        Log.d("DEBUG", "database creation failed")
                                        Toasty.error(
                                            this@FillTheData,
                                            "failed to create account, try again in some time!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //finishAffinity()
                                        //startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                                        //finish()
                                    }
                            }
                        }else{
                            Toasty.warning(this@FillTheData,"we need your birth date",Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toasty.warning(this@FillTheData,"gender is not specified",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toasty.warning(this@FillTheData,"lastname is empty",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toasty.warning(this@FillTheData,"Fullname is empty",Toast.LENGTH_SHORT).show()
            }
        }

        select_image_button.setOnClickListener {
            //check runtime permission
            vibrator.vibrate(60)

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("opening gallery")
            progressDialog.setCanceledOnTouchOutside(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied

                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, permission_code)
                } else {
                    //permission granted
                    progressDialog.show()
                    pickimagefromgallery()
                }
            }


        }

        // get the references from layout file



        // create an OnDateSetListener
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(
                view: DatePicker, year: Int, monthOfYear: Int,
                dayOfMonth: Int
            ) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date_1!!.setOnClickListener {
            vibrator.vibrate(60)
            DatePickerDialog(
                this@FillTheData,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

    }
    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        button_date_1!!.text = sdf.format(cal.getTime())
        mydate = sdf.format(cal.getTime())
    }

    private fun pickimagefromgallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, image_pick_code)

    }
    companion object {
        private val image_pick_code = 1000

        val permission_code = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permission_code -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    progressDialog.show()
                    pickimagefromgallery()
                } else {
                    Toasty.warning(
                        this@FillTheData,
                        "Permission denied!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        progressDialog.dismiss()
        if (resultCode == Activity.RESULT_OK && requestCode == image_pick_code) {
            user_profile_image.setImageURI(data?.data)
            choosen_image_uri = data?.data
            image_choosen = true
        }
    }

    override fun onBackPressed() {
        val alertBox = AlertDialog.Builder(this@FillTheData)
        alertBox.setTitle("Do you wish to abort completing sign up process?")
        alertBox.setIcon(R.mipmap.ic_launcher)
        alertBox.setMessage("Its ok, come back any time to complete your sign up process")
        alertBox.setCancelable(true)
        alertBox.setPositiveButton("yes") { _, _ ->
            finish()
        }
        alertBox.setNegativeButton("No") { _, _ ->

        }
        alertBox.create().show()
    }

    private fun CreateUserData(uid:String, fullname:String, gender:String, email:String, dob:String,file_Uri:Uri) {
        Log.d("FETCH_ERROR", "staging the image to be uploaded")
        val uploadTask = mStorageRef!!.child(auth.uid.toString()).putFile(file_Uri)
        Log.d("FETCH_ERROR", "upload task given")
        val task = uploadTask.continueWithTask { task ->
            Log.d("FETCH_ERROR", "along with uploading task i am into another procedure as well")
            val downloadUrl = task.result
            //val url = downloadUrl!!.toString()
            // THIS IS HOW I AM GETTING URL TO DOWLOAD IMAGE
            val uri: Task<Uri> = downloadUrl.storage.downloadUrl
            while (!uri.isComplete);
            val url: Uri = uri.result
            Log.d("FETCH_ERROR", "Img url to be downloaded is obtained")
            val userInfo = NewUserInfo(uid,fullname,gender,email,dob,url.toString())
            Log.d("FETCH_ERROR","data for sending to asv's database is set")
            database.child(uid).setValue(userInfo)
                .addOnSuccessListener {
                    // write was successful
                    Log.d("FETCH_ERROR","data to asv db done")
                    Log.d("DEBUG", "database created")
                    progressDialog.dismiss()
                    Toasty.success(
                        this@FillTheData,
                        "hey $fullname, your account is all set!, you are ready to go",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("FETCH_ERROR","GONNA INTeNT TO LOGIN ACTIVITY")
                    startActivity(Intent(this@FillTheData, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    // write was failure
                    Log.d("FETCH_ERROR","failed here,,,,,, chack this")
                    progressDialog.dismiss()
                    Log.d("DEBUG", "database creation failed")
                    Toasty.error(
                        this@FillTheData,
                        "failed to create account, try again in some time!",
                        Toast.LENGTH_SHORT
                    ).show()
                    //finishAffinity()
                    //startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                    //finish()
                }

            mStorageRef!!.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FETCH_ERROR","EVERYTHING IS DONE DA ANYTHING PENDING THEN DEBUG")
            }
        }

    }





}
// This is the dataclass for new user info
data class NewUserInfo(var uid:String,var fullname:String,var gender:String,var email:String,var dob:String,var profile_pic_url:String)