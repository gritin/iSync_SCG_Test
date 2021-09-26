package com.example.isync.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.isync.databinding.ActivityLoginBinding
import com.example.isync.dialog.OneButtonDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var mAuth: FirebaseAuth
    private var oneButtonDialog : OneButtonDialog = OneButtonDialog()

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
        mAuth!!.signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            task -> if(task.isSuccessful){
                val user = mAuth!!.currentUser
            updateUI(user)
            }else{
                oneButtonDialog.showDialog(this,"LogIn Fail","Close")
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user!=null) {
            val uid = user.uid
            val email = user.email
            Toast.makeText(this, "logged in with $email", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


}