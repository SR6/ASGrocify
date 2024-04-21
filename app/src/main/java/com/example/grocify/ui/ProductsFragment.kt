package com.example.grocify.ui

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
import androidx.recyclerview.widget.RecyclerView
import com.example.grocify.R
import com.example.grocify.databinding.RecyclerFragmentBinding
import kotlinx.coroutines.launch

class ProductsFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private val args: ProductsFragmentArgs by navArgs()

    private var _binding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(
            args.category,
            null,
            showBackButton = true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductsAdapter(requireContext(), viewLifecycleOwner, viewModel, false)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(ProductsFragmentDirections.actionProductsFragmentToProductFragment(productId, brand))
        }

        binding.recycler.adapter = productAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.loading.root.visibility = View.VISIBLE
        binding.noResultsFound.visibility = View.GONE

        lifecycleScope.launch {
            viewModel.setIsApiRequestCompleted(false)
            viewModel.getProducts(args.category)
        }

        viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted) {
                viewModel.products.observe(viewLifecycleOwner) { products ->
                    if (products != null) {
                        binding.loading.root.visibility = View.GONE
                        if (products.products.isEmpty()) {
                            productAdapter.submitList(emptyList())
                            binding.noResultsFound.visibility = View.VISIBLE
                        }
                        else {
                            binding.noResultsFound.visibility = View.GONE
                            productAdapter.addAdditionalProducts(products.products)
                        }
                    }

                    viewModel.updateHeader(
                        if (args.category.length > 14) args.category.substring(0, 14) + "..." else args.category,
                        resources.getQuantityString(
                            R.plurals.items_quantity_header,
                            products?.meta?.pagination?.total ?: 0,
                            viewModel.addCommasToNumber(products?.meta?.pagination?.total ?: 0)
                        ),
                        showBackButton = true
                    )
                }
            }
        }

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = (binding.recycler.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (binding.recycler.layoutManager as LinearLayoutManager).itemCount
                val firstVisibleItemPosition = (binding.recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    binding.loading.root.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        viewModel.setIsApiRequestCompleted(false)
                        viewModel.getProducts(args.category, productAdapter.itemCount)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}