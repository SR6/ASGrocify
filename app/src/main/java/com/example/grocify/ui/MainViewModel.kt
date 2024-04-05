package com.example.grocify.ui

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.api.KrogerClient.krogerService
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.models.KrogerProductsResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _products = MutableLiveData<KrogerProductsResponse>()

    val products: LiveData<KrogerProductsResponse>
        get() = _products

    fun fetchProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val response = krogerService.getProducts("Bearer $token", "application/json", null, null, null, null, "milk")
                _products.postValue(response)
            }
            catch (e: Exception) {
                Log.d("PRODUCTS FAIL", "Error fetching products")
            }
        }
    }

    private suspend fun getToken(): String {
        val response = krogerService.getAuthToken()
        return response.accessToken
    }

    fun updateHeader(binding: HeaderBinding, title: String?, subtitle: String? = null, favoritesVisible: Boolean = true) {
        binding.title.text = title ?: ""
        binding.subtitle.text = subtitle ?: ""
        if (favoritesVisible)
            binding.favorites.visibility = View.VISIBLE
        else
            binding.favorites.visibility = View.GONE
    }
}
