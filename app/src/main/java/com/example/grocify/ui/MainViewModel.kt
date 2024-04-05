package com.example.grocify.ui

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.R
import com.example.grocify.api.KrogerClient.krogerService
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.models.KrogerProductsResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _products = MutableLiveData<KrogerProductsResponse>()
    val products: LiveData<KrogerProductsResponse> get() = _products
    private val _title = MutableLiveData<String?>()
    val title: MutableLiveData<String?> get() = _title

    private val _subtitle = MutableLiveData<String?>()
    val subtitle: LiveData<String?> get() = _subtitle

    private val _favoritesVisible = MutableLiveData<Boolean>()
    val favoritesVisible: LiveData<Boolean> get() = _favoritesVisible

    private val _showBackButton = MutableLiveData<Boolean>()
    val showBackButton: LiveData<Boolean> get() = _showBackButton

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

    fun updateHeader(
        title: String?,
        subtitle: String? = null,
        favoritesVisible: Boolean = true,
        showBackButton: Boolean = false
    ) {
        _title.postValue(title)
        _subtitle.postValue(subtitle)
        _favoritesVisible.postValue(favoritesVisible)
        _showBackButton.postValue(showBackButton)
    }
}
