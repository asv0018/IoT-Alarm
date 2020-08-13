package com.thinkfinitylabs.iotalarm

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_signin.*



class Signin : AppCompatActivity() {
    lateinit var vibrator:Vibrator
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar?.hide()
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Hold your seat with patience...")
        progressDialog.setCanceledOnTouchOutside(false)

        // when button is clicked, show the alert
        forgot_password_button.setOnClickListener {
            // build alert dialog
            val email = username.getText().toString()
            if (!username.text.isNullOrBlank()){
                val dialogBuilder = AlertDialog.Builder(this)
                // set message of alert dialog
                dialogBuilder.setMessage("Click on 'Continue' to Reset the Password")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Continue", DialogInterface.OnClickListener { dialog, id ->

                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(this, OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toasty.success(
                                        this@Signin,
                                        "Reset link sent to your email",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else {
                                    Toasty.info(
                                        this@Signin,
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
            else{
                Toasty.info(this@Signin, "Please enter your email", Toast.LENGTH_LONG).show()
            }
        }
        signin_bton.setOnClickListener {
            if(!username.text.isNullOrBlank()) {
                if (!password.text.isNullOrBlank()) {
                    vibrator.vibrate(60)
                    progressDialog.show()
                    attemptToLogin()
                } else {
                    Toasty.warning(
                        this@Signin,
                        "You cannot leave the password empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                Toasty.warning(this@Signin,"Username/password is not provided",Toast.LENGTH_SHORT).show()
            }
        }

        // intent to register activity if user clicks on the register button
        signup_redirect.setOnClickListener {
            signup_redirect.isEnabled = false
            vibrator.vibrate(60)
            Log.d("DEBUG","User choose to register himself")
            startActivity(Intent(this@Signin, SignupActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }

    }

    private fun attemptToLogin(){
        Log.d("DEBUG","User choose to register himself")
        auth.signInWithEmailAndPassword(username.text.toString(), password.text.toString()).addOnCompleteListener(this, OnCompleteListener { task ->
            if(task.isSuccessful) {
                progressDialog.dismiss()
                checkIfEmailVerified()
            }else {
                progressDialog.dismiss()
                Toasty.error(this, "Hey! Login Failed, if you have an account, you can retrieve it by forgot password", Toast.LENGTH_SHORT).show()
            }
        })

    }

    //This function helps in verifying whether the email is verified or not.
    private fun checkIfEmailVerified() {
        val users = FirebaseAuth.getInstance().currentUser
        val emailVerified = users!!.isEmailVerified
        if (!emailVerified) {
            Toasty.info(this, "Please verify your Email Id, link is already sent, check your spam folder!.", Toast.LENGTH_LONG).show()
            auth.signOut()
        } else {
            val intent = Intent(this@Signin, MainActivity::class.java)
            // Sending Email to Dashboard Activity using intent.
            startActivity(intent)
        }
    }

}
