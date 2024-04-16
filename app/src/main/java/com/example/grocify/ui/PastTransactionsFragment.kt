package com.example.grocify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocify.databinding.RecyclerFragmentBinding
import kotlinx.coroutines.launch
import kotlin.random.Random

class PastTransactionsFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: RecyclerFragmentBinding? = null
    private val binding get() = _binding!!

    data class PurchaseItem(
        val itemCount: Int,
        val totalCost: Double,
        val dateOfPurchase: String
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerFragmentBinding.inflate(inflater, container, false)

        viewModel.updateHeader("Past Purchases",null,false,false,true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pastPurchaseAdapter = PastPurchaseAdapter(viewModel, setupDummyList() )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = pastPurchaseAdapter

        //TODO: Fetch past purchase list from database


        //if (products != null) {
            //pastPurchaseAdapter.submitList()
        //}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setupDummyList(): List<PurchaseItem>{
        val purchases =  (mutableListOf<PurchaseItem> ())
        var dummyNum = 40
        repeat(10){
            val itemCount = dummyNum
            val totPrice = dummyNum * 2.0
            val date = "3 mar 2024"
            val purchaseItem = PurchaseItem(itemCount,totPrice,date)
            dummyNum += 1
            purchases.add(purchaseItem)
        }
        return purchases
    }
}