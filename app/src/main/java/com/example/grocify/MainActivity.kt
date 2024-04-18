package com.example.grocify

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.grocify.databinding.ActivityMainBinding
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.models.User
import com.example.grocify.ui.AuthUser
import com.example.grocify.ui.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity: AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var authUser : AuthUser

    lateinit var headerBinding: HeaderBinding

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment).navController
        binding.tabbedNavigation.setOnNavigationItemSelectedListener(navListener)

        authUser = AuthUser(activityResultRegistry)
        lifecycle.addObserver(authUser)

        binding.loading.root.visibility = View.VISIBLE

        viewModel.firebaseAuthCheck = FirebaseAuth.AuthStateListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                initializeUser()
                viewModel.user.observe(this@MainActivity) { user ->
                    populateGrocify()
                    if (user != null)
                        populateGrocify()
                }
            }

        }
        FirebaseAuth.getInstance().addAuthStateListener(viewModel.firebaseAuthCheck)
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.category_fragment, R.id.search_fragment, R.id.cart_fragment, R.id.profile_fragment -> {
                navController.popBackStack(navController.graph.startDestinationId, false)
                navController.navigate(item.itemId)
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        viewModel.clearUser()
        FirebaseAuth.getInstance().removeAuthStateListener(viewModel.firebaseAuthCheck)
    }

    private fun initializeUser() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        viewModel.getUser(firebaseUser!!.email!!, onSuccess = { user ->
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
                    onSuccess = {
                        if (navController.currentDestination?.id != R.id.category_fragment)
                            navController.navigate(R.id.category_fragment)
                    },
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
                        if (navController.currentDestination?.id != R.id.category_fragment)
                            navController.navigate(R.id.category_fragment)
                    },
                    onFailure = {
                        Toast.makeText(applicationContext, resources.getString(R.string.user_update_failed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
        onFailure = {
            Toast.makeText(applicationContext, resources.getString(R.string.user_load_failed), Toast.LENGTH_SHORT).show()
        })
    }

    private fun populateGrocify() {
        binding.loading.root.visibility = View.GONE

        headerBinding = HeaderBinding.inflate(layoutInflater)

        supportActionBar?.let{
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayShowCustomEnabled(true)
            it.customView = headerBinding.root
        }

        headerBinding.favorites.setOnClickListener {
            navController.navigate(R.id.favorites_fragment)
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

        for (category in resources.getStringArray(R.array.categories)) {
            lifecycleScope.launch {
                //commented out to reduce requests to Kroger
//                viewModel.getProducts(category)
            }
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

    override fun onSupportNavigateUp(): Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
