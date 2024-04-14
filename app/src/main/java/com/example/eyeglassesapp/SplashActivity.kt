package com.example.eyeglassesapp

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val loginButton: Button = findViewById(R.id.splash_login_redirect)
        val signupButton: Button = findViewById(R.id.splash_signup_redirect)

        loginButton.setOnClickListener {
            // Start LoginActivity
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }

        signupButton.setOnClickListener {
            // Start SignupActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}


