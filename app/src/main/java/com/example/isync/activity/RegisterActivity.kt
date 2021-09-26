package com.example.isync.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.isync.databinding.ActivityLoginBinding
import com.example.isync.databinding.ActivitySignupBinding
import com.example.isync.dialog.OneButtonDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

        lateinit var email:String
        lateinit var password:String
        private var oneButtonDialog : OneButtonDialog = OneButtonDialog()
        lateinit private var mAuth: FirebaseAuth
        lateinit var database: FirebaseDatabase


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

    private fun createAccount() {
        email = binding.signupUsername.text.toString()
        password = binding.signupPassword.text.toString()
        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        val databaseReference =
                            database.reference.child("images/${mAuth.currentUser!!.uid}").push()
                        oneButtonDialog.showDialog(this, "Registration Success", "Close")
                        oneButtonDialog.onOKButtonClickListener = {
                            this.finish()
                        }
                    } else {
                        oneButtonDialog.showDialog(this, "Registration Fail", "Close")
                    }
                }
        }else{
            oneButtonDialog.showDialog(this,"Register","Close",true,"please enter email and password.")
        }
    }




}