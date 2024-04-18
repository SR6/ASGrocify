package com.example.grocify.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R

class PastPurchaseAdapter(
    private val viewModel:MainViewModel,
    private val purchases: List<PastTransactionsFragment.PurchaseItem>
): RecyclerView.Adapter<PastPurchaseAdapter.PurchaseViewHolder>() {
    inner class PurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemCountTextView: TextView = itemView.findViewById(R.id.numItems)
        val totalCostTextView: TextView = itemView.findViewById(R.id.totalCost)
        val dateTextView: TextView = itemView.findViewById(R.id.purchaseDate)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.past_transaction_item, parent, false)
        return PurchaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        //size of the list in here
        return purchases.size
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val purchase = purchases[position]
        holder.itemCountTextView.text = buildString {
            append(purchase.itemCount.toString())
            append(" items")
        }
        holder.totalCostTextView.text = String.format("$%.2f", purchase.totalCost)
        holder.dateTextView.text = purchase.dateOfPurchase
    }
}