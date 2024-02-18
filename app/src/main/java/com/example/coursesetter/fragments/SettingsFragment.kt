package com.example.coursesetter.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.coursesetter.MainActivity
import com.example.coursesetter.R
import com.example.coursesetter.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth


class SettingsFragment : Fragment() {
    // Predeclared vars VVV
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var activityReference: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //gets access to the firebase and activity to fill variables for logout
        mAuth = FirebaseAuth.getInstance()
        val activity = activity
        if (activity is MainActivity) {
            activityReference = activity
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activityReference, gso)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //on click, if there is a user signed in, sign them out, then send them to the sign in page
        val signOutButton = view.findViewById<Button>(R.id.logout_button)
        signOutButton.setOnClickListener {
            if (mAuth.currentUser != null) {
                mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(activityReference, OnCompleteListener<Void?> {
                    })
                mAuth.signOut()

            }
            val intent = Intent(
                activity,
                SignInActivity::class.java
            )
            startActivity(intent)
        }
    }
}