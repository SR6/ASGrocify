package com.example.grocify

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.grocify.databinding.ActivityMainBinding
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.models.User
import com.example.grocify.ui.AuthUser
import com.example.grocify.ui.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity: AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var authUser : AuthUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        authUser = AuthUser(activityResultRegistry)
        lifecycle.addObserver(authUser)

        val firebaseAuthCheck = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null)
                initializeUser()
        }

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuthCheck)

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

        headerBinding.search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    hideKeyboard()
                    lifecycleScope.launch {
                        viewModel.getProducts(query)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        for (category in resources.getStringArray(R.array.categories)) {
            lifecycleScope.launch {
                //commented out to reduce requests to Kroger
//                viewModel.getProducts(category)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun initializeUser() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            viewModel.getUser(firebaseUser.email!!, onSuccess = { user ->
                if (user == null) {
                    viewModel.addUser(
                        User(UUID.randomUUID().toString(),
                            firebaseUser.email!!,
                            firebaseUser.displayName!!,
                            Timestamp.now(),
                            Timestamp.now(),
                            "",
                            resources.getString(R.string.default_zip_code),
                            resources.getString(R.string.default_location_id)),
                        onSuccess = { },
                        onFailure = {
                            Toast.makeText(applicationContext, resources.getString(R.string.user_add_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                else {
                    viewModel.updateUser(
                        User(user.userId,
                            firebaseUser.email!!,
                            firebaseUser.displayName!!,
                            user.createdAt,
                            Timestamp.now(),
                            user.paymentMethod,
                            user.zipCode,
                            user.locationId),
                        onSuccess = {
                            initializeCartAndFavorites()
                            if (navController.currentDestination?.id != R.id.navigation_category)
                                navController.navigateUp()
                        },
                        onFailure = {
                            Toast.makeText(applicationContext, resources.getString(R.string.user_update_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }, onFailure = {
                Toast.makeText(applicationContext, resources.getString(R.string.user_load_failed), Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun initializeCartAndFavorites() {
        viewModel.getCart(viewModel.user.value!!.userId,
            onSuccess = { },
            onFailure = {
                Toast.makeText(applicationContext, resources.getString(R.string.cart_load_failed), Toast.LENGTH_SHORT).show()
            }
        )
        viewModel.getFavorites(viewModel.user.value!!.userId,
            onSuccess = { },
            onFailure = {
                Toast.makeText(applicationContext, resources.getString(R.string.cart_load_failed), Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }
}
