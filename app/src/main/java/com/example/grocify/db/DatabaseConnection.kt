package com.example.grocify.db

import android.util.Log
import com.example.grocify.models.GrocifyCategory
import com.example.grocify.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException

class DatabaseConnection {
    private val db: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage
    fun getCategories(onSuccess: (List<GrocifyCategory>) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            db.collection("categories")
                .get()
                .addOnSuccessListener { result ->
                    val categoriesList = mutableListOf<GrocifyCategory>()
                    for (document in result) {
                        val name = document.getString("name") ?: ""
                        val imageFile = document.getString("imageFile") ?: ""
                        categoriesList.add(GrocifyCategory(name, imageFile))
                    }
                    onSuccess(categoriesList)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
        catch (e: IOException) {
            onFailure(e)
        }
    }

    fun getCategoryImage(imageFile: String, onSuccess: (File) -> Unit, onFailure: (Exception) -> Unit) {
        if (imageFile.isBlank()) {
            onFailure(IllegalArgumentException("Image file is empty"))
            return
        }

        val storageUrl = FirebaseApp.getInstance().options.storageBucket
        val imageUrl = "gs://$storageUrl/categoryImages/$imageFile"
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        try {
            val localFile = File.createTempFile("images", "png")
            storageReference.getFile(localFile)
                .addOnSuccessListener {
                    onSuccess(localFile)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
        catch (e: IOException) {
            onFailure(e)
        }
    }

    fun getUser(email: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty)
                        onSuccess(null)
                    else {
                        onSuccess(result.documents[0].toObject(User::class.java))
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
                onFailure: (Exception) -> Unit) {
        try {
            val userData = hashMapOf(
                "email" to user.email,
                "name" to user.name,
                "paymentMethod" to user.paymentMethod,
                "zipCode" to user.zipCode
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
                   onFailure: (Exception) -> Unit) {
        try {
            val userData: Map<String, Any> = hashMapOf(
                "email" to (user.email),
                "name" to (user.name),
                "paymentMethod" to (user.paymentMethod ?: ""),
                "zipCode" to (user.zipCode ?: "")
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