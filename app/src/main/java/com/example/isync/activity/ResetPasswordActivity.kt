package com.example.isync.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.isync.databinding.ActivityLoginBinding
import com.example.isync.databinding.ActivityResetPasswordBinding
import com.example.isync.databinding.ActivitySignupBinding
import com.example.isync.dialog.OneButtonDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding

        lateinit var email:String
        private var oneButtonDialog : OneButtonDialog = OneButtonDialog()
        lateinit private var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResetPasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.resetClose.setOnClickListener {
            this.finish()
        }


        mAuth = FirebaseAuth.getInstance()

        binding.resetSubmit.setOnClickListener {
            reset()
        }
    }

    private fun reset() {
        email = binding.resetUsername.text.toString()

        if (!email.isEmpty()) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    oneButtonDialog.showDialog(
                        this,
                        "Reset Password",
                        "Close",
                        true,
                        "Please check your email."
                    )
                    oneButtonDialog.onOKButtonClickListener = {
                        this.finish()
                    }
                } else {
                    oneButtonDialog.showDialog(
                        this,
                        "Reset Password",
                        "Close",
                        true,
                        "reset password fail"
                    )
                }
            }
        }else{
            oneButtonDialog.showDialog(this,"Reset Password","Close",true,"please enter email.")
        }
    }


}