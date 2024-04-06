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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class User(
    val uid: String? = null,
    val email: String? = null,
    val name: String? = null
)

class MainViewModel : ViewModel() {

    private val categories = listOf("Fruits","Vegetables","Meat","Seafood","Dairy","Deli","Bakery","Pantry","Eggs","Frozen","Beverages","Breakfast","Candy","Laundry","Cleaning")

    private val _products = MutableLiveData<KrogerProductsResponse>()
    val products: LiveData<KrogerProductsResponse> get() = _products

    private val _title = MutableLiveData<String?>()
    val title: MutableLiveData<String?> get() = _title

    private val _subtitle = MutableLiveData<String?>()
    val subtitle: LiveData<String?> get() = _subtitle

    private val _favoritesVisible = MutableLiveData<Boolean>()
    val favoritesVisible: LiveData<Boolean> get() = _favoritesVisible

    private val _searchVisible = MutableLiveData<Boolean>()
    val searchVisible: LiveData<Boolean> get() = _searchVisible

    private val _showBackButton = MutableLiveData<Boolean>()
    val showBackButton: LiveData<Boolean> get() = _showBackButton

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun observeFetchProducts() : LiveData<KrogerProductsResponse> {
        return products
    }
    fun getCategories() : List<String> {
        return categories
    }

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
        searchVisible: Boolean = false,
        showBackButton: Boolean = false
    ) {
        _title.postValue(title)
        _subtitle.postValue(subtitle)
        _favoritesVisible.postValue(favoritesVisible)
        _searchVisible.postValue(searchVisible)
        _showBackButton.postValue(showBackButton)
    }

    fun updateUser() {
        val user = FirebaseAuth.getInstance().currentUser
        _user.postValue(User(user?.uid, user?.email, user?.displayName))
    }

    fun userLogout() {
        FirebaseAuth.getInstance().signOut()
        _user.postValue(User(null, null, null))
    }
}
