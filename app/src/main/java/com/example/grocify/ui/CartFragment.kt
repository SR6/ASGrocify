package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grocify.databinding.RecyclerFragmentBinding

class CartFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: RecyclerFragmentBinding? = null
    //private var _recyclerBinding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!
    //private val recyclerBinding get() = _recyclerBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader("Cart", "0")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = RecyclerFragmentBinding.bind(view)
//        val productAdapter = ProductAdapter(
//            requireContext(),
//            viewLifecycleOwner,
//            viewModel,
//            findNavController(),
//            ProductAdapter.FragmentDirections.CART
//        )
//
//        binding.recycler.adapter = productAdapter
//        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        //TODO: much like ItemsFragment, load the cartProductList here
//        viewModel.observeCartList().observe(viewLifecycleOwner){
//            rowAdapter.submitList(it)
//            //listSize = it.size
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}