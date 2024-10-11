package com.iti4.retailhub.features.productSearch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentProducSearchBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.home.OnClickGoToDetails
import com.iti4.retailhub.models.HomeProducts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductSearchFragment : Fragment(), OnClickGoToDetails {
    private val viewModel: ProductSEarchViewModel by viewModels()
    private lateinit var binding: FragmentProducSearchBinding
    private var currentList = emptyList<HomeProducts>()
    //    private var isListView = true
    var isratingbarevisible=false
    private val productSearchListViewAdapter by lazy { ProductSearchListViewAdapter(this) }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProducSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("search", "onViewCreated:${arguments} ")
        if (arguments != null) {
            viewModel.searchProducts(arguments?.getString("query").toString())
        }
        setupRecycleViewWithAdapter()
        searchBarTextChangeListner()
        setupDataListener()
        sliderAndFilterListner()
//        onSwitchViewClicked()
    }

    private fun sliderAndFilterListner() {
        binding.filter.setOnClickListener{
            if(isratingbarevisible){
                binding.rangeSlider.visibility = View.GONE
                isratingbarevisible=false
            }else{
                binding.searchView.setQuery("",true)
                binding.rangeSlider.visibility = View.VISIBLE
                productSearchListViewAdapter.submitList(emptyList())
                isratingbarevisible=true
            }
        }
        binding.rangeSlider.addOnChangeListener { slider, minValue, maxValue ->
            Log.d("search", "sliderAndFilterListner:${minValue} ${maxValue} ")
            viewModel.searchProducts("price:>=${minValue}")
        }
    }

    private fun setupRecycleViewWithAdapter() {
        binding.searchRV.apply {
            adapter = productSearchListViewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        productSearchListViewAdapter.submitList(currentList)
//        binding.switchView.setImageResource(R.drawable.view_by_list)
//        isListView=!isListView
    }

    private fun searchBarTextChangeListner() {
        binding.searchView.setOnClickListener{
            binding.rangeSlider.visibility = View.GONE
            isratingbarevisible=false
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle the search query submission, if needed
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isEmpty()){
                        productSearchListViewAdapter.submitList(emptyList())
                    }else{
                        binding.rangeSlider.visibility = View.GONE
                        isratingbarevisible=false
                        Log.d("search", "onQueryTextChange: start")
                        //search by brand + product title
//                   viewModel.search("title:${"vans"}* *${newText}*")
                        //search by products name
                        viewModel.searchProducts("title:*${newText}*")
                    }

                }
                return true
            }
        })
    }

    private fun setupDataListener(){
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.searchList.collect{
                    when (it){
                        is ApiState.Success<*> -> {
                            Log.d("search", "setupDataListener:${it} ")
//                            currentList=it.data as List<HomeProducts>
//                            handleDataResult(currentList)
                            productSearchListViewAdapter.submitList(it.data as List<HomeProducts>)
                        }
                        is ApiState.Error -> {
                            Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_SHORT).show()
                        }
                        is ApiState.Loading -> {

                        }
                    }
                }
            }
        }
    }

    /*private fun handleDataResult(data: List<HomeProducts>){
        if (isListView) {
            Log.d("search", "handleDataResult: ${isListView} ${data}")
            gridViewAdapter.submitList(data)
        } else {
            listViewAdapter.submitList(data)

        }
    }*/

    /* private fun onSwitchViewClicked() {
         binding.switchView.setOnClickListener {
             if (isListView) {
                 binding.searchRV.apply {
                     adapter = listViewAdapter
                     layoutManager = GridLayoutManager(requireContext(), 2)
                 }
                 binding.switchView.setImageResource(R.drawable.view_by_grid)
                 gridViewAdapter.submitList(currentList)
             } else {
                 binding.searchRV.apply {
                     adapter = listViewAdapter
                     layoutManager = LinearLayoutManager(requireContext())
                 }
                 binding.switchView.setImageResource(R.drawable.view_by_list)
                 listViewAdapter.submitList(currentList)

             }
             isListView = !isListView
         }
     }*/

    override fun goToDetails(productId: String) {
        findNavController().navigate(R.id.productDetailsFragment, bundleOf("productid" to productId))
    }

    override fun saveToFavorites(
        variantID: String,
        productId: String,
        selectedProductColor: String,
        selectedProductSize: String,
        productTitle: String,
        selectedImage: String,
        price: String
    ) {

    }

}