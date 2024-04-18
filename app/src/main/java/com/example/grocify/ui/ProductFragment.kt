package com.example.grocify.ui

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
import kotlinx.coroutines.launch

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
            favoritesVisible = true,
            searchVisible = false,
            showBackButton = true
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        favoritesVisible = true,
                        searchVisible = false,
                        showBackButton = true
                    )

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
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


//class ProductImageSwiperAdapter(private val context: Context, private val imageUrls: List<String>) :
//    PagerAdapter() {
//
//    override fun getCount(): Int = imageUrls.size
//
//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val imageView = ImageView(context)
//        Glide.loadProductImage(imageUrls[position], imageView, 500, 500)
//        container.addView(imageView)
//        return imageView
//    }
//
//    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`
//
//    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        container.removeView(`object` as View)
//    }
//}
}