package com.example.grocify.ui

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.R
import com.example.grocify.api.KrogerClient.krogerService
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.models.KrogerProduct
import com.example.grocify.models.KrogerProductsResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Might use later for profile
//data class User(
//    val uid: String? = null,
//    val email: String? = null,
//    val name: String? = null
//)

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

//    private val _user = MutableLiveData<User?>()
//    val user: LiveData<User?> get() = _user

    private var cartList = MediatorLiveData<List<KrogerProduct>>().apply {
        value = emptyList()
    }
    fun observeFetchProducts() : LiveData<KrogerProductsResponse> {
        return products
    }
    fun getCategories() : List<String> {
        return categories
    }

    fun observeCartList() : LiveData<List<KrogerProduct>>{
        return cartList
    }
    fun setCartList(item: KrogerProduct){
        if (cartList.value == null) {
            cartList.postValue(listOf(item))
        } else {
            if (!item.inCart) {
                val currentCart = cartList.value!!.filterNot{ it == item }
                cartList.postValue(currentCart)
            } else {
                cartList.postValue(cartList.value?.plus(listOf(item)))
            }
        }
    }
    fun fetchProducts(item:String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val response = krogerService.getProducts("Bearer $token", "application/json", null, null, null, null, item)
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
}
