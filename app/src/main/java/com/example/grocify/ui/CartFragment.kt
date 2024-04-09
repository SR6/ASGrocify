package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.databinding.CartFragmentBinding
import com.example.grocify.databinding.RecyclerFragmentBinding

class CartFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: CartFragmentBinding? = null
    private var _recyclerBinding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!
    private val recyclerBinding get() = _recyclerBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CartFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader("Cart", "0")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _recyclerBinding = RecyclerFragmentBinding.bind(view)
        val productAdapter = ProductAdapter(viewModel)
        recyclerBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        recyclerBinding.recycler.adapter = productAdapter
//        viewModel.observeCartList().observe(viewLifecycleOwner){
//            rowAdapter.submitList(it)
//            //listSize = it.size
//        }
    }
    override fun onDestroyView() {
        _binding = null
        _recyclerBinding = null
        super.onDestroyView()
    }
}