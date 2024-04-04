package com.example.grocify

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.databinding.ActivityMainBinding
import com.example.grocify.ui.GrocifyFragment

class MainActivity : AppCompatActivity() {
    private var headerBinding: HeaderBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        supportActionBar?.let{
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayShowCustomEnabled(true)
            headerBinding = HeaderBinding.inflate(layoutInflater)
            it.customView = headerBinding?.root
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, GrocifyFragment())
            .commit()
    }
}
