package com.example.grocify.ui

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.async
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToProductFragment(productId, brand))
        }

        binding.recycler.adapter = productAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.loading.root.visibility = View.VISIBLE

        viewModel.favoriteProducts.observe(viewLifecycleOwner) { favoriteProducts ->
            if (favoriteProducts != null) {
                val products = mutableListOf<KrogerProduct>()
                lifecycleScope.launch {
//                    for (favoriteProduct in favoriteProducts) {
//                        viewModel.getProductById(favoriteProduct.productId)
//
//                        viewModel.product.observe(viewLifecycleOwner) { product ->
//                            product?.let {
//                                products.add(product.product)
//                            }
//                        }
//                    }
                    val deferredProducts = favoriteProducts.map { favoriteProduct ->
                        async {
                            viewModel.getProductById(favoriteProduct.productId)

                            viewModel.product.observe(viewLifecycleOwner) { product ->
                                product?.let {
                                    products.add(product.product)
                                }
                            }
                        }
                    }

                    deferredProducts.forEach { it.await() }

                    binding.loading.root.visibility = View.GONE
                    productAdapter.submitList(products)

                    if (products.size == 1)
                        viewModel.updateHeader(
                            "Favorites",
                            products.size.toString()
                                    + "\n" + resources.getString(R.string.item),
                            favoritesVisible = false,
                            searchVisible = false,
                            showBackButton = false)
                    else
                        viewModel.updateHeader(
                            "Favorites",
                            viewModel.addCommasToNumber(products.size)
                                    + "\n" + resources.getString(R.string.items),
                            favoritesVisible = false,
                            searchVisible = false,
                            showBackButton = false)
                }
            }
            else {
                Log.d("HERE1", "HERE")
                binding.loading.root.visibility = View.GONE
                binding.noProductsFound.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}