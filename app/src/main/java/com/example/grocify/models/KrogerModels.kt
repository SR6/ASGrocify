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
    @SerializedName("meta") val meta: ProductMeta
)

data class KrogerProductResponse(
    @SerializedName("product") val product: KrogerProduct,
)

data class KrogerProduct(
    @SerializedName("productId") val productId: String,
    @SerializedName("aisleLocations") val aisleLocations: List<AisleLocation>,
    @SerializedName("brand") val brand: String,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("countryOrigin") val countryOrigin: String,
    @SerializedName("description") val description: String,
    @SerializedName("items") val items: List<Item>,
    @SerializedName("itemInformation") val itemInformation: ItemInformation,
    @SerializedName("temperature") val temperature: Temperature,
    @SerializedName("images") val images: List<Image>,
    @SerializedName("upc") var ucp: String,
)

data class AisleLocation(
    @SerializedName("bayNumber") val bayNumber: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("numberOfFacings") val numberOfFacings: String? = null,
    @SerializedName("sequenceNumber") val sequenceNumber: String? = null,
    @SerializedName("side") val side: String? = null,
    @SerializedName("shelfNumber") val shelfNumber: String? = null,
    @SerializedName("shelfPositionInBay") val shelfPositionInBay: String? = null
)

data class Item(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("inventory") val inventory: Inventory,
    @SerializedName("favorite") val favorite: Boolean,
    @SerializedName("fulfillment") val fulfillment: Fulfillment,
    @SerializedName("price") val price: Price,
    @SerializedName("nationalPrice") val nationalPrice: Price,
    @SerializedName("size") val size: String,
    @SerializedName("soldBy") val soldBy: String
)

data class Inventory(
    @SerializedName("stockLevel") val stockLevel: String
)

data class Fulfillment(
    @SerializedName("curbside") val curbside: Boolean,
    @SerializedName("delivery") val delivery: Boolean,
    @SerializedName("instore") val instore: Boolean,
    @SerializedName("shiptohome") val shiptohome: Boolean
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

data class ProductMeta(
    @SerializedName("pagination") val pagination: Pagination
)

data class KrogerLocationsResponse(
    @SerializedName("data") val data: List<Location>,
    @SerializedName("meta") val meta: LocationMeta
)

data class Location(
    @SerializedName("address") val address: Address,
    @SerializedName("chain") val chain: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("departments") val departments: List<Department>,
    @SerializedName("geolocation") val geolocation: Geolocation,
    @SerializedName("hours") val hours: Hours,
    @SerializedName("locationId") val locationId: String,
    @SerializedName("storeNumber") val storeNumber: String,
    @SerializedName("divisionNumber") val divisionNumber: String,
    @SerializedName("name") val name: String
)

data class Address(
    @SerializedName("addressLine1") val addressLine1: String,
    @SerializedName("addressLine2") val addressLine2: String,
    @SerializedName("city") val city: String,
    @SerializedName("county") val county: String,
    @SerializedName("state") val state: String,
    @SerializedName("zipCode") val zipCode: String
)

data class Department(
    @SerializedName("departmentId") val departmentId: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("hours") val hours: Hours
)

data class Hours(
    @SerializedName("Open24") val Open24: Boolean,
    @SerializedName("gmtOffset") val gmtOffset: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("friday") val friday: Day,
    @SerializedName("monday") val monday: Day,
    @SerializedName("saturday") val saturday: Day,
    @SerializedName("sunday") val sunday: Day,
    @SerializedName("thursday") val thursday: Day,
    @SerializedName("tuesday") val tuesday: Day,
    @SerializedName("wednesday") val wednesday: Day
)

data class Day(
    @SerializedName("open") val open: String,
    @SerializedName("close") val close: Int,
    @SerializedName("open24") val open24: Boolean
)

data class Geolocation(
    @SerializedName("latLng") val latLng: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class LocationMeta(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("warnings") val warnings: List<String>
)

data class Pagination(
    @SerializedName("start") val start: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int
)