package com.example.grocify.models

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userId") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: Timestamp,
    @SerializedName("lastLoginAt") val lastLoginAt: Timestamp,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("zipCode") val zipCode: String,
    @SerializedName("locationId") val locationId: String
)

data class GrocifyProduct(
    @SerializedName("grocifyProductId") val grocifyProductId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("cartCount") val cartCount: Int,
    @SerializedName("favoriteCount") val favoriteCount: Int,
    @SerializedName("transactionCount") val transactionCount: Int,
    @SerializedName("addedAt") val addedAt: Timestamp
)

data class GrocifyCategory(
    @SerializedName("name") val name: String,
    @SerializedName("imageFile") val imageFile: String
)

data class UserProduct(
    @SerializedName("userProductId") val userProductId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("count") val count: Int,
    @SerializedName("addedAt") val addedAt: Timestamp
)

data class Transaction(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("purchasedAt") val purchasedAt: Timestamp
)