package com.example.grocify.models

import com.google.gson.annotations.SerializedName

data class AuthTokenResponse(
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

data class KrogerProductsResponse(
    @SerializedName("data") val products: List<KrogerProduct>,
    @SerializedName("meta") val meta: Meta
)

data class KrogerProductResponse(
    @SerializedName("data") val products: KrogerProduct,
    @SerializedName("meta") val meta: Meta
)

data class KrogerProduct(
    @SerializedName("productId") val productId: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("countryOrigin") val countryOrigin: String,
    @SerializedName("description") val description: String,
    @SerializedName("items") val items: List<Item>,
    @SerializedName("itemInformation") val itemInformation: ItemInformation,
    @SerializedName("temperature") val temperature: Temperature,
    @SerializedName("images") val images: List<Image>,
)

data class Item(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("inventory") val inventory: Inventory,
    @SerializedName("favorite") val favorite: Boolean,
    @SerializedName("price") val price: Price,
    @SerializedName("nationalPrice") val nationalPrice: Price,
    @SerializedName("size") val size: String,
    @SerializedName("soldBy") val soldBy: String
)

data class Inventory(
    @SerializedName("stockLevel") val stockLevel: String
)

data class Price(
    @SerializedName("regular") val regular: Double,
    @SerializedName("promo") val promo: Double,
    @SerializedName("regularPerUnitEstimate") val regularPerUnitEstimate: Double,
    @SerializedName("promoPerUnitEstimate") val promoPerUnitEstimate: Double
)

data class ItemInformation(
    @SerializedName("depth") val depth: String,
    @SerializedName("height") val height: String,
    @SerializedName("width") val width: String
)

data class Temperature(
    @SerializedName("indicator") val indicator: String,
    @SerializedName("heatSensitive") val heatSensitive: Boolean
)

data class Image(
    @SerializedName("id") val id: String,
    @SerializedName("perspective") val perspective: String,
    @SerializedName("default") val default: Boolean,
    @SerializedName("sizes") val sizes: List<Size>
)

data class Size(
    @SerializedName("id") val id: String,
    @SerializedName("size") val size: String,
    @SerializedName("url") val url: String
)

data class Meta(
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("limit") val limit: Int,
    @SerializedName("nextUrl") val nextUrl: String?,
    @SerializedName("previousUrl") val previousUrl: String?
)
