package com.example.grocify.db

import com.example.grocify.models.GrocifyProduct
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GrocifyProductDatabaseConnection {
    private val db: FirebaseFirestore = Firebase.firestore

    private fun deserializeGrocifyProduct(documentSnapshot: DocumentSnapshot): GrocifyProduct {
        val grocifyProductId = documentSnapshot.id
        val productId = documentSnapshot.getString("productId") ?: ""
        val cartCount = documentSnapshot.getLong("cartCount") ?: 0
        val favoriteCount = documentSnapshot.getLong("favoriteCount") ?: 0
        val transactionCount = documentSnapshot.getLong("transactionCount") ?: 0
        val addedAt = documentSnapshot.getTimestamp("addedAt") ?: Timestamp.now()
        return GrocifyProduct(grocifyProductId, productId, cartCount.toInt(), favoriteCount.toInt(), transactionCount.toInt(), addedAt)
    }

    fun getGrocifyProduct(productId: String,
                          onSuccess: (GrocifyProduct?) -> Unit,
                          onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection("products")
                .limit(1)
                .get()
                .addOnSuccessListener { initialResult ->
                    if (initialResult.isEmpty)
                        onSuccess(null)
                    else {
                        db.collection("products")
                            .whereEqualTo("productId", productId)
                            .get()
                            .addOnSuccessListener { result ->
                                if (result.isEmpty)
                                    onSuccess(null)
                                else
                                    onSuccess(deserializeGrocifyProduct(result.documents[0]))
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
        catch (e: Exception) {
            onFailure(e)
        }
    }

    fun grocifyProductListener(productId: String,
                               onSuccess: (GrocifyProduct?) -> Unit,
                               onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection("products")
                .whereEqualTo("productId", productId)
                .limit(1)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        onFailure(exception)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty)
                        onSuccess(deserializeGrocifyProduct(snapshot.documents[0]))
                    else
                        onSuccess(null)
                }
        }
        catch (e: Exception) {
            onFailure(e)
        }
    }

    fun addGrocifyProduct(grocifyProduct: GrocifyProduct,
                          onSuccess: () -> Unit,
                          onFailure: (Exception) -> Unit
    ) {
        try {
            val grocifyProductData = hashMapOf(
                "grocifyProductId" to grocifyProduct.grocifyProductId,
                "productId" to grocifyProduct.productId,
                "cartCount" to grocifyProduct.cartCount,
                "favoriteCount" to grocifyProduct.favoriteCount,
                "transactionCount" to grocifyProduct.transactionCount,
                "addedAt" to grocifyProduct.addedAt
            )

            db.collection("products")
                .document(grocifyProduct.grocifyProductId)
                .set(grocifyProductData)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
        catch (e: Exception) {
            onFailure(e)
        }
    }

    fun updateGrocifyProduct(grocifyProduct: GrocifyProduct,
                             onSuccess: () -> Unit,
                             onFailure: (Exception) -> Unit
    ) {
        try {
            val grocifyProductData: Map<String, Any> = hashMapOf(
                "grocifyProductId" to grocifyProduct.grocifyProductId,
                "productId" to grocifyProduct.productId,
                "cartCount" to grocifyProduct.cartCount,
                "favoriteCount" to grocifyProduct.favoriteCount,
                "transactionCount" to grocifyProduct.transactionCount,
                "addedAt" to grocifyProduct.addedAt
            )

            db.collection("products")
                .document(grocifyProduct.grocifyProductId)
                .update(grocifyProductData)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
        catch (e: Exception) {
            onFailure(e)
        }
    }

    fun removeGrocifyProduct(grocifyProductId: String,
                             onSuccess: () -> Unit,
                             onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection("products")
                .document(grocifyProductId)
                .delete()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
        catch (e: Exception) {
            onFailure(e)
        }
    }
}