package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.FragmentRvBinding

class CartFragment : Fragment(R.layout.fragment_rv) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentRvBinding? = null
    private val binding get() = _binding!!

    private var listSize = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRvBinding.bind(view)
        val rowAdapter = RowAdapter(viewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = rowAdapter
        viewModel.observeCartList().observe(viewLifecycleOwner){
            rowAdapter.submitList(it)
            //listSize = it.size
        }
        viewModel.updateHeader("Cart", listSize.toString()) //make subtitle size of cartList
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}