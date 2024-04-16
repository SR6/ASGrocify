package com.example.grocify.models

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class GrocifyProduct(
    @SerializedName("productId") val productId: String,
    @SerializedName("cartCount") val cartCount: Int
)

data class UserProduct(
    @SerializedName("userProductId") val userProductId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("addedAt") val addedAt: Timestamp?
)

data class GrocifyCategory(
    @SerializedName("name") val name: String,
    @SerializedName("imageFile") val imageFile: String
)

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