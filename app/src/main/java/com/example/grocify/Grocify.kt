package com.example.grocify

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.example.grocify.ui.AuthUser
import com.google.firebase.FirebaseApp

class Grocify: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}