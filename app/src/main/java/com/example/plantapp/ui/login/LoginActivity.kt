package com.example.plantapp.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.plantapp.MainActivity
import com.example.plantapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if(currentUser != null) { // If user is logged in, go to FeedActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpClicked.setOnClickListener {
            val signupIntent = Intent(this, RegisterActivity::class.java)
            startActivity(signupIntent)
            finish()
        }

    }
}

    /*
        fun signInClicked(view: View) {

            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.equals("") || password.equals("")) {
                Toast.makeText(this, "Email and password are required!", Toast.LENGTH_LONG).show()
            }else{
                auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }

        fun signUp(view: View){
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun signUpClicked(view: View) {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.equals("") || password.equals("")) {
                // If email or password is empty, show error message
                Toast.makeText(this, "Email and password are required!", Toast.LENGTH_LONG).show()
            } else {
                // If email and password are not empty, try to login
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                    // If login is successful, go to FeedActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { exception ->
                    // If login is not successful, show error message
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }




     */

