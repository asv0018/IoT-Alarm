package com.thinkfinitylabs.iotalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Insets.add
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_signin.*
import java.util.jar.Manifest
import java.util.zip.Inflater


class EditProfile : AppCompatActivity() {
    lateinit var auth : FirebaseAuth
    //Creating member variables
    var refUsers: DatabaseReference? = null
    var refUsersMain: DatabaseReference? = null
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null
    lateinit var vibrator: Vibrator

    private val RequestCode = 438
    var imageuri: Uri? = null
    private var StorageRef: StorageReference? = null

    var firebaseUser: FirebaseUser?= null
    lateinit var username: EditText
    lateinit var email: Button
    lateinit var phoneno:EditText
    lateinit var fullname:EditText
    lateinit var github:EditText
    lateinit var photo:CircleImageView
    var emaill : String? = null
    var skills_present : MutableList<String>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        photo = findViewById(R.id.edit_image)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        StorageRef = FirebaseStorage.getInstance().reference.child("user_profile_pictures")
        refUsers = FirebaseDatabase.getInstance().reference.child("user_account").child(firebaseUser!!.uid)
        val refUser = FirebaseDatabase.getInstance().reference.child("user_account").child(firebaseUser!!.uid)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        refUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    edit_firstname.text = p0.child("fullname").value.toString().toEditable()
                    val imageUrl = p0.child("profile_pic_url").value.toString()
                    if (imageUrl != "") {
                        Picasso.get().load(imageUrl).into(edit_image)
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("DEBUGGING", "Data cancelled in profile fragment")
            }
        })

        password_edit.setOnClickListener {
            var email = FirebaseAuth.getInstance().currentUser?.email.toString()
                val dialogBuilder = android.app.AlertDialog.Builder(this)
                // set message of alert dialog
                dialogBuilder.setMessage("Click on 'Continue' to Reset the Password")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Continue", DialogInterface.OnClickListener { dialog, id ->
                        var auth = FirebaseAuth.getInstance()
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(this, OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toasty.success(
                                        this@EditProfile,
                                        "Reset link sent to your email",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else {
                                    Toasty.info(
                                        this@EditProfile,
                                        "Unable to send re-enter mail",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })

                    })
                    // negative button text and action
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Reset Password")
                // show alert dialog
                alert.show()

        }

        update_image.setOnClickListener {
            pickImage()
        }

        update_profile.setOnClickListener {
            val fullname = edit_firstname.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(fullname)){
                vibrator.vibrate(60)
                update(fullname)
            }else{
                Toasty.warning(this@EditProfile,"Full name cannot be left blank",Toast.LENGTH_LONG).show()
            }
        }


    }
    private fun update(full_name:String) {
        // Create new post at /user-posts/$userid/$postid and at
        val firebaseuid= FirebaseAuth.getInstance().currentUser!!.uid
        val database=FirebaseDatabase.getInstance().getReference("users/$firebaseuid")
        val child_Updates=HashMap<String,Any>()
        child_Updates["fullname"]=full_name
        database.updateChildren(child_Updates)
            .addOnSuccessListener {
                Toasty.success(this@EditProfile,"profile updated successfully",Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener{
                Toasty.error(this@EditProfile,"failed to update profile",Toast.LENGTH_SHORT).show()

            }
        Toasty.success(this@EditProfile,"profile updated successfully",Toast.LENGTH_SHORT).show()
    }
    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null)
        {
            imageuri = data.data
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase()
    {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("Image is uploading, please wait!")
        progressBar.show()

        if (edit_image!=null)
        {

            val fileRef = StorageRef!!.child(firebaseUser!!.uid.toString())

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageuri!!)


            uploadTask.continueWithTask(Continuation < UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful)
                {
                    task.exception?.let{
                        throw it

                    }
                }
                return@Continuation fileRef.downloadUrl
            } ).addOnCompleteListener{ task ->
                if (task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val mUri = downloadUrl.toString()
                    val mapProfileImg = HashMap<String, Any>()
                    mapProfileImg["profile_pic_url"] = mUri
                    refUsers!!.updateChildren(mapProfileImg)

                    progressBar.dismiss()
                }
            }
        }

    }
    override fun onBackPressed() {
        val alertBox = android.app.AlertDialog.Builder(this@EditProfile)
        alertBox.setTitle("Do you wish discard the edit to your profile?")
        alertBox.setIcon(R.mipmap.ic_launcher)
        alertBox.setMessage("By clicking yes, the changes shall be discarded")
        alertBox.setCancelable(true)
        alertBox.setPositiveButton("yes") { _, _ ->
            finish()        }
        alertBox.setNegativeButton("No") { _, _ ->

        }
        alertBox.create().show()

    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}