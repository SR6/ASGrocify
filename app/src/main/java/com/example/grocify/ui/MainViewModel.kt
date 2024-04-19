package com.example.grocify.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.R
import com.example.grocify.api.KrogerClient.krogerService
import com.example.grocify.db.CategoryDatabaseConnection
import com.example.grocify.db.TransactionsDatabaseConnection
import com.example.grocify.db.UserProductDatabaseConnection
import com.example.grocify.db.UserDatabaseConnection
import com.example.grocify.models.GrocifyCategory
import com.example.grocify.models.KrogerLocationsResponse
import com.example.grocify.models.KrogerProduct
import com.example.grocify.models.KrogerProductResponse
import com.example.grocify.models.KrogerProductsResponse
import com.example.grocify.models.Transaction
import com.example.grocify.models.User
import com.example.grocify.models.UserProduct
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel: ViewModel() {
    /* Auth global */
    lateinit var firebaseAuthCheck: FirebaseAuth.AuthStateListener

    /* Database globals. */
    enum class DatabaseCollection(val databaseCollection: String) {
        FAVORITES("favorites"),
        CART("cart")
    }

    private val categoryDatabaseConnection = CategoryDatabaseConnection()
    private val userDatabaseConnection = UserDatabaseConnection()
    private val transactionsDatabaseConnection = TransactionsDatabaseConnection()
    private val cartDatabaseConnection = UserProductDatabaseConnection(DatabaseCollection.CART.databaseCollection)
    private val favoritesDatabaseConnection = UserProductDatabaseConnection(DatabaseCollection.FAVORITES.databaseCollection)

    private val _favoriteUserProducts = MutableLiveData<List<UserProduct>?>()
    val favoriteUserProducts: LiveData<List<UserProduct>?> get() = _favoriteUserProducts

    private val _cartUserProducts = MutableLiveData<List<UserProduct>?>()
    val cartUserProducts: LiveData<List<UserProduct>?> get() = _cartUserProducts

    /* API globals. */
    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0

    /* API response globals. */
    private val _products = MutableLiveData<KrogerProductsResponse?>()
    val products: LiveData<KrogerProductsResponse?> get() = _products

    private val _searchProducts = MutableLiveData<KrogerProductsResponse?>()
    val searchProducts: LiveData<KrogerProductsResponse?> get() = _searchProducts

    private val _cartProducts = MutableLiveData<List<KrogerProduct>?>()
    val cartProducts: LiveData<List<KrogerProduct>?> get() = _cartProducts

    private val _favoriteProducts = MutableLiveData<List<KrogerProduct>?>()
    val favoriteProducts: LiveData<List<KrogerProduct>?> get() = _favoriteProducts

    private val _locations = MutableLiveData<KrogerLocationsResponse>()
    val locations: LiveData<KrogerLocationsResponse> get() = _locations

    private val _transactions = MutableLiveData<List<Transaction>?>()
    val transactions: LiveData<List<Transaction>?> get() = _transactions

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

    fun getProducts(term: String, isSearchProducts: Boolean = false) {
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
                if (!isSearchProducts)
                    _products.postValue(response)
                else
                    _searchProducts.postValue(response)
                _isApiRequestCompleted.postValue(true)

                val temp = HashMap(_categoryProductCounts.value ?: hashMapOf())
                temp[term] = response.meta.pagination.total
                _categoryProductCounts.postValue(temp)
            }
            catch (_: Exception) { }
        }
    }

    suspend fun getProductById(productId: String): KrogerProductResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val token = getToken()
                val response = krogerService.getProductById(
                    "Bearer $token",
                    "application/json",
                    productId,
                    user.value!!.locationId)
                _isApiRequestCompleted.postValue(true)
                response
            }
            catch (_: Exception) {
                null
            }
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
        userDatabaseConnection.addUser(user, {
            _user.postValue(user)
            onSuccess()
        }, onFailure)

    }

    fun updateUser(user: User,
                   onSuccess: () -> Unit,
                   onFailure: (Exception) -> Unit) {
        userDatabaseConnection.updateUser(user, {
            _user.postValue(user)
            onSuccess()
        }, onFailure)
    }

    fun clearUser() {
        _user.postValue(null)
    }

    fun getFavorites(userId: String,
                     onSuccess: (List<UserProduct>?) -> Unit,
                     onFailure: (Exception) -> Unit) {
        favoritesDatabaseConnection.getUserProducts(userId, { userProducts ->
            CoroutineScope(Dispatchers.IO).launch {
                _favoriteUserProducts.postValue(userProducts)
                _favoriteProducts.postValue(null)
                userProducts?.forEach { userProduct ->
                    val product = getProductById(userProduct.productId)
                    product?.let { favoriteProduct ->
                        val currentFavoriteProducts = _favoriteProducts.value.orEmpty().toMutableList()
                        currentFavoriteProducts.add(favoriteProduct.product)
                        _favoriteProducts.postValue(currentFavoriteProducts)
                    }
                }
                onSuccess(userProducts)
            }
        }, onFailure)
    }

    fun addToFavorites(userProduct: UserProduct,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit) {
        favoritesDatabaseConnection.addUserProduct(userProduct, {
            CoroutineScope(Dispatchers.IO).launch {
                val currentFavoriteUserProducts = _favoriteUserProducts.value.orEmpty().toMutableList()
                currentFavoriteUserProducts.add(userProduct)
                _favoriteUserProducts.postValue(currentFavoriteUserProducts)

                val product = getProductById(userProduct.productId)

                product?.let { favoriteProduct ->
                    val currentFavoriteProducts = _favoriteProducts.value.orEmpty().toMutableList()
                    currentFavoriteProducts.add(favoriteProduct.product)
                    _favoriteProducts.postValue(currentFavoriteProducts)
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }, onFailure)
    }

    fun removeFromFavorites(userProduct: UserProduct,
                            onSuccess: () -> Unit,
                            onFailure: (Exception) -> Unit) {
        favoritesDatabaseConnection.removeUserProduct(userProduct, {
            val currentFavoriteUserProducts = _favoriteUserProducts.value.orEmpty().toMutableList()
            currentFavoriteUserProducts.removeIf { it.userProductId == userProduct.userProductId }
            _favoriteUserProducts.postValue(currentFavoriteUserProducts)

            val currentFavoriteProducts = _favoriteProducts.value.orEmpty().toMutableList()
            currentFavoriteProducts.removeIf { it.productId == userProduct.productId }
            _favoriteProducts.postValue(currentFavoriteProducts)

            onSuccess()
        }, onFailure)
    }

    fun getCart(userId: String,
                onSuccess: (List<UserProduct>?) -> Unit,
                onFailure: (Exception) -> Unit) {
        cartDatabaseConnection.getUserProducts(userId, { userProducts ->
            CoroutineScope(Dispatchers.IO).launch {
                _cartUserProducts.postValue(userProducts)
                _cartProducts.postValue(null)
                userProducts?.forEach { userProduct ->
                    val product = getProductById(userProduct.productId)
                    product?.let { cartProduct ->
                        val currentCartProducts = _cartProducts.value.orEmpty().toMutableList()
                        currentCartProducts.add(cartProduct.product)
                        _cartProducts.postValue(currentCartProducts)
                    }
                }
                onSuccess(userProducts)
            }
        }, onFailure)
    }

    fun addToCart(userProduct: UserProduct,
                  onSuccess: () -> Unit,
                  onFailure: (Exception) -> Unit) {
        cartDatabaseConnection.addUserProduct(userProduct, {
            CoroutineScope(Dispatchers.IO).launch {
                val currentCartUserProducts = _cartUserProducts.value.orEmpty().toMutableList()
                currentCartUserProducts.add(userProduct)
                _cartUserProducts.postValue(currentCartUserProducts)

                val product = getProductById(userProduct.productId)

                product?.let { cartProduct ->
                    val currentCartProducts = _cartProducts.value.orEmpty().toMutableList()
                    currentCartProducts.add(cartProduct.product)
                    _cartProducts.postValue(currentCartProducts)
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }, onFailure)
    }

    fun removeFromCart(userProduct: UserProduct,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit) {
        cartDatabaseConnection.removeUserProduct(userProduct, {
            val currentCartUserProducts = _cartUserProducts.value.orEmpty().toMutableList()
            currentCartUserProducts.removeIf { it.userProductId == userProduct.userProductId }
            _cartUserProducts.postValue(currentCartUserProducts)

            val currentCartProducts = _cartProducts.value.orEmpty().toMutableList()
            currentCartProducts.removeIf { it.productId == userProduct.productId }
            _cartProducts.postValue(currentCartProducts)

            onSuccess()
        }, onFailure)
    }

    fun getTransactions(userId: String,
                        onSuccess: (List<Transaction>?) -> Unit,
                        onFailure: (Exception) -> Unit) {
        transactionsDatabaseConnection.getUserTransactions(userId, { transactions ->
            _transactions.postValue(transactions)
            onSuccess(transactions)
        }, onFailure)
    }

    fun addTransaction(transaction: Transaction,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit) {
        transactionsDatabaseConnection.addTransaction(transaction, {
            val currentTransactions = _transactions.value.orEmpty().toMutableList()
            currentTransactions.add(transaction)
            _transactions.postValue(currentTransactions)
            onSuccess()
        }, onFailure)

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

    /* Format helpers */

    fun addCommasToNumber(number: Int): String {
        val numberString = number.toString()
        val regex = "(\\d)(?=(\\d{3})+(?!\\d))".toRegex()
        return numberString.replace(regex, "$1,")
    }

    fun obfuscateCardNumber(context: Context, cardNumber: String): String {
        return context.resources.getString(R.string.ending_in) + cardNumber.takeLast(4)
    }
}

class ConfirmationDialogFragment(
    private val onConfirmListener: () -> Unit,
    private val message: String,
    private val positiveMessage: String,
    private val negativeMessage: String,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setPositiveButton(positiveMessage) { _, _ ->
                    onConfirmListener.invoke()
                }
                .setNegativeButton(negativeMessage) { _, _ -> }
            builder.create()
        } ?: throw java.lang.Exception()
    }
}