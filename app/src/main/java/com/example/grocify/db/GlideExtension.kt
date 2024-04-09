package com.example.grocify.db

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.MemoryCacheAdapter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.example.grocify.R
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.InputStream

@GlideModule
class GlideExtension: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}

object Glide {
    private var glideOptions = RequestOptions().fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL)

    fun load(file: File, imageView: ImageView, width: Int, height: Int) {
        GlideApp.with(imageView.context)
            .load(file)
            .apply(glideOptions)
            .error(R.drawable.ic_invalid_image)
            .override(width, height)
            .into(imageView)
    }
}