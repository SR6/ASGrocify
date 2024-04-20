package com.example.grocify.db

import com.example.grocify.models.GrocifyCategory
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException

class CategoryDatabaseConnection {
    private val db: FirebaseFirestore = Firebase.firestore

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
}