package com.example.grocify.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.R
import com.example.grocify.databinding.CartFragmentBinding
import com.example.grocify.models.GrocifyProduct
import com.example.grocify.models.Transaction
import com.example.grocify.models.UserProduct
import com.google.firebase.Timestamp
import java.util.UUID

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

    @SuppressLint("NotifyDataSetChanged")
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
                binding.cartInformation.visibility = View.VISIBLE
                if (cartProducts.isEmpty()) {
                    productAdapter.submitList(emptyList())
                    binding.cartInformation.visibility = View.GONE
                    binding.recyclerFragment.noResultsFound.visibility = View.VISIBLE
                }
                else {
                    binding.recyclerFragment.noResultsFound.visibility = View.GONE
                    productAdapter.submitList(cartProducts)
                }
            }
            else {
                binding.recyclerFragment.loading.root.visibility = View.GONE
                productAdapter.submitList(emptyList())
                binding.cartInformation.visibility = View.GONE
                binding.recyclerFragment.noResultsFound.visibility = View.VISIBLE
            }

            var totalCartProducts = 0
            var totalAvailableCartProducts = 0
            var totalPrice = 0.0
            val transactionProducts = mutableMapOf<String, Int>()

            cartProducts?.forEach { product ->
                val cartUserProduct = viewModel.cartUserProducts.value?.find { it.productId == product.productId }
                totalCartProducts += cartUserProduct?.count ?: 0

                if (cartUserProduct != null && product.items[0].price != null) {
                    if (product.items.firstOrNull()?.inventory?.stockLevel != "TEMPORARILY_OUT_OF_STOCK") {
                        totalAvailableCartProducts += cartUserProduct.count

                        transactionProducts[product.productId] = cartUserProduct.count

                        product.items.firstOrNull()?.price?.let { price ->
                            totalPrice += if (price.promo != 0.0 && price.promo < price.regular) price.promo * cartUserProduct.count else price.regular * cartUserProduct.count
                        }
                    }
                }
            }

            viewModel.updateHeader(
                getString(R.string.cart),
                resources.getQuantityString(
                    R.plurals.items_quantity_header,
                    totalCartProducts,
                    viewModel.addCommasToNumber(totalCartProducts)
                )
            )

            binding.totalItems.text = String.format(
                "Total (%s %s)",
                resources.getQuantityString(
                    R.plurals.items_quantity,
                    totalAvailableCartProducts,
                    viewModel.addCommasToNumber(totalAvailableCartProducts)
                ),
                resources.getString(R.string.available)
            )

            binding.totalPrice.text = String.format("$%.2f", totalPrice)

            binding.zipCode.text = String.format("%s %s", resources.getString(R.string.store_at_zip_code), viewModel.user.value!!.zipCode)

            val isValid = viewModel.user.value!!.paymentMethod != ""

            binding.cardNumber.apply {
                text = viewModel.obfuscateCardNumber(
                    requireContext(),
                    viewModel.user.value!!.paymentMethod
                )
                setTextColor(context.resources.getColor(if (isValid) R.color.black else R.color.red, null))
            }

            binding.checkout.apply{
                isEnabled = isValid
                setTextColor(context.resources.getColor(if (isValid) R.color.black else R.color.gray, null))
            }

            binding.checkout.setOnClickListener {
                val confirmationDialog = Helpers.ConfirmationDialogFragment(
                    {
                        viewModel.addTransaction(
                            Transaction(UUID.randomUUID().toString(),
                                viewModel.user.value!!.userId,
                                totalAvailableCartProducts,
                                totalPrice,
                                Timestamp.now()
                            ),
                            onSuccess = {
                                transactionProducts.forEach { (productId, productCount) ->
                                    viewModel.removeFromCart(
                                        viewModel.user.value!!.userId + productId,
                                        productId,
                                        onSuccess = {
                                            viewModel.getGrocifyProduct(productId,
                                                onSuccess = { grocifyProduct ->
                                                    viewModel.updateGrocifyProduct(
                                                        GrocifyProduct(grocifyProduct!!.grocifyProductId,
                                                            productId,
                                                            grocifyProduct.cartCount - productCount,
                                                            grocifyProduct.favoriteCount,
                                                            grocifyProduct.transactionCount + 1,
                                                            grocifyProduct.addedAt
                                                        ),
                                                        onSuccess = { },
                                                        onFailure = { }
                                                    )
                                                },
                                                onFailure = { }
                                            )
                                        },
                                        onFailure = { }
                                    )
                                }
                                productAdapter.notifyDataSetChanged()
                            },
                            onFailure = {
                                Toast.makeText(context, resources.getString(R.string.transaction_failed), Toast.LENGTH_SHORT).show()
                            }
                        )
                        Toast.makeText(requireContext(), resources.getString(R.string.transaction_completed), Toast.LENGTH_SHORT).show()
                    },
                    resources.getString(R.string.purchase_message),
                    resources.getString(R.string.purchase),
                    resources.getString(R.string.cancel),
                )
                confirmationDialog.show(parentFragmentManager, resources.getString(R.string.purchase))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}