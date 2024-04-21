package com.example.grocify.db

import com.example.grocify.models.Transaction
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TransactionsDatabaseConnection {
    private val db: FirebaseFirestore = Firebase.firestore

    private fun deserializeTransaction(documentSnapshot: DocumentSnapshot): Transaction {
        val transactionId = documentSnapshot.id
        val userId = documentSnapshot.getString("userId") ?: ""
        val totalItems = documentSnapshot.getLong("totalItems") ?: 0
        val totalPrice = documentSnapshot.getDouble("totalPrice") ?: 0.0
        val purchasedAt = documentSnapshot.getTimestamp("purchasedAt") ?: Timestamp.now()
        return Transaction(transactionId, userId, totalItems.toInt(), totalPrice, purchasedAt)
    }

    fun getUserTransactions(
        userId: String,
        onSuccess: (List<Transaction>?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection("transactions")
                .limit(1)
                .get()
                .addOnSuccessListener { initialResult ->
                    if (initialResult.isEmpty)
                        onSuccess(null)
                    else {
                        db.collection("transactions")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener { result ->
                                if (result.isEmpty)
                                    onSuccess(null)
                                else
                                    onSuccess(result.documents.map { deserializeTransaction(it) })
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

    fun addTransaction(transaction: Transaction,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit
    ) {
        try {
            val transactionData = hashMapOf(
                "transactionId" to transaction.transactionId,
                "userId" to transaction.userId,
                "totalItems" to transaction.totalItems,
                "totalPrice" to transaction.totalPrice,
                "purchasedAt" to transaction.purchasedAt
            )

            db.collection("transactions")
                .document(transaction.transactionId)
                .set(transactionData)
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