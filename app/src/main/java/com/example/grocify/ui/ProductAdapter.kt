package com.example.grocify.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R
import com.example.grocify.databinding.ProductItemBinding
import com.example.grocify.db.Glide
import com.example.grocify.models.KrogerProduct

class ProductAdapter(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: MainViewModel,
    private val isCartOrFavorites: Boolean
): ListAdapter<KrogerProduct, ProductAdapter.ViewHolder>(ItemDiff()) {
    inner class ViewHolder(val productItemBinding: ProductItemBinding): RecyclerView.ViewHolder(productItemBinding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val productId = getItem(position).productId
                    val brand = getItem(position)?.brand ?: context.resources.getString(R.string.grocify)
                    onItemClicked(productId, brand)
                }
            }
        }
    }

    var onItemClicked: (productId: String, brand: String) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position)

        Glide.loadProductImage(product.images[0].sizes[0].url, holder.productItemBinding.productImage,250,250)

        if (product.description.length > 50)
            holder.productItemBinding.productDescription.text = product.description.substring(0, 50) + "..."
        else
            holder.productItemBinding.productDescription.text = product.description

        var addToCartDisabled: Boolean
        val price = product.items.getOrNull(0)?.price

        if (price != null) {
            holder.productItemBinding.apply {
                productPrice.textSize = 22F
                if (price.promo != 0.0 && price.promo < price.regular) {
                    productPrice.text = String.format("$%.2f", price.promo)
                    onSale.visibility = View.VISIBLE
                    productOldPrice.apply {
                        text = String.format("$%.2f", price.regular)
                        visibility = View.VISIBLE
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                }
                else {
                    onSale.visibility = View.GONE
                    productPrice.text = String.format("$%.2f", price.regular)
                }
            }
            val inventory = product.items.getOrNull(0)?.inventory
            holder.productItemBinding.outOfStock.visibility = if (inventory?.stockLevel == "TEMPORARILY_OUT_OF_STOCK") View.VISIBLE else View.GONE
            addToCartDisabled = inventory?.stockLevel == "TEMPORARILY_OUT_OF_STOCK"
        }
        else {
            holder.productItemBinding.apply {
                productPrice.apply {
                    textSize = 15F
                    text = context.resources.getString(R.string.unavailable_price)
                    setTextColor(context.resources.getColor(R.color.gray, null))
                }
                onSale.visibility = View.GONE
                outOfStock.visibility = View.GONE
                addToCartDisabled = true
            }
        }

        val cartAndFavoritesHelpers = Helpers.CartAndFavoritesHelpers(
            context,
            viewLifecycleOwner,
            holder.productItemBinding.cartAndFavorites,
            viewModel,
            getItem(position),
            this,
            position,
            isCartOrFavorites,
            addToCartDisabled
        )

        cartAndFavoritesHelpers.toggleFavorites()
        cartAndFavoritesHelpers.toggleCart()
    }

    class ItemDiff : DiffUtil.ItemCallback<KrogerProduct>() {
        override fun areItemsTheSame(oldItem: KrogerProduct, newItem: KrogerProduct): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: KrogerProduct, newItem: KrogerProduct): Boolean {
            if (oldItem.items[0].price == null || oldItem.items[0].inventory == null)
                return false

            return oldItem.description == newItem.description &&
                    oldItem.items[0].price == newItem.items[0].price &&
                    oldItem.items[0].inventory == newItem.items[0].inventory
        }
    }
}