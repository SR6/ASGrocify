package com.example.grocify.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R
import com.example.grocify.databinding.ProductItemBinding
import com.example.grocify.db.Glide
import com.example.grocify.models.KrogerProduct
import com.example.grocify.models.UserProduct
import com.google.firebase.Timestamp

class ProductAdapter(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: MainViewModel,
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

        if (product.items[0].price != null) {
            holder.productItemBinding.productPrice.textSize = 20F
            if (product.items[0].price!!.promo != 0.0 && product.items[0].price!!.promo < product.items[0].price!!.regular) {
                holder.productItemBinding.productPrice.text = String.format("$%.2f", product.items[0].price!!.promo)
                holder.productItemBinding.onSale.visibility = View.VISIBLE
                holder.productItemBinding.productOldPrice.text = String.format("$%.2f", product.items[0].price!!.regular)
                holder.productItemBinding.productOldPrice.visibility = View.VISIBLE
                holder.productItemBinding.productOldPrice.paintFlags = holder.productItemBinding.productOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            }
            else {
                holder.productItemBinding.productPrice.text = String.format("$%.2f", product.items[0].price!!.regular)
                holder.productItemBinding.onSale.visibility = View.GONE
                holder.productItemBinding.productOldPrice.visibility = View.GONE
            }
        }
        else {
            holder.productItemBinding.productPrice.text = context.resources.getString(R.string.unavailable_price)
            holder.productItemBinding.productPrice.textSize = 15F
            holder.productItemBinding.onSale.visibility = View.GONE
        }

        if (product.items[0].inventory != null && product.items[0].inventory!!.stockLevel == "TEMPORARILY_OUT_OF_STOCK") {
            holder.productItemBinding.outOfStock.visibility = View.VISIBLE
            holder.productItemBinding.toggleCart.isClickable = false
            holder.productItemBinding.toggleCart.isEnabled = false
            holder.productItemBinding.toggleCart.setColorFilter(context.resources.getColor(R.color.gray))
        }
        else {
            holder.productItemBinding.outOfStock.visibility = View.GONE
            holder.productItemBinding.toggleCart.isClickable = true
            holder.productItemBinding.toggleCart.isEnabled = true
        }

        viewModel.favoriteProducts.observe(viewLifecycleOwner) { favoriteProducts ->
            val isInFavorites = favoriteProducts?.any { it.productId == product.productId } ?: false
            val drawableId = if (!isInFavorites) R.drawable.ic_unfavorite else R.drawable.ic_favorite
            holder.productItemBinding.toggleFavorites.setImageDrawable(ResourcesCompat.getDrawable(context.resources, drawableId, null))
        }

        holder.productItemBinding.toggleFavorites.setOnClickListener {
            val isInFavorites = viewModel.favoriteProducts.value?.any { it.productId == product.productId } ?: false

            if (!isInFavorites) {
                viewModel.addToFavorites(
                    UserProduct(viewModel.user.value!!.userId + product.productId,
                        viewModel.user.value!!.userId,
                        product.productId,
                        Timestamp.now()),
                    onSuccess = {
                        holder.productItemBinding.toggleFavorites.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_favorite, null))
                    },
                    onFailure = {
                        Toast.makeText(context, context.resources.getString(R.string.add_to_favorites_failed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else {
                viewModel.removeFromFavorites(
                    UserProduct(viewModel.user.value!!.userId + product.productId,
                        viewModel.user.value!!.userId,
                        product.productId,
                        null),
                    onSuccess = {
                        holder.productItemBinding.toggleFavorites.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_unfavorite, null))
                    },
                    onFailure = {
                        Toast.makeText(context, context.resources.getString(R.string.remove_from_favorites_failed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        viewModel.cartProducts.observe(viewLifecycleOwner) { cartProducts ->
            val isInCart = cartProducts?.any { it.productId == product.productId } ?: false
            val drawableId = if (!isInCart) R.drawable.ic_add else R.drawable.ic_remove
            holder.productItemBinding.toggleCart.setImageDrawable(ResourcesCompat.getDrawable(context.resources, drawableId, null))
        }

        holder.productItemBinding.toggleCart.setOnClickListener {
            val isInCart = viewModel.cartProducts.value?.any { it.productId == product.productId } ?: false

            if (!isInCart) {
                viewModel.addToCart(
                    UserProduct(viewModel.user.value!!.userId + product.productId,
                        viewModel.user.value!!.userId,
                        product.productId,
                        Timestamp.now()),
                    onSuccess = {
                        holder.productItemBinding.toggleCart.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_remove, null))
                    },
                    onFailure = {
                        Toast.makeText(context, context.resources.getString(R.string.add_to_cart_failed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else {
                viewModel.removeFromCart(
                    UserProduct(viewModel.user.value!!.userId + product.productId,
                        viewModel.user.value!!.userId,
                        product.productId,
                        null),
                    onSuccess = {
                        holder.productItemBinding.toggleCart.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_add, null))
                    },
                    onFailure = {
                        Toast.makeText(context, context.resources.getString(R.string.remove_from_cart_failed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
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