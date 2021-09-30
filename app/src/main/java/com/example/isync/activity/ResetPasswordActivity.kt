package com.example.isync.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.isync.R
import com.example.isync.databinding.ActivityLoginBinding
import com.example.isync.databinding.ActivityResetPasswordBinding
import com.example.isync.databinding.ActivitySignupBinding
import com.example.isync.dialog.LoadingDialog
import com.example.isync.dialog.OneButtonDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding

        lateinit var email:String
        private var oneButtonDialog : OneButtonDialog = OneButtonDialog()
        private var loadingDialog : LoadingDialog = LoadingDialog(this)
        lateinit private var mAuth: FirebaseAuth
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+")


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

            if(isValidString(email)){
                onsetErrorText("",false)
                loadingDialog.startLoadingDialog()
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        loadingDialog.dismiss()
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
                        loadingDialog.dismiss()
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
                onsetErrorText("Please check email format",true)
            }


        }else{
            onsetErrorText("Please enter email",true)
        }
    }

    fun onsetErrorText(text:String,isError:Boolean){

      if (isError){
          binding.resetEmailWrong.text = text
          binding.resetUsername.setBackgroundResource(R.drawable.red_round_angle_border)
          binding.resetEmailWrong.visibility = View.VISIBLE
      }else{
          binding.resetUsername.setBackgroundResource(R.drawable.gray_round_angle_border)
          binding.resetEmailWrong.visibility = View.INVISIBLE
      }

    }

    fun isValidString(str:String):Boolean{
        return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
    }

}