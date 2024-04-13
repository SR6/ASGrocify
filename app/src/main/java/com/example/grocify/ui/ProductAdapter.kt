package com.example.grocify.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R
import com.example.grocify.databinding.ProductItemBinding
import com.example.grocify.db.Glide
import com.example.grocify.models.KrogerProduct

class ProductAdapter(private val viewModel:MainViewModel,
    private val navigateToSingleItem: (String) -> Unit )
    : ListAdapter<KrogerProduct, ProductAdapter.VH>(ItemDiff()) {

    inner class VH(val rowCategoryBinding : ProductItemBinding)
        : RecyclerView.ViewHolder(rowCategoryBinding.root) {
        init {
            itemView.setOnClickListener {
                val currentPos = bindingAdapterPosition
                val prodList = viewModel.observeProductList()
                val product = prodList[currentPos]
                //val prodId = product.productId
                Log.d("ItemNav"," OnClickListener ProductID ${product.productId} UPC ${product.upc}")
                navigateToSingleItem(product.productId)
                //notifyDataSetChanged()
            }
            itemView.setOnLongClickListener {
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowBinding = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(rowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val productPick = getItem(position)
        //below is mad because it can't translate concatenated strings
        if (productPick.brand != null) {
            holder.rowCategoryBinding.productName.text = productPick.brand + " " + productPick.description
        } else {
            holder.rowCategoryBinding.productName.text = productPick.description
        }
        val itemPrice = productPick.items[0].price
        //val itemList = productPick.items
        //val itemPrice = itemList.price
        if (itemPrice != null) {
            holder.rowCategoryBinding.productPrice.text = "$" + itemPrice.regular.toString() //"price".toString()
        }
        if (!productPick.images.isEmpty()) {
            val imageUrl = productPick.images[0].sizes[0].url
            Glide.loadProductImage(imageUrl, holder.rowCategoryBinding.productImage,250,250)
        }
//        if (productPick.inCart) {
//            holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.ic_delete)
//        } else {
            holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.ic_add)
        holder.rowCategoryBinding.addToFavorites.setImageResource(R.drawable.ic_favorites)
//        }
        //set ClickListener for button to add to Cart
        holder.rowCategoryBinding.addToCart.setOnClickListener{
            var row = getItem(position)
            //we need to decide how we want to do this. Do we have another list that holds just these items?
//            row.inCart = !row.inCart
//            viewModel.setCartList(row)
            //Need to change icon based on item being in cart or not
//            if (row.inCart) {
//                holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.ic_delete)
//            } else {
                holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.ic_add)
//            }

        }
    }

    class ItemDiff : DiffUtil.ItemCallback<KrogerProduct>() {
        override fun areItemsTheSame(oldItem: KrogerProduct, newItem: KrogerProduct): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: KrogerProduct, newItem: KrogerProduct): Boolean {
            return oldItem.itemInformation == newItem.itemInformation &&
                    oldItem.description == newItem.description &&
                    oldItem.brand == newItem.brand
            //what else might need to be checked for comparison? Price once it is fixed?
        }
    }
}