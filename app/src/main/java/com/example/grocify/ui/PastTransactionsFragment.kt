package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.RecyclerFragmentBinding

class PastTransactionsFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(
            resources.getString(R.string.past_transactions),
            null,
            favoritesVisible = false,
            showBackButton = true
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pastTransactionsAdapter = PastTransactionsAdapter(requireContext(), viewModel)

        binding.recycler.adapter = pastTransactionsAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getTransactions(
            viewModel.user.value!!.userId,
            onSuccess = { transactions ->
                if (transactions.isNullOrEmpty()) {
                    pastTransactionsAdapter.submitList(emptyList())
                    binding.noResultsFound.visibility = View.VISIBLE
                }
                else {
                    binding.noResultsFound.visibility = View.GONE
                    pastTransactionsAdapter.submitList(transactions.sortedByDescending { it.purchasedAt?.seconds })
                }
            },
            onFailure = {
                Toast.makeText(context, resources.getString(R.string.past_transactions_load_failed), Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}