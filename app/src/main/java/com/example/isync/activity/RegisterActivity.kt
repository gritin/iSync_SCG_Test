package com.example.isync.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.isync.R
import com.example.isync.databinding.ActivityLoginBinding
import com.example.isync.databinding.ActivitySignupBinding
import com.example.isync.dialog.LoadingDialog
import com.example.isync.dialog.OneButtonDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

        lateinit var email:String
        lateinit var password:String
        private var oneButtonDialog : OneButtonDialog = OneButtonDialog()
        lateinit private var mAuth: FirebaseAuth
        private var loadingDialog : LoadingDialog = LoadingDialog(this)
        lateinit var database: FirebaseDatabase
        private var EMAIL = 100
        private var PASSWORD = 200;
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

        binding = ActivitySignupBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.signupClose.setOnClickListener {
            this.finish()
        }


        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.SignupSubmit.setOnClickListener {

                createAccount()
        }
    }

    fun onsetErrorText(text:String, type:Int , isError:Boolean){
        when(type){
            EMAIL ->{
                if (isError){
                    binding.singupEmailWrong.text = text
                    binding.signupUsername.setBackgroundResource(R.drawable.red_round_angle_border)
                    binding.singupEmailWrong.visibility = View.VISIBLE
                }else{
                    binding.signupUsername.setBackgroundResource(R.drawable.gray_round_angle_border)
                    binding.singupEmailWrong.visibility = View.INVISIBLE
                }

            }
            PASSWORD ->{
                if (isError){
                    binding.singupPasswordWrong.text = text
                    binding.signupPassword.setBackgroundResource(R.drawable.red_round_angle_border)
                    binding.singupPasswordWrong.visibility = View.VISIBLE
                }else{
                    binding.signupPassword.setBackgroundResource(R.drawable.gray_round_angle_border)
                    binding.singupPasswordWrong.visibility = View.INVISIBLE
                }

            }
        }
    }

    fun checkValidation(email : String) : Boolean{
        if(isValidString(email)){
            onsetErrorText("",EMAIL,false)
            onsetErrorText("",PASSWORD,false)
            return true
        }else {
            onsetErrorText("Please check email format",EMAIL,true)
            return false
        }
    }

    private fun createAccount() {
        email = binding.signupUsername.text.toString()
        password = binding.signupPassword.text.toString()
        if (!email.isEmpty() && !password.isEmpty()) {
            if (password.length < 6){
                onsetErrorText("Password should be at least 6 characters",PASSWORD,true)
            }else {
                loadingDialog.startLoadingDialog()
                if (checkValidation(email)) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = mAuth.currentUser
                                val databaseReference =
                                    database.reference.child("images/${mAuth.currentUser!!.uid}")
                                        .push()
                                loadingDialog.dismiss()
                                oneButtonDialog.showDialog(this, "Registration Success", "Close")
                                oneButtonDialog.onOKButtonClickListener = {
                                    this.finish()
                                }
                            } else {

                                loadingDialog.dismiss()
                                oneButtonDialog.showDialog(this, "Signup Fail", "Close")
//                                Log.d("REGISTER", "createAccount: ${task.exception}")
                            }
                        }
                }
            }
        }else{
            if(email.isEmpty()){
                onsetErrorText("Please enter email",EMAIL,true)
            }else{
                onsetErrorText("Please enter password",PASSWORD,true)
            }
        }
    }

    fun isValidString(str:String):Boolean{
        return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
    }


}