package com.example.grocify.api

import com.example.grocify.models.KrogerProductsResponse
import com.example.grocify.models.KrogerProductResponse
import com.example.grocify.models.AuthTokenResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IKrogerService {
    @FormUrlEncoded
    @POST("connect/oauth2/authorize")
    fun getAuthCode(
        @Header("Authorization") credentials: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): Call<OAuthTokenResponse>

    @FormUrlEncoded
    @POST("connect/oauth2/token")
    fun getAuthToken(
        @Header("Authorization") credentials: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): Call<AuthTokenResponse>

    @GET("products")
    fun getProducts(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String,
        @Query("filter.brand") brand: String?,
        @Query("filter.limit") limit: String?,
        @Query("filter.productId") productId: String?,
        @Query("filter.start") start: String?,
        @Query("filter.term") term: String?
    ): Call<KrogerProductsResponse>

    @GET("products/{productId}")
    fun getProductById(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String,
        @Path("productId") productId: String,
    ): Call<KrogerProductResponse>
}