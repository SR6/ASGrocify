package com.example.grocify.api

import com.example.grocify.BuildConfig
import com.example.grocify.models.AuthTokenResponse
import com.example.grocify.models.KrogerLocationsResponse
import com.example.grocify.models.KrogerProductResponse
import com.example.grocify.models.KrogerProductsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object KrogerClient {
    private const val BASE_URL = "https://api.kroger.com/v1/"

    private val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    val krogerService: IKrogerService by lazy {
        retrofit.create(IKrogerService::class.java)
    }
}

interface IKrogerService {
    @FormUrlEncoded
    @POST("connect/oauth2/token")
    suspend fun getAuthToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Field("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET,
        @Field("scope") scope: String = BuildConfig.PRODUCT_SCOPE,
    ): AuthTokenResponse

    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String,
        @Query("filter.brand") brand: String?,
        @Query("filter.limit") limit: Int?,
        @Query("filter.locationId") locationId: String?,
        @Query("filter.productId") productId: String?,
        @Query("filter.start") start: Int?,
        @Query("filter.term") term: String?
    ): KrogerProductsResponse

    @GET("products/{productId}")
    suspend fun getProductById(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String,
        @Path("productId") productId: String,
        @Query("filter.locationId") locationId: String?
    ): KrogerProductResponse

    @GET("locations")
    suspend fun getLocations(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String,
        @Query("filter.zipCode.near") zipCodeNear: String?,
        @Query("filter.latLong.near") latLongNear: String?,
        @Query("filter.lat.near") latNear: String?,
        @Query("filter.lon.near") lonNear: String?,
        @Query("filter.radiusInMiles") radiusInMiles: Int?,
        @Query("filter.limit") limit: Int?,
        @Query("filter.chain") chain: String?,
        @Query("filter.department") department: String?,
        @Query("filter.locationId") locationId: String?
    ): KrogerLocationsResponse
}