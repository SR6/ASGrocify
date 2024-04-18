package com.example.grocify.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var productAdapter: ProductAdapter

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

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(ProductsFragmentDirections.actionProductsFragmentToProductFragment(productId, brand))
        }

        binding.recycler.adapter = productAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.loading.root.visibility = View.VISIBLE
        binding.noProductsFound.visibility = View.GONE

        lifecycleScope.launch {
            viewModel.setIsApiRequestCompleted(false)
            viewModel.getProducts(args.category)
        }

        viewModel.clearProducts()

        viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted) {
                viewModel.products.observe(viewLifecycleOwner) { products ->
                    if (products != null) {
                        binding.loading.root.visibility = View.GONE
                        if (products.products.isEmpty()) {
                            viewModel.clearProducts()
                            productAdapter.submitList(emptyList())
                            binding.noProductsFound.visibility = View.VISIBLE
                        }
                        else {
                            binding.noProductsFound.visibility = View.GONE
                            productAdapter.submitList(products.products)
                        }
                    }
                    viewModel.updateHeader(
                        if (args.category.length > 14) args.category.substring(0, 14) + "..." else args.category,
                        resources.getQuantityString(
                            R.plurals.items_quantity,
                            products?.meta?.pagination?.total ?: 0,
                            viewModel.addCommasToNumber(products?.meta?.pagination?.total ?: 0)
                        ),
                        favoritesVisible = true,
                        searchVisible = false,
                        showBackButton = true
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        productAdapter.submitList(emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearProducts()
    }
}