package com.example.coursesetter

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class SignInActivity : AppCompatActivity() {
    //Predeclared Vars for login vvv
    private lateinit var auth: FirebaseAuth
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // LOGIN CODE VVVV

        //Get current user from firebase
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        //If there is a user logged in, go straight to main
        if (currentUser != null) {
            // The user is already signed in, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // finish the current activity to prevent the user from coming back to the SignInActivity using the back button
        }
        //if sign in button is clicked, start sign in process
        val signInButton = findViewById<SignInButton>(R.id.signInButton)
        signInButton.setOnClickListener {
            signIn()
        }
        //if skip sign in is clicked, move to main without a user
        val skipSignInButton = findViewById<Button>(R.id.skipSignInButton)
        skipSignInButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //if skip to map is pressed, moves to the MapsActivity
        val mapButton = findViewById<Button>(R.id.mapButton)
        mapButton.setOnClickListener {
            val Intent = Intent(this, MapsActivity::class.java)
            startActivity(Intent)
        }
    }
    //Starts the sign in process, starts the signin intent and sends the results to onActivityResult
    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN) //needs to be updated, wont for now (famous last words)
       // startActivity(signInIntent, RC_SIGN_IN)

    }
    //Receives results from signInIntent, if all is well tries firebaseauthWithGoogle on the idtoken
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
    //finalizes the login process and sends to next activity.
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userID = user!!.uid
                    val email : String? = user.email
                    //This checks to see if the user has an account in the database
                    val uidEventListener: ValueEventListener = object : ValueEventListener
                    {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                //create new user
                                Log.d(TAG, "new user")
                                if (email != null) {
                                    Firebase.database.getReference("Users").child(userID).child("Email").setValue(email)
                                    Firebase.database.getReference("Users").child(userID).child("Name").setValue(user.displayName)
                                }
                            }
                            else
                            {
                                Log.d(TAG, "returning user")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d(TAG, databaseError.message) //Don't ignore errors!
                        }
                    }
                    Firebase.database.reference.child("Users").child(userID).addListenerForSingleValueEvent(uidEventListener)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}