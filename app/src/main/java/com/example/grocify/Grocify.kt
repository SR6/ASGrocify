package com.example.grocify

import android.app.Application
import com.google.firebase.FirebaseApp

class Grocify: Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}