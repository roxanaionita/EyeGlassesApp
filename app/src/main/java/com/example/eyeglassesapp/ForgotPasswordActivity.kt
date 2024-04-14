package com.example.eyeglassesapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eyeglassesapp.databinding.ActivityForgotPasswordBinding
import com.example.eyeglassesapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSendEmail.setOnClickListener{
            val email = binding.loginEmail.text.toString().trim()
            if(email.isNotEmpty()){
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this, "Reset Password Email has been sent to you.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LogInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            }
            }

        binding.backButton.setOnClickListener{
            onBackPressed()
        }
        binding.rememberLoginRedirect.setOnClickListener{
            val loginIntent = Intent(this,LogInActivity::class.java)
            startActivity(loginIntent)
            finish()
        }




        }
    }
