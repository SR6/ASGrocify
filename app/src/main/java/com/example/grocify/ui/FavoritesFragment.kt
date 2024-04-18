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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.RecyclerFragmentBinding
import com.example.grocify.models.KrogerProduct
import kotlinx.coroutines.launch

class FavoritesFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container,false)

        viewModel.updateHeader(
            getString(R.string.favorites),
            null,
            favoritesVisible = false,
            showBackButton = true)

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToProductFragment(productId, brand))
        }

        binding.recycler.adapter = productAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.loading.root.visibility = View.VISIBLE

        val products = mutableListOf<KrogerProduct>()

        viewModel.favoriteProducts.observe(viewLifecycleOwner) { favoriteProducts ->
            products.clear()
            lifecycleScope.launch {
                if (favoriteProducts != null) {
                    favoriteProducts.forEach { favoriteProduct ->
                        if (products.none { it.productId == favoriteProduct.productId }) {
                            val product = viewModel.getProductById(favoriteProduct.productId)
                            product?.product?.let { products.add(it) }
                        }
                    }
                    binding.loading.root.visibility = View.GONE
                    if (products.isEmpty()) {
                        viewModel.clearProducts()
                        productAdapter.submitList(emptyList())
                        binding.noProductsFound.visibility = View.VISIBLE
                    }
                    else {
                        binding.noProductsFound.visibility = View.GONE
                        productAdapter.submitList(products)
                        productAdapter.notifyDataSetChanged()
                    }
                }
                viewModel.updateHeader(
                    getString(R.string.favorites),
                    resources.getQuantityString(
                        R.plurals.items_quantity,
                        products.size,
                        viewModel.addCommasToNumber(products.size)
                    ),
                    favoritesVisible = false,
                    searchVisible = false,
                    showBackButton = false
                )
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
    }
}