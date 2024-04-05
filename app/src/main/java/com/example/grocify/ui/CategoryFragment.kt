package com.example.grocify.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.grocify.R
import com.example.grocify.databinding.CategoryFragmentBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: CategoryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CategoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
"""
        lifecycleScope.launch {
            viewModel.fetchProducts()
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products != null) {
                binding.productName.text = products.products[0].brand
            }
        }
        """
        val container = view.findViewById<LinearLayout>(R.id.catContainer)
        val itemTextViewTemplate = view.findViewById<TextView>(R.id.itemTextView)
        val categories = viewModel.getCategories()
        categories.forEachIndexed { index, item ->
            val textView = TextView(requireContext())
            textView.layoutParams = itemTextViewTemplate.layoutParams
            textView.setPadding (
                itemTextViewTemplate.paddingBottom,
                itemTextViewTemplate.paddingTop,
                itemTextViewTemplate.paddingLeft,
                itemTextViewTemplate.paddingRight
            )
            textView.setTextColor(itemTextViewTemplate.textColors)
            textView.textSize = itemTextViewTemplate.textSize
            textView.setOnClickListener{
                onItemClick(item)
            }
            textView.text = item
            container.addView(textView)
        }

    }
    fun onItemClick(item:String) {
        // Handle click event, for example, navigate to a new list Chat example need to refine
        //val intent = Intent(requireContext(), NewListActivity::class.java)
        //intent.putExtra("clickedItem", item)
        //startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}