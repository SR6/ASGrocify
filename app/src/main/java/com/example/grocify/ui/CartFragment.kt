package com.example.grocify.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.CartFragmentBinding
import com.example.grocify.models.KrogerProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: CartFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CartFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(getString(R.string.cart), null)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel, true)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(CartFragmentDirections.actionCartFragmentToProductFragment(productId, brand))
        }

        binding.recyclerFragment.recycler.adapter = productAdapter
        binding.recyclerFragment.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerFragment.loading.root.visibility = View.VISIBLE

        viewModel.cartProducts.observe(viewLifecycleOwner) { cartProducts ->
            if (cartProducts != null) {
                binding.recyclerFragment.loading.root.visibility = View.GONE
                if (cartProducts.isEmpty()) {
                    productAdapter.submitList(emptyList())
                    binding.recyclerFragment.noProductsFound.visibility = View.VISIBLE
                }
                else {
                    binding.recyclerFragment.noProductsFound.visibility = View.GONE
                    productAdapter.submitList(cartProducts)
                }
            }
            else {
                binding.recyclerFragment.loading.root.visibility = View.GONE
                productAdapter.submitList(emptyList())
                binding.recyclerFragment.noProductsFound.visibility = View.VISIBLE
            }

            viewModel.updateHeader(
                getString(R.string.cart),
                resources.getQuantityString(
                    R.plurals.items_quantity_header,
                    cartProducts?.size ?: 0,
                    viewModel.addCommasToNumber(cartProducts?.size ?: 0)
                )
            )

            var totalPrice = 0.0
            var totalProductsAvailableToPurchase = 0
            cartProducts?.forEach { product ->
                if (product.items[0].price != null) {
                    if (product.items.firstOrNull()?.inventory?.stockLevel != "TEMPORARILY_OUT_OF_STOCK") {
                        totalProductsAvailableToPurchase++
                        product.items.firstOrNull()?.price?.let { price ->
                            totalPrice += if (price.promo != 0.0 && price.promo < price.regular) price.promo else price.regular
                        }
                    }
                }
            }
            binding.totalItems.text = String.format(
                "Total (%s) %s",
                resources.getQuantityString(
                    R.plurals.items_quantity,
                    totalProductsAvailableToPurchase,
                    viewModel.addCommasToNumber(totalProductsAvailableToPurchase)
                ),
                resources.getString(R.string.available)
            )
            binding.totalPrice.text = String.format("$%.2f", totalPrice)
            binding.cardNumber.text = viewModel.obfuscateCardNumber(
                requireContext(),
                viewModel.user.value!!.paymentMethod
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}