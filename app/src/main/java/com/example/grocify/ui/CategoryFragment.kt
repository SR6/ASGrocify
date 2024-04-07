package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.grocify.R
import com.example.grocify.databinding.CategoryFragmentBinding
import com.google.firebase.auth.FirebaseAuth
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

        var userFirstName = FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.firstOrNull()

        if (userFirstName.isNullOrBlank())
            userFirstName = "User"

        if (userFirstName.length > 15)
            userFirstName = userFirstName.substring(0,15) + "..."

        viewModel.updateHeader(getString(R.string.grocify), "Hi, $userFirstName")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.catContainer)
        val itemTextViewTemplate = view.findViewById<TextView>(R.id.itemTextView)
        val categories = viewModel.getCategories()
        categories.forEachIndexed { _, item ->
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
        // Handle click event, set search string for fetchProducts and navigate to itemsFragment
        val action = CategoryFragmentDirections.actionCategoryFragmentToItemsFragment(item)
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}