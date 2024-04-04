package com.example.grocify.ui

import com.example.grocify.api.KrogerClient.krogerService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grocify.models.KrogerProductsResponse
import com.example.grocify.models.AuthTokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel() : ViewModel() {

    // LiveData for holding the products response
    private val _productsResponse = MutableLiveData<KrogerProductsResponse>()
    val productsResponse: LiveData<KrogerProductsResponse>
        get() = _productsResponse

    fun fetchProducts() {
        // Call private function to get OAuth token
        getToken(object : TokenCallback {
            override fun onTokenReceived(token: String) {
                // Token received, now make API call to get products
                val call = krogerService.getProducts("Bearer $token", "application/json", null, null, "0001111041660", null, null)
                call.enqueue(object : Callback<KrogerProductsResponse> {
                    override fun onResponse(
                        call: Call<KrogerProductsResponse>,
                        response: Response<KrogerProductsResponse>
                    ) {
                        if (response.isSuccessful) {
                            // Update LiveData with the response
                            _productsResponse.postValue(response.body())
                        } else {
                            // Handle unsuccessful response here
                            // Maybe show an error message to the user
                            // For example:
                            val errorMessage = "Failed to fetch products: ${response.code()}"
                            // You might want to post the error message to LiveData for observing in UI
                            println(errorMessage)
                        }
                    }

                    override fun onFailure(call: Call<KrogerProductsResponse>, t: Throwable) {
                        // Handle failure here
                        // Maybe show an error message to the user
                        // For example:
                        val errorMessage = "Failed to fetch products: ${t.message}"
                        // You might want to post the error message to LiveData for observing in UI
                        println(errorMessage)
                    }
                })
            }

            override fun onTokenFailed() {
                // Handle token retrieval failure here
                // Maybe show an error message to the user
                // For example:
                val errorMessage = "Failed to retrieve OAuth token"
                // You might want to post the error message to LiveData for observing in UI
                println(errorMessage)
            }
        })
    }

    private fun getToken(callback: TokenCallback) {
        krogerService.getAuthToken().enqueue(object : Callback<AuthTokenResponse> {
            override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken
                    if (token != null) {
                        callback.onTokenReceived(token)
                    } else {
                        callback.onTokenFailed()
                    }
                } else {
                    callback.onTokenFailed()
                }
            }

            override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
                callback.onTokenFailed()
            }
        })
    }

    // Callback interface for token retrieval
    private interface TokenCallback {
        fun onTokenReceived(token: String)
        fun onTokenFailed()
    }
}
