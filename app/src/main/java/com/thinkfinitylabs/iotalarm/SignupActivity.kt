package com.thinkfinitylabs.iotalarm

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_signin.*
import java.util.*


class SignupActivity : AppCompatActivity(), View.OnClickListener {
    var email: EditText? = null
    var password: EditText? = null
    var mRegisterbtn: Button? = null
    var mLoginPageBack: Button? = null
    var mAuth: FirebaseAuth? = null
    var userEmail: String? = null
    var Password: String? = null
    var mDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        email = findViewById<View>(R.id.usernameforSignup) as EditText
        password = findViewById<View>(R.id.passwordForSignup) as EditText
        mRegisterbtn =
            findViewById<View>(R.id.signup_bton) as Button
        mLoginPageBack = findViewById<View>(R.id.signin_redirect) as Button
        // for authentication using FirebaseAuth.
        mAuth = FirebaseAuth.getInstance()
        mRegisterbtn!!.setOnClickListener(this)
        mLoginPageBack!!.setOnClickListener(this)
        mDialog = ProgressDialog(this)
    }

    override fun onClick(v: View) {
        if (v === mRegisterbtn) {
            UserRegister()
        } else if (v === mLoginPageBack) {
            startActivity(Intent(this@SignupActivity, Signin::class.java))
        }
    }

    private fun UserRegister() {
        userEmail = email!!.text.toString().trim { it <= ' ' }
        Password = password!!.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userEmail)) {
            Toasty.error(this@SignupActivity, "Enter Email", Toast.LENGTH_SHORT).show()
            return
        } else if (TextUtils.isEmpty(Password)) {
            Toasty.error(this@SignupActivity, "Enter Password", Toast.LENGTH_SHORT).show()
            return
        } else if (Password!!.length < 6) {
            Toast.makeText(
                this@SignupActivity,
                "Passwor must be greater then 6 digit",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        mDialog!!.setMessage("Creating User please wait...")
        mDialog!!.setCanceledOnTouchOutside(false)
        mDialog!!.show()
        mAuth!!.createUserWithEmailAndPassword(userEmail!!, Password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification()
                    mDialog!!.dismiss()
                    mAuth!!.signOut()
                    startActivity(Intent(this@SignupActivity,Signin::class.java))
                    finish()
                } else {
                    Toasty.error(this@SignupActivity, "error on creating user, try again with different email", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener{
                Toasty.error(this@SignupActivity, "error on creating user, try again after sometime", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    //Email verification code using FirebaseUser object and using isSucccessful()function.
    private fun sendEmailVerification() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.info(
                    this@SignupActivity,
                    "Check your Email for verification",
                    Toast.LENGTH_SHORT
                ).show()
                FirebaseAuth.getInstance().signOut()
            }
        }
    }



}