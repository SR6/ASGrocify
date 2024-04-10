package com.example.grocify.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.grocify.R
import com.example.grocify.databinding.CategoryFragmentBinding
import com.example.grocify.databinding.CategoryItemBinding
import com.example.grocify.db.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class CategoryFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: CategoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CategoryFragmentBinding.inflate(inflater, container, false)

        var userFirstName = FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.firstOrNull()

        if (userFirstName.isNullOrBlank())
            userFirstName = "User"

        if (userFirstName.length > 15)
            userFirstName = userFirstName.substring(0,15) + "..."

        viewModel.updateHeader(getString(R.string.grocify), "Hi, $userFirstName")

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCategories(
            onSuccess = { categories ->
                categories.forEach { category ->
                    val categoryItemBinding = CategoryItemBinding.inflate(layoutInflater, binding.categories, false)

                    viewModel.getCategoryImage(
                        imageFile = category.imageFile,
                        onSuccess = { file ->
                            Glide.loadCategoryImage(file, categoryItemBinding.categoryImage, 150, 150)
                        },
                        onFailure = {
                            categoryItemBinding.categoryImage.setImageResource(R.drawable.ic_invalid_image)
                        }
                    )

                    categoryItemBinding.categoryName.text = category.name

                    viewModel.categoryProductCounts.observe(viewLifecycleOwner) { categoryProductCounts ->
//                        if (categoryProductCounts[category.name] == null)
//                            categoryItemBinding.categoryCount.text = "loading..."
//                        else
                        categoryItemBinding.categoryCount.text = categoryProductCounts[category.name].toString() + " items"
                    }

                    categoryItemBinding.root.setOnClickListener {
                        findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToItemsFragment(category.name))
                    }

                    if (binding.categories.childCount > 0) {
                        val divider = View(requireContext())
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            3
                        )
                        params.setMargins(0, 10, 0, 10)
                        divider.layoutParams = params
                        divider.background = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
                        binding.categories.addView(divider)
                    }

                    binding.categories.addView(categoryItemBinding.root)
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }
}