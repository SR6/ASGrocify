package com.example.grocify

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.grocify.databinding.ActivityMainBinding
import com.example.grocify.databinding.ActionBarBinding
import androidx.appcompat.app.ActionBar
import androidx.navigation.ui.AppBarConfiguration
import com.example.grocify.ui.MainViewModel

class MainActivity : AppCompatActivity() {

    private var actionBarBinding: ActionBarBinding? = null
    private val viewModel: MainViewModel by viewModels()
    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }
    private fun initActionBar(actionBar: ActionBar) {
        // Disable the default and enable the custom
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBarBinding = ActionBarBinding.inflate(layoutInflater)
        // Apply the custom view
        actionBar.customView = actionBarBinding?.root
        //viewModel.initActionBarBinding(actionBarBinding!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        supportActionBar?.let{
            initActionBar(it)
        }
    }
}
