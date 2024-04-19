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
import kotlinx.coroutines.launch

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

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(requireContext(), viewLifecycleOwner, viewModel)
        productAdapter.onItemClicked = { productId, brand ->
            findNavController().navigate(CartFragmentDirections.actionCartFragmentToProductFragment(productId, brand))
        }

        binding.recyclerFragment.recycler.adapter = productAdapter
        binding.recyclerFragment.recycler.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerFragment.loading.root.visibility = View.VISIBLE


        viewModel.cartProducts.observe(viewLifecycleOwner) { cartProducts ->
            val products = mutableListOf<KrogerProduct>()
            products.clear()
            lifecycleScope.launch {
                if (cartProducts != null) {
                    cartProducts.forEach { cartProduct ->
                        if (products.none { it.productId == cartProduct.productId }) {
                            val product = viewModel.getProductById(cartProduct.productId)
                            product?.product?.let { products.add(it) }
                        }
                    }
                    binding.recyclerFragment.loading.root.visibility = View.GONE
                    if (products.isEmpty())
                        displayNoProductsFound()
                    else {
                        binding.recyclerFragment.noProductsFound.visibility = View.GONE
                        productAdapter.submitList(products)
                    }
                }
                else {
                    binding.recyclerFragment.loading.root.visibility = View.GONE
                    displayNoProductsFound()
                }

                viewModel.updateHeader(
                    getString(R.string.cart),
                    resources.getQuantityString(
                        R.plurals.items_quantity_header,
                        products.size,
                        viewModel.addCommasToNumber(products.size)
                    )
                )

                var totalPrice = 0.0
                var totalProductsAvailableToPurchase = 0
                products.forEach { product ->
                    if (product.items[0].price != null && product.items[0].inventory != null && product.items[0].inventory!!.stockLevel != "TEMPORARILY_OUT_OF_STOCK") {
                        totalPrice += if (product.items[0].price!!.promo != 0.0 && product.items[0].price!!.promo < product.items[0].price!!.regular)
                            product.items[0].price!!.promo
                        else
                            product.items[0].price!!.regular
                        totalProductsAvailableToPurchase++
                    }
                }
                binding.totalItems.text = String.format(
                    "Total (%s)",
                    resources.getQuantityString(
                        R.plurals.items_quantity,
                        totalProductsAvailableToPurchase,
                        viewModel.addCommasToNumber(totalProductsAvailableToPurchase)
                    )
                )
                binding.totalPrice.text = String.format("$%.2f", totalPrice)
                binding.cardNumber.text = viewModel.obfuscateCardNumber(requireContext(), viewModel.user.value!!.paymentMethod)
            }
        }
    }

    private fun displayNoProductsFound() {
        viewModel.clearProducts()
        productAdapter.submitList(emptyList())
        binding.recyclerFragment.noProductsFound.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        productAdapter.submitList(emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}