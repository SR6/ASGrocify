package com.example.grocify.ui

import KrogerClient.krogerService
import KrogerService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.grocify.api.IKrogerService
import com.example.grocify.databinding.CategoryFragmentBinding
import com.example.grocify.models.KrogerProductsResponse
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback

class CategoryFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: CategoryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CategoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchProducts()
        viewModel.productsResponse.observe(viewLifecycleOwner, Observer { productsResponse ->
            binding.productName.text = productsResponse.products[0].brand
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}