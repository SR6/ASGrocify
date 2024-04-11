package com.example.grocify.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.grocify.databinding.ItemFragmentBinding
import androidx.navigation.fragment.navArgs
import com.example.grocify.db.Glide
import com.example.grocify.db.GlideProductUrl
import kotlinx.coroutines.launch

class ItemFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: ItemFragmentBinding? = null
    private val args: ItemFragmentArgs by navArgs()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader("Item Details",null,true,false,true)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //viewModel.getProductById(args.productID)
        viewModel.viewModelScope.launch{
            viewModel.getProductById(args.productID)
        }

        //val product = viewModel.observeProduct()
        viewModel.product.observe(viewLifecycleOwner) { product ->
            //lifecycleScope.launch {
            //    viewModel.getProductById(args.productID)
            //}
            Log.d("ItemNav","In product observe is ${product.product}")
            if (product.product != null) {
                binding.UPC.text = product.product.upc
                binding.prodDescription.text = product.product.brand + product.product.description
                Glide.loadProductImage(product.product.images[0].sizes[0].url,binding.prodImage,150,150)
            }
        }
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}