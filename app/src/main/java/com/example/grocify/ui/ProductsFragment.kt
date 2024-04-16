package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.RecyclerFragmentBinding
import kotlinx.coroutines.launch

class ProductsFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private val args: ProductsFragmentArgs by navArgs()

    private var _binding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(
            args.category,
            null,
            favoritesVisible = true,
            searchVisible = false,
            showBackButton = true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel, findNavController())

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = productAdapter

        binding.loading.root.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.setIsApiRequestCompleted(false)
            viewModel.getProducts(args.category)
        }

        viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted) {
                viewModel.products.observe(viewLifecycleOwner) { products ->
                    if (products != null) {
                        binding.loading.root.visibility = View.GONE
                        productAdapter.submitList(products.products)

                        var category = args.category
                        if (category.length > 14)
                            category = category.substring(0, 14) + "..."

                        if (products.products.size == 1)
                            viewModel.updateHeader(category,
                                products.meta.pagination.total.toString() +
                                        resources.getString(R.string.item),
                                favoritesVisible = true,
                                searchVisible = false,
                                showBackButton = true)
                        else
                            viewModel.updateHeader(category,
                                viewModel.addCommasToNumber(products.meta.pagination.total)
                                 + "\n" + resources.getString(R.string.items),
                                favoritesVisible = true,
                                searchVisible = false,
                                showBackButton = true)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}