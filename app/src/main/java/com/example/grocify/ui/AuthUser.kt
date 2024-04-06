package com.example.grocify.ui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.grocify.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthUser(private val registry: ActivityResultRegistry) :
    DefaultLifecycleObserver,
    FirebaseAuth.AuthStateListener {

    private lateinit var loginLauncher: ActivityResultLauncher<Intent>

    init {
        Firebase.auth.addAuthStateListener(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        loginLauncher = registry.register("key", owner,
            FirebaseAuthUIActivityResultContract()) { }
    }

    override fun onAuthStateChanged(user: FirebaseAuth) {
        if(user.currentUser == null) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            loginLauncher.launch(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .setTheme(R.style.AppTheme)
                    .build())
        }
    }
}