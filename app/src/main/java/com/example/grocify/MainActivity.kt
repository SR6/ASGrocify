package com.example.grocify

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
//import com.example.grocify.databinding.ActionBarBinding
//import edu.cs371m.reddit.databinding.ActivityMainBinding
//import edu.cs371m.reddit.databinding.FragmentRvBinding
import com.example.grocify.ui.HomeFragmentDirections
import com.example.grocify.ui.MainViewModel


class MainActivity : AppCompatActivity() {
    private var actionBarBinding: ActionBarBinding? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController : NavController

    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }
    // https://stackoverflow.com/questions/24838155/set-onclick-listener-on-action-bar-title-in-android/29823008#29823008
    private fun initActionBar(actionBar: ActionBar) {
        // Disable the default and enable the custom
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBarBinding = ActionBarBinding.inflate(layoutInflater)
        // Apply the custom view
        actionBar.customView = actionBarBinding?.root
        viewModel.initActionBarBinding(actionBarBinding!!)
    }
    // https://nezspencer.medium.com/navigation-components-a-fix-for-navigation-action-cannot-be-found-in-the-current-destination-95b63e16152e
    // You get a NavDirections object from a Directions object like
    // HomeFragmentDirections.
    // safeNavigate checks if you are in the source fragment for the directions
    // object and if not does nothing
    private fun NavController.safeNavigate(direction: NavDirections) {
        currentDestination?.
        getAction(direction.actionId)?.
        run {
            navigate(direction)
        }
    }
    private fun actionBarTitleLaunchSubreddit() {
        // XXX Write me actionBarBinding, safeNavigate
        //viewModel.setTitle("r/"+viewModel.observeSubreddit().value) //this doesn't update the title
        //actionBarBinding?.actionTitle?.text = "r/" + viewModel.observeSubreddit().value

        // Navigate to the subreddit fragment, ensuring safe navigation
        navController.safeNavigate(HomeFragmentDirections.actionHomeFragmentToSubreddits())
    }
    private fun actionBarLaunchFavorites() {
        // XXX Write me actionBarBinding, safeNavigate
        actionBarBinding?.actionTitle?.text = "Favorites"
        navController.safeNavigate(HomeFragmentDirections.actionHomeFragmentToFavorites())

    }
    private fun actionBarSearch() {
        // XXX Write me
        Log.d("SearchHut","in ActionBarSearch")
        viewModel.setSearchTerm(actionBarBinding?.actionSearch?.text.toString())

    }

    private fun initTitleObservers() {
        // Observe title changes
        //lifecycle.addObserver(this)
        viewModel.observeSubreddit().observe(this) { value ->
            //supportActionBar?.title = value
            actionBarBinding?.actionTitle?.text = "r/$value"
        }
        viewModel.observeTitle().observe(this){ title ->
            actionBarBinding?.actionTitle?.text = title
            Log.d("Title", title)
        }

    }
    var searchWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            val searchTerm = s.toString()
            Log.d("SearchHut","beforeTextchanged Listener")
            if (searchTerm.isNotEmpty()) {
                viewModel.setSearchTerm(searchTerm)
                viewModel.filterFavorites(searchTerm)
                viewModel.filterNetPosts(searchTerm)
                viewModel.filterSubRedditList(searchTerm)
                Log.d("SearchHunt", "searchTerm not empty")
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.d("SearchHunt","MainActivity onTextChanged triggered")
            val searchTerm = s.toString()
            if (searchTerm.isEmpty()){
                viewModel.setSearchTerm("")
                hideKeyboard()
            }
            viewModel.setSearchTerm(searchTerm)
            viewModel.filterFavorites(searchTerm)
            viewModel.filterNetPosts(searchTerm)
            viewModel.filterSubRedditList(searchTerm)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        supportActionBar?.let{
            initActionBar(it)
        }
        // Set up our nav graph
        navController = findNavController(R.id.main_frame)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        // If we have a toolbar (not actionbar) we don't need to override
        // onSupportNavigateUp().
        activityMainBinding.toolbar.setupWithNavController(navController, appBarConfiguration)
        initTitleObservers()
        actionBarBinding?.actionSearch?.addTextChangedListener(searchWatcher)
        actionBarBinding?.actionSearch?.setOnClickListener {
            actionBarSearch()
        }
        actionBarBinding?.actionTitle?.setOnClickListener {
            actionBarTitleLaunchSubreddit()
        }
        actionBarBinding?.actionFavorite?.setOnClickListener {
            actionBarLaunchFavorites()
        }
    }
}