package com.example.grocify.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.grocify.databinding.ProductFragmentBinding
import androidx.navigation.fragment.navArgs
import com.denzcoskun.imageslider.models.SlideModel
import com.example.grocify.R
import kotlinx.coroutines.launch
import kotlin.math.round

class ProductFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private val args: ProductFragmentArgs by navArgs()

    private var _binding: ProductFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProductFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader(
            args.productBrand,
            null,
            showBackButton = true
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.grocifyProductListener(args.productId,
            onSuccess = { grocifyProduct ->
                binding.cartCount.visibility = View.GONE
                binding.transactionCount.visibility = View.GONE
                binding.favoriteCount.visibility = View.GONE

                if (grocifyProduct != null) {
                    if (grocifyProduct.cartCount > 0) {
                        binding.cartCount.apply {
                            visibility = View.VISIBLE
                            text = String.format("%s %s.",
                                resources.getString(R.string.currently_in),
                                resources.getQuantityString(
                                    R.plurals.carts_quantity,
                                    grocifyProduct.cartCount,
                                    viewModel.addCommasToNumber(grocifyProduct.cartCount)
                                )
                            )
                        }
                    }

                    if (grocifyProduct.transactionCount > 0) {
                        binding.transactionCount.apply {
                            visibility = View.VISIBLE
                            text = String.format("%s %s.",
                                resources.getString(R.string.bought_by),
                                resources.getQuantityString(
                                    R.plurals.people_quantity,
                                    grocifyProduct.transactionCount,
                                    viewModel.addCommasToNumber(grocifyProduct.transactionCount)
                                )
                            )
                        }
                    }

                    if (grocifyProduct.favoriteCount > 0) {
                        binding.favoriteCount.apply {
                            visibility = View.VISIBLE
                            text = String.format("%s %s.",
                                resources.getString(R.string.favorited_by),
                                resources.getQuantityString(
                                    R.plurals.people_quantity,
                                    grocifyProduct.favoriteCount,
                                    viewModel.addCommasToNumber(grocifyProduct.favoriteCount)
                                )
                            )
                        }
                    }
                }
            },
            onFailure = { }
        )

        lifecycleScope.launch {
            viewModel.setIsApiRequestCompleted(false)
            val product = viewModel.getProductById(args.productId)
            viewModel.isApiRequestCompleted.observe(viewLifecycleOwner) { isCompleted ->
                if (isCompleted) {
                    var productBrand = args.productBrand
                    if (productBrand.length > 20)
                        productBrand = productBrand.substring(0, 20) + "..."

                    viewModel.updateHeader(
                        productBrand,
                        null,
                        showBackButton = true
                    )

                    binding.productDescription.text = product?.product?.description

                    val productImageUrls = ArrayList<SlideModel>()

                    product?.product?.images?.forEach { image ->
                        image.sizes.filter { size ->
                            size.size == "xlarge"
                        }.map { size ->
                            val imageUrl = size.url
                            val slideModel = SlideModel(imageUrl)
                            productImageUrls.add(slideModel)
                        }
                    }

                    binding.productImages.setImageList(productImageUrls)

                    val addToCartDisabled: Boolean
                    val price = product?.product?.items?.getOrNull(0)?.price

                    if (price != null) {
                        if (price.promo != 0.0 && price.promo < price.regular) {
                            binding.productPrice.text = String.format("$%.2f", price.promo)
                            binding.productOldPrice.apply {
                                text = String.format("$%.2f", price.regular)
                                visibility = View.VISIBLE
                                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            }
                            binding.onSale.visibility = View.VISIBLE
                        }
                        else {
                            binding.productPrice.text = String.format("$%.2f", price.regular)
                            binding.productOldPrice.visibility = View.GONE
                            binding.onSale.visibility = View.GONE
                        }

                        val inventory = product.product.items.getOrNull(0)?.inventory
                        addToCartDisabled = inventory?.stockLevel == "TEMPORARILY_OUT_OF_STOCK"
                    }
                    else {
                        binding.productPrice.apply {
                            text = context.resources.getString(R.string.unavailable_price_)
                            setTextColor(context.resources.getColor(R.color.gray, null))
                        }
                        binding.onSale.visibility = View.GONE
                        addToCartDisabled = true
                    }

                    val size = product?.product?.items?.getOrNull(0)?.size

                    if (size != null){
                        binding.size.text = String.format("(%s)", size)
                        binding.size.visibility = View.VISIBLE
                    }
                    else
                        binding.size.visibility = View.GONE

                    if (product?.product != null) {
                        val cartAndFavoritesHelpers = Helpers.CartAndFavoritesHelpers(
                            requireContext(),
                            viewLifecycleOwner,
                            binding.cartAndFavorites,
                            viewModel,
                            product.product,
                            null,
                            0,
                            false,
                            addToCartDisabled
                        )

                        cartAndFavoritesHelpers.toggleFavorites()
                        cartAndFavoritesHelpers.toggleCart()
                    }

                    val brand = product?.product?.brand

                    if (brand != null){
                        binding.brand.text = brand
                        binding.brand.visibility = View.VISIBLE
                    }
                    else
                        binding.brand.visibility = View.GONE

                    val category = product?.product?.categories?.getOrNull(0)

                    if (category != null){
                        binding.category.text = category
                        binding.category.visibility = View.VISIBLE
                    }
                    else
                        binding.category.visibility = View.GONE

                    val inventory = product?.product?.items?.getOrNull(0)?.inventory

                    binding.inventoryLevel.apply {
                        text = when (inventory?.stockLevel) {
                            "TEMPORARILY_OUT_OF_STOCK" -> {
                                setTextColor(resources.getColor(R.color.red, null))
                                resources.getString(R.string.out_of_stock)
                            }
                            "LOW" -> {
                                setTextColor(resources.getColor(R.color.orange, null))
                                resources.getString(R.string.low)
                            }
                            "HIGH" -> {
                                setTextColor(resources.getColor(R.color.green, null))
                                resources.getString(R.string.high)
                            }
                            else -> {
                                setTextColor(resources.getColor(R.color.black, null))
                                inventory?.stockLevel ?: resources.getString(R.string.unavailable)
                            }
                        }
                    }

                    val dimensions = product?.product?.itemInformation

                    if (dimensions?.depth != null && dimensions.height != null && dimensions.width != null)
                        binding.dimensions.text = String.format("%s\" x %s\" x %s\"", dimensions.depth, dimensions.width, dimensions.height)
                    else
                        binding.dimensions.text = resources.getString(R.string.unavailable)

                    val temperature = product?.product?.temperature?.indicator

                    if (temperature != null)
                        binding.temperature.text = temperature
                    else
                        binding.temperature.text =resources.getString(R.string.unavailable)

                    val countryOrigin = product?.product?.countryOrigin

                    if (countryOrigin != null) {
                        binding.countryOrigin.text = countryOrigin
                    }
                    else
                        binding.countryOrigin.text = resources.getString(R.string.unavailable)

                    val upc = product?.product?.upc

                    if (upc != null){
                        binding.upc.text = upc
                    }
                    else
                        binding.upc.text = resources.getString(R.string.unavailable)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
