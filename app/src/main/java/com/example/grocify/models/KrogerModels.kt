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
    @SerializedName("data") val product: KrogerProduct
)

data class KrogerProduct(
    @SerializedName("aisleLocations") val aisleLocations: List<AisleLocation>,
    @SerializedName("brand") val brand: String,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("countryOrigin") val countryOrigin: String,
    @SerializedName("description") val description: String,
    @SerializedName("images") val images: List<Image>,
    @SerializedName("itemInformation") val itemInformation: ItemInformation,
    @SerializedName("items") val items: List<Item>,
    @SerializedName("productId") val productId: String,
    @SerializedName("temperature") val temperature: Temperature,
    @SerializedName("upc") var upc: String
)

data class AisleLocation(
    @SerializedName("bayNumber") val bayNumber: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("numberOfFacings") val numberOfFacings: String? = null,
    @SerializedName("sequenceNumber") val sequenceNumber: String? = null,
    @SerializedName("shelfNumber") val shelfNumber: String? = null,
    @SerializedName("shelfPositionInBay") val shelfPositionInBay: String? = null,
    @SerializedName("side") val side: String? = null
)

data class Fulfillment(
    @SerializedName("curbside") val curbside: Boolean,
    @SerializedName("delivery") val delivery: Boolean,
    @SerializedName("instore") val instore: Boolean,
    @SerializedName("shiptohome") val shiptohome: Boolean
)

data class Image(
    @SerializedName("default") val default: Boolean,
    @SerializedName("id") val id: String,
    @SerializedName("perspective") val perspective: String,
    @SerializedName("sizes") val sizes: List<Size>
)

data class Inventory(
    @SerializedName("stockLevel") val stockLevel: String
)

data class Item(
    @SerializedName("favorite") val favorite: Boolean,
    @SerializedName("fulfillment") val fulfillment: Fulfillment,
    @SerializedName("inventory") val inventory: Inventory?,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("nationalPrice") val nationalPrice: Price,
    @SerializedName("price") val price: Price?,
    @SerializedName("size") val size: String,
    @SerializedName("soldBy") val soldBy: String
)

data class ItemInformation(
    @SerializedName("depth") val depth: String,
    @SerializedName("height") val height: String,
    @SerializedName("width") val width: String
)

data class Price(
    @SerializedName("promo") val promo: Double,
    @SerializedName("promoPerUnitEstimate") val promoPerUnitEstimate: Double,
    @SerializedName("regular") val regular: Double,
    @SerializedName("regularPerUnitEstimate") val regularPerUnitEstimate: Double
)

data class Size(
    @SerializedName("id") val id: String,
    @SerializedName("size") val size: String,
    @SerializedName("url") val url: String
)

data class Temperature(
    @SerializedName("heatSensitive") val heatSensitive: Boolean,
    @SerializedName("indicator") val indicator: String
)

data class KrogerLocationsResponse(
    @SerializedName("data") val data: List<Location>,
    @SerializedName("meta") val meta: Meta
)

data class Location(
    @SerializedName("address") val address: Address,
    @SerializedName("chain") val chain: String,
    @SerializedName("departments") val departments: List<Department>,
    @SerializedName("divisionNumber") val divisionNumber: String,
    @SerializedName("geolocation") val geolocation: Geolocation,
    @SerializedName("hours") val hours: Hours,
    @SerializedName("locationId") val locationId: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("storeNumber") val storeNumber: String
)

data class Address(
    @SerializedName("addressLine1") val addressLine1: String,
    @SerializedName("addressLine2") val addressLine2: String?,
    @SerializedName("city") val city: String,
    @SerializedName("county") val county: String,
    @SerializedName("state") val state: String,
    @SerializedName("zipCode") val zipCode: String
)

data class Department(
    @SerializedName("address") val address: Address?,
    @SerializedName("departmentId") val departmentId: String,
    @SerializedName("geolocation") val geolocation: Geolocation?,
    @SerializedName("hours") val hours: Hours?,
    @SerializedName("name") val name: String,
    @SerializedName("offsite") val offsite: Boolean?,
    @SerializedName("phone") val phone: String?
)

data class Hours(
    @SerializedName("friday") val friday: Day,
    @SerializedName("gmtOffset") val gmtOffset: String,
    @SerializedName("monday") val monday: Day,
    @SerializedName("open24") val open24: Boolean,
    @SerializedName("saturday") val saturday: Day,
    @SerializedName("sunday") val sunday: Day,
    @SerializedName("thursday") val thursday: Day,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("tuesday") val tuesday: Day,
    @SerializedName("wednesday") val wednesday: Day
)

data class Day(
    @SerializedName("close") val close: String,
    @SerializedName("open") val open: String,
    @SerializedName("open24") val open24: Boolean
)

data class Geolocation(
    @SerializedName("latLng") val latLng: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class Meta(
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("limit") val limit: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("total") val total: Int
)
