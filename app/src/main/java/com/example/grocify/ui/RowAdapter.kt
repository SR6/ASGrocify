package com.example.grocify.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R
import com.example.grocify.databinding.CategoryFragmentBinding
import com.example.grocify.databinding.FragmentRvBinding
import com.example.grocify.databinding.RowItemBinding
import com.example.grocify.models.KrogerProduct
import com.example.grocify.models.KrogerProductsResponse

class RowAdapter(private val viewModel:MainViewModel)
    : ListAdapter<KrogerProduct, RowAdapter.VH>(ItemDiff()) {

    inner class VH(val rowCategoryBinding : RowItemBinding)
        : RecyclerView.ViewHolder(rowCategoryBinding.root) {
        init {
            itemView.setOnClickListener {
                //TODO: this should send to single item view
                //val currentPos = bindingAdapterPosition
                //navigateToItems(category)
                //notifyDataSetChanged()

            }
        }
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        //XXX Write me.
        val rowBinding = RowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
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
        Log.d("Price", "price is $itemPrice")
        //val itemList = productPick.items
        //val itemPrice = itemList.price
        if (itemPrice != null) {
            holder.rowCategoryBinding.productPrice.text = itemPrice.regular.toString() //"price".toString()
        }
        if (productPick.inCart) {
            holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.minus_sign)
        } else {
            holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.plus_sign)
        }
        //set ClickListener for button to add to Cart
        holder.rowCategoryBinding.addToCart.setOnClickListener{
            var row =getItem(position)
            //we need to decide how we want to do this. Do we have another list that holds just these items?
            row.inCart = !row.inCart
            viewModel.setCartList(row)
            //Need to change icon based on item being in cart or not
            if (row.inCart) {
                holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.minus_sign)
            } else {
                holder.rowCategoryBinding.addToCart.setImageResource(R.drawable.plus_sign)
            }

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