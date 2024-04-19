package com.example.grocify.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.MainActivity
import com.example.grocify.R
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.databinding.RecyclerFragmentBinding
import kotlinx.coroutines.launch

class SearchFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    private var _headerBinding: HeaderBinding? = null
    private val headerBinding get() = _headerBinding!!

    private lateinit var productAdapter: ProductAdapter
    private var isNavigatingToProduct = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container,false)

        viewModel.updateHeader(
            null,
            null,
            favoritesVisible = false,
            searchVisible = true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel, false)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToProductFragment(productId, brand))
            isNavigatingToProduct = true
        }

        binding.recycler.adapter = productAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        _headerBinding = (requireActivity() as MainActivity).headerBinding

        headerBinding.search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    productAdapter.submitList(emptyList())
                    binding.loading.root.visibility = View.VISIBLE
                    binding.noProductsFound.visibility = View.GONE

                    hideKeyboard()
                    headerBinding.search.clearFocus()

                    lifecycleScope.launch {
                        viewModel.setIsApiRequestCompleted(false)
                        viewModel.getProducts(query, true)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted) {
                viewModel.searchProducts.observe(viewLifecycleOwner) { products ->
                    if (products != null) {
                        binding.loading.root.visibility = View.GONE
                        if (products.products.isEmpty()) {
                            productAdapter.submitList(emptyList())
                            binding.noProductsFound.visibility = View.VISIBLE
                        }
                        else {
                            binding.noProductsFound.visibility = View.GONE
                            productAdapter.submitList(products.products)
                        }
                    }
                    viewModel.updateHeader(
                        null,
                        resources.getQuantityString(
                            R.plurals.items_quantity_header,
                            products?.meta?.pagination?.total ?: 0,
                            viewModel.addCommasToNumber(products?.meta?.pagination?.total ?: 0)
                        ),
                        favoritesVisible = false,
                        searchVisible = true,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isNavigatingToProduct) {
            productAdapter.submitList(emptyList())
            headerBinding.search.setQuery("", false)
        }
        isNavigatingToProduct = false
    }


    fun Fragment.hideKeyboard() {
        val input = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        input.hideSoftInputFromWindow(requireActivity().window.decorView.rootView.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}