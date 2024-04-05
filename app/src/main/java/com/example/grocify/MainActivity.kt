package com.example.grocify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        supportActionBar?.let{
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayShowCustomEnabled(true)
            it.customView = HeaderBinding.inflate(layoutInflater).root
        }

        navController = (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment).navController
        activityMainBinding.tabbedNavigation.setupWithNavController(navController)
    }
}
