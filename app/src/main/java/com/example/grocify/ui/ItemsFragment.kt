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
import com.example.grocify.databinding.RecyclerFragmentBinding
import kotlinx.coroutines.launch

class ItemsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: RecyclerFragmentBinding? = null
    private val args: ItemsFragmentArgs by navArgs()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(args.category,null,true,false,true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productAdapter = ProductAdapter(viewModel) {
            //TODO: Add action and findNavController().navigate(action) Need productId
            val action = ItemsFragmentDirections.actionItemsFragmentToItemFragment(it)
            findNavController().navigate(action)
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = productAdapter

        lifecycleScope.launch {
            viewModel.getProducts(args.category)
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products != null) {
                productAdapter.submitList(products.products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}