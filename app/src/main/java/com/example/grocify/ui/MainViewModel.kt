package com.example.grocify.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.api.KrogerClient.krogerService
import com.example.grocify.db.CategoryDatabaseConnection
import com.example.grocify.db.UserProductDatabaseConnection
import com.example.grocify.db.UserDatabaseConnection
import com.example.grocify.models.GrocifyCategory
import com.example.grocify.models.KrogerLocationsResponse
import com.example.grocify.models.KrogerProductResponse
import com.example.grocify.models.KrogerProductsResponse
import com.example.grocify.models.User
import com.example.grocify.models.UserProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel: ViewModel() {
    /* Database globals. */
    enum class DatabaseCollection(val databaseCollection: String) {
        FAVORITES("favorites"),
        CART("cart")
    }

    private val categoryDatabaseConnection = CategoryDatabaseConnection()
    private val userDatabaseConnection = UserDatabaseConnection()
    private val cartDatabaseConnection = UserProductDatabaseConnection(DatabaseCollection.CART.databaseCollection)
    private val favoritesDatabaseConnection = UserProductDatabaseConnection(DatabaseCollection.FAVORITES.databaseCollection)

    private val _favoriteProducts = MutableLiveData<List<UserProduct>?>()
    val favoriteProducts: LiveData<List<UserProduct>?> get() = _favoriteProducts

    private val _cartProducts = MutableLiveData<List<UserProduct>?>()
    val cartProducts: LiveData<List<UserProduct>?> get() = _cartProducts

    /* API globals. */
    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0

    /* API response globals. */
    private val _products = MutableLiveData<KrogerProductsResponse>()
    val products: LiveData<KrogerProductsResponse> get() = _products

    private val _product = MutableLiveData<KrogerProductResponse>()
    val product: LiveData<KrogerProductResponse> get() = _product

    private val _locations = MutableLiveData<KrogerLocationsResponse>()
    val locations: LiveData<KrogerLocationsResponse> get() = _locations

    private val _isApiRequestCompleted = MutableLiveData<Boolean>()
    val isApiRequestCompleted: LiveData<Boolean> get() = _isApiRequestCompleted

    /* Header globals. */
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

    /* User globals. */
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    /* Category globals. */
    private val _categoryProductCounts = MutableLiveData<HashMap<String, Int>>()
    val categoryProductCounts: LiveData<HashMap<String, Int>> = _categoryProductCounts

    /* API calls. */
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
                    user.value!!.locationId,
                    null,
                    null,
                    term)
                _products.postValue(response)

                val temp = HashMap(_categoryProductCounts.value ?: hashMapOf())
                temp[term] = response.meta.pagination.total
                _categoryProductCounts.postValue(temp)
                _isApiRequestCompleted.postValue(true)
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
                    productId,
                    user.value!!.locationId)
                _product.postValue(response)
                _isApiRequestCompleted.postValue(true)
            }
            catch (_: Exception) { }
        }
    }

    fun getLocations(zipCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val response = krogerService.getLocations(
                    "Bearer $token",
                    "application/json",
                    zipCode,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)
                _locations.postValue(response)
                _isApiRequestCompleted.postValue(true)
            }
            catch (_: Exception) { }
        }
    }

    fun setIsApiRequestCompleted(value: Boolean) {
        _isApiRequestCompleted.value = value
    }

    /* Database logic. */
    fun getCategories(
        onSuccess: (List<GrocifyCategory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        categoryDatabaseConnection.getCategories(
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getCategoryImage(
        imageFile: String,
        onSuccess: (File) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        categoryDatabaseConnection.getCategoryImage(
            imageFile = imageFile,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getUser(email: String,
                onSuccess: (User?) -> Unit,
                onFailure: (Exception) -> Unit) {
        userDatabaseConnection.getUser(email, { user ->
            _user.postValue(user)
            onSuccess(user)
        }, onFailure)
    }

    fun addUser(user: User,
                onSuccess: () -> Unit,
                onFailure: (Exception) -> Unit) {
        _user.postValue(user)
        userDatabaseConnection.addUser(user, onSuccess, onFailure)
    }

    fun updateUser(user: User,
                   onSuccess: () -> Unit,
                   onFailure: (Exception) -> Unit) {
        _user.postValue(user)
        userDatabaseConnection.updateUser(user, onSuccess, onFailure)
    }

    fun getFavorites(userId: String,
                     onSuccess: (List<UserProduct>?) -> Unit,
                     onFailure: (Exception) -> Unit) {
        favoritesDatabaseConnection.getUserProducts(userId, { userProducts ->
            _favoriteProducts.postValue(userProducts)
            onSuccess(userProducts)
        }, onFailure)
    }

    fun addToFavorites(userProduct: UserProduct,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit) {
        val currentFavoriteProducts = _favoriteProducts.value.orEmpty().toMutableList()
        currentFavoriteProducts.add(userProduct)
        _favoriteProducts.postValue(currentFavoriteProducts)
        favoritesDatabaseConnection.addUserProduct(userProduct, onSuccess, onFailure)
    }

    fun removeFromFavorites(userProduct: UserProduct,
                            onSuccess: () -> Unit,
                            onFailure: (Exception) -> Unit) {
        val currentFavoriteProducts = _favoriteProducts.value.orEmpty().toMutableList()
        currentFavoriteProducts.removeIf { it.userProductId == userProduct.userProductId }
        _favoriteProducts.postValue(currentFavoriteProducts)
        favoritesDatabaseConnection.removeUserProduct(userProduct, onSuccess, onFailure)
    }

    fun getCart(userId: String,
                onSuccess: (List<UserProduct>?) -> Unit,
                onFailure: (Exception) -> Unit) {
        cartDatabaseConnection.getUserProducts(userId, { userProducts ->
            _cartProducts.postValue(userProducts)
            onSuccess(userProducts)
        }, onFailure)
    }

    fun addToCart(userProduct: UserProduct,
                  onSuccess: () -> Unit,
                  onFailure: (Exception) -> Unit) {
        val currentCartProducts = _cartProducts.value.orEmpty().toMutableList()
        currentCartProducts.add(userProduct)
        _cartProducts.postValue(currentCartProducts)
        cartDatabaseConnection.addUserProduct(userProduct, onSuccess, onFailure)
    }

    fun removeFromCart(userProduct: UserProduct,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit) {
        val currentCartProducts = _cartProducts.value.orEmpty().toMutableList()
        currentCartProducts.removeIf { it.userProductId == userProduct.userProductId }
        _cartProducts.postValue(currentCartProducts)
        cartDatabaseConnection.removeUserProduct(userProduct, onSuccess, onFailure)
    }

    /* Header logic. */
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

    fun addCommasToNumber(number: Int): String {
        val numberString = number.toString()
        val regex = "(\\d)(?=(\\d{3})+(?!\\d))".toRegex()
        return numberString.replace(regex, "$1,")
    }
}
