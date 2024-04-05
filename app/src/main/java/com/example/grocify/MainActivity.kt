package com.example.grocify

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.databinding.ActivityMainBinding
import com.example.grocify.ui.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var headerBinding: HeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        headerBinding = HeaderBinding.inflate(layoutInflater)

        navController = (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment).navController
        binding.tabbedNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            var title: String? = null
            var subtitle: String? = null
            var favoritesVisible = true
            var showBackButton = false
            
            when (destination.id) {
                R.id.navigation_category -> {
                    title = getString(R.string.grocify)
                    subtitle = "Hi, User"
                }
                R.id.navigation_search -> {
                    subtitle = "88 items found"
                    favoritesVisible = false
                }
                R.id.navigation_cart -> {
                    title = getString(R.string.cart)
                    subtitle = "20 items added"
                }
                R.id.navigation_profile -> {
                    title = "Hi, User"
                    favoritesVisible = false
                }
                R.id.navigation_favorites -> {
                    title = getString(R.string.favorites)
                    favoritesVisible = false
                    showBackButton = true
                }
                else -> { }
            }
            viewModel.updateHeader(headerBinding, title, subtitle, favoritesVisible)
            supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
            supportActionBar?.setDisplayShowHomeEnabled(showBackButton)
        }

        supportActionBar?.let{
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayShowCustomEnabled(true)
            it.customView = headerBinding.root
        }

        headerBinding.favorites.setOnClickListener {
            navController.navigate(R.id.navigation_favorites)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
