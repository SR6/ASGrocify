package com.example.grocify.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.HeaderBinding
import com.example.grocify.databinding.RecyclerFragmentBinding
import com.example.grocify.databinding.SearchFragmentBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    //private var _binding: SearchFragmentBinding? = null
    private var _binding : RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater,container,false)

        viewModel.updateHeader(null, "50 items found", favoritesVisible = false, searchVisible = true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productAdapter = ProductAdapter(viewModel) {
            //Don't think we actually need anything here unless you want to go to single item view from cart?
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = productAdapter


        Log.d("Search","in onViewCreated of SearchFragment")

        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products != null) {
                productAdapter.submitList(products.products)
            }
        }
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}