package com.example.grocify.db

import com.example.grocify.models.UserProduct
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserProductDatabaseConnection(private var databaseCollection: String) {
    private val db: FirebaseFirestore = Firebase.firestore

    private fun deserializeUserProduct(documentSnapshot: DocumentSnapshot): UserProduct {
        val userProductId = documentSnapshot.id
        val userId = documentSnapshot.getString("userId") ?: ""
        val productId = documentSnapshot.getString("productId") ?: ""
        val count = documentSnapshot.getLong("count") ?: 0
        val addedAt = documentSnapshot.getTimestamp("addedAt") ?: Timestamp.now()
        return UserProduct(userProductId, userId, productId, count.toInt(), addedAt)
    }

    fun getUserProducts(userId: String,
                        onSuccess: (List<UserProduct>?) -> Unit,
                        onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection(databaseCollection)
                .limit(1)
                .get()
                .addOnSuccessListener { initialResult ->
                    if (initialResult.isEmpty)
                        onSuccess(null)
                    else {
                        db.collection(databaseCollection)
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener { result ->
                                if (result.isEmpty)
                                    onSuccess(null)
                                else
                                    onSuccess(result.documents.map { deserializeUserProduct(it) })
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

    fun addUserProduct(userProduct: UserProduct,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit
    ) {
        try {
            val userProductData = hashMapOf(
                "userProductId" to userProduct.userProductId,
                "userId" to userProduct.userId,
                "productId" to userProduct.productId,
                "count" to userProduct.count,
                "addedAt" to userProduct.addedAt
            )

            db.collection(databaseCollection)
                .document(userProduct.userProductId)
                .set(userProductData)
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

    fun updateUserProduct(userProduct: UserProduct,
                          onSuccess: () -> Unit,
                          onFailure: (Exception) -> Unit
    ) {
        try {
            val userProductData: Map<String, Any> = hashMapOf(
                "userProductId" to userProduct.userProductId,
                "userId" to userProduct.userId,
                "productId" to userProduct.productId,
                "count" to userProduct.count,
                "addedAt" to userProduct.addedAt
            )

            db.collection(databaseCollection)
                .document(userProduct.userProductId)
                .update(userProductData)
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

    fun removeUserProduct(userProductId: String,
                          onSuccess: () -> Unit,
                          onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection(databaseCollection)
                .document(userProductId)
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