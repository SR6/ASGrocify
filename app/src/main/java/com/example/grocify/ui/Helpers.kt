package com.example.grocify.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import com.example.grocify.R
import com.example.grocify.databinding.CartAndFavoritesBinding
import com.example.grocify.models.GrocifyProduct
import com.example.grocify.models.KrogerProduct
import com.example.grocify.models.UserProduct
import com.google.firebase.Timestamp
import java.util.UUID

class Helpers {
    class CartAndFavoritesHelpers(
        private val context: Context,
        private val viewLifecycleOwner: LifecycleOwner,
        private val binding: CartAndFavoritesBinding,
        private val viewModel: MainViewModel,
        private val product: KrogerProduct,
        private val adapter: ProductAdapter?,
        private val position: Int,
        private val isCartOrFavoritesFragment: Boolean,
        private val addToCartDisabled: Boolean
    ) {
        fun toggleFavorites() {
            viewModel.favoriteUserProducts.observe(viewLifecycleOwner) { favoriteUserProducts ->
                val isInFavorites = favoriteUserProducts?.any { it.productId == product.productId } ?: false
                val drawableId = if (!isInFavorites) R.drawable.ic_unfavorite else R.drawable.ic_favorite
                binding.toggleFavorites.setImageDrawable(ResourcesCompat.getDrawable(context.resources, drawableId, null))
            }

            binding.toggleFavorites.setOnClickListener {
                val isInFavorites = viewModel.favoriteUserProducts.value?.any { it.productId == product.productId } ?: false

                if (!isInFavorites) {
                    viewModel.addToFavorites(
                        UserProduct(viewModel.user.value!!.userId + product.productId,
                            viewModel.user.value!!.userId,
                            product.productId,
                            0,
                            Timestamp.now()
                        ),
                        onSuccess = {
                            binding.toggleFavorites.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_favorite, null))
                            if (adapter != null && isCartOrFavoritesFragment)
                                adapter.notifyItemChanged(position)

                            viewModel.getGrocifyProduct(product.productId,
                                onSuccess = { grocifyProduct ->
                                    if (grocifyProduct == null) {
                                        viewModel.addGrocifyProduct(
                                            GrocifyProduct(UUID.randomUUID().toString(),
                                                product.productId,
                                                0,
                                                1,
                                                0,
                                                Timestamp.now()
                                            ),
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                    else {
                                        viewModel.updateGrocifyProduct(
                                            GrocifyProduct(grocifyProduct.grocifyProductId,
                                                product.productId,
                                                grocifyProduct.cartCount,
                                                grocifyProduct.favoriteCount + 1,
                                                grocifyProduct.transactionCount,
                                                grocifyProduct.addedAt
                                            ),
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                },
                                onFailure = { }
                            )
                        },
                        onFailure = {
                            Toast.makeText(context, context.resources.getString(R.string.add_to_favorites_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                else {
                    viewModel.removeFromFavorites(
                        viewModel.user.value!!.userId + product.productId,
                        product.productId,
                        onSuccess = {
                            binding.toggleFavorites.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_unfavorite, null))
                            if (adapter != null && isCartOrFavoritesFragment)
                                adapter.notifyItemChanged(position)

                            viewModel.getGrocifyProduct(product.productId,
                                onSuccess = { grocifyProduct ->
                                    if (grocifyProduct!!.favoriteCount == 1 && grocifyProduct.cartCount == 0 && grocifyProduct.transactionCount == 0) {
                                        viewModel.removeGrocifyProduct(
                                            grocifyProduct.grocifyProductId,
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                    else {
                                        viewModel.updateGrocifyProduct(
                                            GrocifyProduct(grocifyProduct.grocifyProductId,
                                                product.productId,
                                                grocifyProduct.cartCount,
                                                grocifyProduct.favoriteCount - 1,
                                                grocifyProduct.transactionCount,
                                                grocifyProduct.addedAt
                                            ),
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                },
                                onFailure = { }
                            )
                        },
                        onFailure = {
                            Toast.makeText(context, context.resources.getString(R.string.remove_from_favorites_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        fun toggleCart() {
            viewModel.cartUserProducts.observe(viewLifecycleOwner) { cartUserProducts ->
                val userProduct = cartUserProducts?.find { it.productId == product.productId }

                if (userProduct != null) {
                    if (addToCartDisabled || userProduct.count >= 15)
                        binding.addToCart.apply {
                            isClickable = false
                            isEnabled = false
                            setColorFilter(context.resources.getColor(R.color.gray, null))
                        }
                    else
                        binding.addToCart.apply {
                            isClickable = true
                            isEnabled = true
                            setColorFilter(context.resources.getColor(R.color.black, null))
                        }

                    if (userProduct.count > 0) {
                        binding.removeFromCart.apply {
                            visibility = View.VISIBLE
                            isClickable = true
                            isEnabled = true
                            setColorFilter(context.resources.getColor(R.color.black, null))
                        }
                        binding.productCount.apply {
                            visibility = View.VISIBLE
                            text = userProduct.count.toString()
                        }
                    }
                }
                else {
                    binding.removeFromCart.apply {
                        visibility = View.GONE
                        isClickable = false
                        isEnabled = false
                    }

                    binding.productCount.visibility = View.GONE

                    if (addToCartDisabled) {
                        binding.addToCart.apply {
                            isClickable = false
                            isEnabled = false
                            setColorFilter(context.resources.getColor(R.color.gray, null))
                        }
                    }
                    else {
                        binding.addToCart.apply {
                            isClickable = true
                            isEnabled = true
                            setColorFilter(context.resources.getColor(R.color.black, null))
                        }
                    }
                }

                binding.addToCart.visibility = View.VISIBLE
            }

            binding.addToCart.setOnClickListener {
                val userProduct = viewModel.cartUserProducts.value?.find { it.productId == product.productId }

                if (userProduct == null) {
                    viewModel.addToCart(
                        UserProduct(viewModel.user.value!!.userId + product.productId,
                            viewModel.user.value!!.userId,
                            product.productId,
                            1,
                            Timestamp.now()
                        ),
                        onSuccess = {
                            if (adapter != null && isCartOrFavoritesFragment)
                                adapter.notifyItemChanged(position)

                            viewModel.getGrocifyProduct(product.productId,
                                onSuccess = { grocifyProduct ->
                                    if (grocifyProduct == null) {
                                        viewModel.addGrocifyProduct(
                                            GrocifyProduct(UUID.randomUUID().toString(),
                                                product.productId,
                                                1,
                                                0,
                                                0,
                                                Timestamp.now()
                                            ),
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                    else {
                                        viewModel.updateGrocifyProduct(
                                            GrocifyProduct(grocifyProduct.grocifyProductId,
                                                product.productId,
                                                grocifyProduct.cartCount + 1,
                                                grocifyProduct.favoriteCount,
                                                grocifyProduct.transactionCount,
                                                grocifyProduct.addedAt
                                            ),
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                },
                                onFailure = { }
                            )
                        },
                        onFailure = {
                            Toast.makeText(context, context.resources.getString(R.string.add_to_cart_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                else {
                    viewModel.updateCart(
                        UserProduct(viewModel.user.value!!.userId + product.productId,
                            viewModel.user.value!!.userId,
                            product.productId,
                            userProduct.count + 1,
                            userProduct.addedAt
                        ),
                        onSuccess = { },
                        onFailure = {
                            Toast.makeText(context, context.resources.getString(R.string.add_to_cart_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            binding.removeFromCart.setOnClickListener {
                val userProduct = viewModel.cartUserProducts.value?.find { it.productId == product.productId }

                if (userProduct!!.count == 1) {
                    viewModel.removeFromCart(
                        viewModel.user.value!!.userId + product.productId,
                        product.productId,
                        onSuccess = {
                            if (adapter != null && isCartOrFavoritesFragment)
                                adapter.notifyItemChanged(position)

                            viewModel.getGrocifyProduct(product.productId,
                                onSuccess = { grocifyProduct ->
                                    if (grocifyProduct!!.cartCount == 1 && grocifyProduct.favoriteCount == 0 && grocifyProduct.transactionCount == 0) {
                                        viewModel.removeGrocifyProduct(
                                            grocifyProduct.grocifyProductId,
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                    else {
                                        viewModel.updateGrocifyProduct(
                                            GrocifyProduct(grocifyProduct.grocifyProductId,
                                                product.productId,
                                                grocifyProduct.cartCount - 1,
                                                grocifyProduct.favoriteCount,
                                                grocifyProduct.transactionCount,
                                                grocifyProduct.addedAt
                                            ),
                                            onSuccess = { },
                                            onFailure = { }
                                        )
                                    }
                                },
                                onFailure = { }
                            )
                        },
                        onFailure = {
                            Toast.makeText(context, context.resources.getString(R.string.remove_from_cart_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                else {
                    viewModel.updateCart(
                        UserProduct(viewModel.user.value!!.userId + product.productId,
                            viewModel.user.value!!.userId,
                            product.productId,
                            userProduct.count - 1,
                            userProduct.addedAt
                        ),
                        onSuccess = { },
                        onFailure = {
                            Toast.makeText(context, context.resources.getString(R.string.add_to_cart_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    class ConfirmationDialogFragment(
        private val onConfirmListener: () -> Unit,
        private val message: String,
        private val positiveMessage: String,
        private val negativeMessage: String,
    ) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(message)
                    .setPositiveButton(positiveMessage) { _, _ ->
                        onConfirmListener.invoke()
                    }
                    .setNegativeButton(negativeMessage) { _, _ -> }
                builder.create()
            } ?: throw Exception()
        }
    }
}