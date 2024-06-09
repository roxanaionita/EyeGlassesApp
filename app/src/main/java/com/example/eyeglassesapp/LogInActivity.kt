package com.example.eyeglassesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eyeglassesapp.databinding.ActivityLogInBinding
import com.example.eyeglassesapp.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()


        firebaseAuth = FirebaseAuth.getInstance()

        //LOGIN CU MAIL SI PAROLA
        binding.loginButton.setOnClickListener{
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        // Sign in success, check if the user is admin
                        val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                val role = document.getString("role")
                                val intent = if (role == "admin") {
                                    Intent(this, AdminDashboardActivity::class.java)
                                } else {
                                    Intent(this, MainActivity::class.java)
                                }
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Failure
                                Log.w("Login", "Error fetching user document", e)
                                Toast.makeText(this, "Failed to check user role.", Toast.LENGTH_SHORT).show()
                            }
                    }else{
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
                }else{
                Toast.makeText(this, "Can't have empty fields", Toast.LENGTH_SHORT).show()
            }

        }
        //SIGNUP REDIRECT
        binding.signupRedirect.setOnClickListener{
            val signupIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signupIntent)
        }
        //BACK BUTTON
        binding.backButton.setOnClickListener{
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
        //FORGOT PASSWORD
        binding.forgotPasswordText.setOnClickListener{
            val forgetPassIntent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgetPassIntent)
            finish()
        }


    }

//    private fun signInGoogle() {
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//        val googleSignInClient = GoogleSignIn.getClient(this, gso)
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


}