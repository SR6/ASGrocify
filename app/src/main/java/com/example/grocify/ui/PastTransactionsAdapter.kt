package com.example.grocify.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R
import com.example.grocify.databinding.PastTransactionItemBinding
import com.example.grocify.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class PastTransactionsAdapter(
    private val context: Context,
    private val viewModel: MainViewModel,
): ListAdapter<Transaction, PastTransactionsAdapter.ViewHolder>(ItemDiff()) {
    inner class ViewHolder(val pastTransactionItemBinding: PastTransactionItemBinding): RecyclerView.ViewHolder(pastTransactionItemBinding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION)
                    getItem(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PastTransactionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val purchaseDate = transaction.purchasedAt.toDate().let { dateFormat.format(it) }

        holder.pastTransactionItemBinding.purchaseDate.text = purchaseDate
        holder.pastTransactionItemBinding.totalItems.text = context.resources.getQuantityString(
            R.plurals.items_quantity,
            transaction.totalItems,
            viewModel.addCommasToNumber(transaction.totalItems)
        )
        holder.pastTransactionItemBinding.totalPrice.text = String.format("$%.2f", transaction.totalPrice)
    }

    class ItemDiff : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.transactionId == newItem.transactionId
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.totalItems == newItem.totalItems &&
                    oldItem.totalPrice == newItem.totalPrice &&
                    oldItem.purchasedAt == newItem.purchasedAt
        }
    }
}