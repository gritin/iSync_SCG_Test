package com.example.isync.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.isync.R
import com.example.isync.databinding.ActivityLoginBinding
import com.example.isync.dialog.LoadingDialog
import com.example.isync.dialog.OneButtonDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var mAuth: FirebaseAuth
    private var oneButtonDialog : OneButtonDialog = OneButtonDialog()
    private var loadingDialog : LoadingDialog = LoadingDialog(this)
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

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.loginSignUp.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginForgotPassword.setOnClickListener {
            val intent = Intent(this,ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        mAuth = FirebaseAuth.getInstance()

        binding.loginSignIn.setOnClickListener {
            login()
        }

    }

    override fun onStart() {
        super.onStart()

        if (mAuth != null) {
            val currentuser = mAuth!!.currentUser
            updateUI(currentuser)
        }
    }

    private fun login() {
        email = binding.loginUsername.text.toString()
        password = binding.loginPassword.text.toString()

        if (!email.isEmpty() && !password.isEmpty()){
            if (checkValidation(email)){
                loadingDialog.startLoadingDialog()
                mAuth!!.signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
                        task -> if(task.isSuccessful){
                    val user = mAuth!!.currentUser
                    loadingDialog.dismiss()
                    updateUI(user)
                }else{
                    loadingDialog.dismiss()
                    oneButtonDialog.showDialog(this,"LogIn Fail","Close")
                }
                }
            }
        }else{
            if (email.isEmpty()){
                onsetErrorText("Please enter email",EMAIL,true)
            }else{
                onsetErrorText("Please enter password",PASSWORD,true)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user!=null) {
            val uid = user.uid
            val email = user.email
            Toast.makeText(this, "logged in with $email", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    fun isValidString(str:String):Boolean{
        return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
    }

    fun onsetErrorText(text:String, type:Int , isError:Boolean){
        when(type){
            EMAIL ->{
                if (isError){
                    binding.loginEmailWrong.text = text
                    binding.loginUsername.setBackgroundResource(R.drawable.red_round_angle_border)
                    binding.loginEmailWrong.visibility = View.VISIBLE
                }else{
                    binding.loginUsername.setBackgroundResource(R.drawable.gray_round_angle_border)
                    binding.loginEmailWrong.visibility = View.INVISIBLE
                }

            }
            PASSWORD ->{
                if (isError){
                    binding.loginPasswordWrong.text = text
                    binding.loginPassword.setBackgroundResource(R.drawable.red_round_angle_border)
                    binding.loginPasswordWrong.visibility = View.VISIBLE
                }else{
                    binding.loginPassword.setBackgroundResource(R.drawable.gray_round_angle_border)
                    binding.loginPasswordWrong.visibility = View.INVISIBLE
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
}