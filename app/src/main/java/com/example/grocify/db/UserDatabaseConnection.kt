package com.example.grocify.db

import com.example.grocify.models.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDatabaseConnection {
    private val db: FirebaseFirestore = Firebase.firestore

    private fun deserializeUser(documentSnapshot: DocumentSnapshot): User {
        val userId = documentSnapshot.id
        val email = documentSnapshot.getString("email") ?: ""
        val name = documentSnapshot.getString("name") ?: ""
        val createdAt = documentSnapshot.getTimestamp("createdAt") ?: Timestamp.now()
        val lastLoginAt = documentSnapshot.getTimestamp("lastLoginAt" ) ?: Timestamp.now()
        val paymentMethod = documentSnapshot.getString("paymentMethod") ?: ""
        val zipCode = documentSnapshot.getString("zipCode") ?: ""
        val locationId = documentSnapshot.getString("locationId") ?: ""
        return User(userId, email, name, createdAt, lastLoginAt, paymentMethod, zipCode, locationId)
    }

    fun getUser(
        email: String, onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection("users")
                .limit(1)
                .get()
                .addOnSuccessListener { initialResult ->
                    if (initialResult.isEmpty)
                        onSuccess(null)
                    else {
                        db.collection("users")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { result ->
                                if (result.isEmpty)
                                    onSuccess(null)
                                else {
                                    onSuccess(deserializeUser(result.documents[0]))
                                }
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

    fun addUser(user: User,
                onSuccess: () -> Unit,
                onFailure: (Exception) -> Unit
    ) {
        try {
            val userData = hashMapOf(
                "userId" to user.userId,
                "email" to user.email,
                "name" to user.name,
                "createdAt" to user.createdAt,
                "lastLoginAt" to user.lastLoginAt,
                "paymentMethod" to user.paymentMethod,
                "zipCode" to user.zipCode,
                "locationId" to user.locationId
            )

            db.collection("users")
                .document(user.userId)
                .set(userData)
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

    fun updateUser(user: User,
                   onSuccess: () -> Unit,
                   onFailure: (Exception) -> Unit
    ) {
        try {
            val userData: Map<String, Any> = hashMapOf(
                "userId" to user.userId,
                "email" to user.email,
                "name" to user.name,
                "createdAt" to user.createdAt,
                "lastLoginAt" to user.lastLoginAt,
                "paymentMethod" to user.paymentMethod,
                "zipCode" to user.zipCode,
                "locationId" to user.locationId
            )

            db.collection("users")
                .document(user.userId)
                .update(userData)
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