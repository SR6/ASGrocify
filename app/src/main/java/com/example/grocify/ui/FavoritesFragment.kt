package com.example.grocify.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.RecyclerFragmentBinding

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

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel, true)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(FavoritesFragmentDirections.actionFavoritesFragmentToProductFragment(productId, brand))
        }

        binding.recycler.adapter = productAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.loading.root.visibility = View.VISIBLE

        viewModel.favoriteProducts.observe(viewLifecycleOwner) { favoriteProducts ->
            Log.d("HERE123", "HERE")
            if (favoriteProducts != null) {
                binding.loading.root.visibility = View.GONE
                if (favoriteProducts.isEmpty()){
                    productAdapter.submitList(emptyList())
                    binding.noResultsFound.visibility = View.VISIBLE
                }
                else {
                    Log.d("HERE1234", "HERE")
                    binding.noResultsFound.visibility = View.GONE
                    productAdapter.submitList(favoriteProducts)
                }
            }
            else {
                binding.loading.root.visibility = View.GONE
                productAdapter.submitList(emptyList())
                binding.noResultsFound.visibility = View.VISIBLE
            }

            viewModel.updateHeader(
                getString(R.string.favorites),
                resources.getQuantityString(
                    R.plurals.items_quantity_header,
                    favoriteProducts?.size ?: 0,
                    viewModel.addCommasToNumber(favoriteProducts?.size ?: 0)
                ),
                favoritesVisible = false,
                showBackButton = true
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}