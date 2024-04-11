package com.example.grocify.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.api.KrogerClient.krogerService
import com.example.grocify.db.DatabaseConnection
import com.example.grocify.models.GrocifyCategory
import com.example.grocify.models.KrogerProduct
import com.example.grocify.models.KrogerProductResponse
import com.example.grocify.models.KrogerProductsResponse
import com.example.grocify.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {
    private val databaseConnection = DatabaseConnection()

    private val _categoryProductCounts = MutableLiveData<HashMap<String, Int>>()
    val categoryProductCounts: LiveData<HashMap<String, Int>> = _categoryProductCounts

    private val _cartProducts = MediatorLiveData<KrogerProductsResponse>()
    val cartProducts: LiveData<KrogerProductsResponse> = _cartProducts

    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0

    private val _products = MutableLiveData<KrogerProductsResponse>()
    val products: LiveData<KrogerProductsResponse> get() = _products

    private val _product = MutableLiveData<KrogerProductResponse>()
    val product: LiveData<KrogerProductResponse> get() = _product

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

    private suspend fun getToken(): String {
        return if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime)
            cachedToken!!
        else {
            val response = krogerService.getAuthToken()
            cachedToken = response.accessToken
            tokenExpirationTime = System.currentTimeMillis() + response.expiresIn * 1000
            cachedToken!!
        }
    }

    fun getProducts(term: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val response = krogerService.getProducts(
                    "Bearer $token",
                    "application/json",
                    null,
                    null,
                    "01400943", //I have no idea where this is, just the example from the documentation
                    null,
                    null,
                    term)
                _products.postValue(response)

                val temp = HashMap(_categoryProductCounts.value ?: hashMapOf())
                temp[term] = response.meta.pagination.total
                _categoryProductCounts.postValue(temp)
            }
            catch (_: Exception) { }
        }
    }

    fun getProductById(productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val response = krogerService.getProductById(
                    "Bearer $token",
                    "application/json",
                    productId)
                _product.postValue(response)
            }
            catch (_: Exception) { }
        }
    }
    fun observeProductList(): List<KrogerProduct>{
        return products.value!!.products
    }
    fun observeProduct(): KrogerProduct {
        return product.value!!.product
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

    fun getCategories(
        onSuccess: (List<GrocifyCategory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        databaseConnection.getCategories(
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getCategoryImage(
        imageFile: String,
        onSuccess: (File) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        databaseConnection.getCategoryImage(
            imageFile = imageFile,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getUser(email: String,
                onSuccess: (User?) -> Unit,
                onFailure: (Exception) -> Unit) {
        databaseConnection.getUser(email, { user ->
            _user.postValue(user)
            onSuccess(user)
        }, onFailure)
    }

    fun addUser(user: User,
                onSuccess: () -> Unit,
                onFailure: (Exception) -> Unit) {
        databaseConnection.addUser(user, onSuccess, onFailure)
        _user.postValue(user)
    }

    fun updateUser(user: User,
                   onSuccess: () -> Unit,
                   onFailure: (Exception) -> Unit) {
        databaseConnection.updateUser(user, onSuccess, onFailure)
        _user.postValue(user)
    }



//    private var cartList = MediatorLiveData<List<KrogerProduct>>().apply {
//        value = emptyList()
//    }


//    fun observeCartList() : LiveData<List<KrogerProduct>>{
//        return cartList
//    }
//
//    fun setCartList(item: KrogerProduct){
//        if (cartList.value == null) {
//            cartList.postValue(listOf(item))
//        } else {
//            if (!item) {
//                val currentCart = cartList.value!!.filterNot{ it == item }
//                cartList.postValue(currentCart)
//            } else {
//                cartList.postValue(cartList.value?.plus(listOf(item)))
//            }
//        }
//    }



}
