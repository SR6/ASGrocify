package com.example.grocify

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.grocify.databinding.ActivityMainBinding
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.ui.AuthUser
import com.example.grocify.ui.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var authUser : AuthUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authUser = AuthUser(activityResultRegistry)
        lifecycle.addObserver(authUser)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerBinding = HeaderBinding.inflate(layoutInflater)

        navController = (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment).navController
        binding.tabbedNavigation.setupWithNavController(navController)

        supportActionBar?.let{
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayShowCustomEnabled(true)
            it.customView = headerBinding.root
        }

        headerBinding.favorites.setOnClickListener {
            navController.navigate(R.id.navigation_favorites)
        }

        viewModel.title.observe(this) { title ->
            headerBinding.title.text = title ?: ""
        }

        viewModel.subtitle.observe(this) { subtitle ->
            headerBinding.subtitle.text = subtitle ?: ""
        }

        viewModel.favoritesVisible.observe(this) { favoritesVisible ->
            headerBinding.favorites.visibility = if (favoritesVisible) View.VISIBLE else View.GONE
        }

        viewModel.searchVisible.observe(this) { searchVisible ->
            headerBinding.search.visibility = if (searchVisible) View.VISIBLE else View.INVISIBLE
        }

        viewModel.showBackButton.observe(this) { showBackButton ->
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(showBackButton)
                setDisplayShowHomeEnabled(showBackButton)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
