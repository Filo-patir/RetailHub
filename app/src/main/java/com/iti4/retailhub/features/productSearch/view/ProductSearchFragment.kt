package com.iti4.retailhub.features.productSearch.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentProducSearchBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.favorits.viewmodel.FavoritesViewModel
import com.iti4.retailhub.features.home.OnClickGoToDetails
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.features.productSearch.viewmodel.ProductSearchViewModel
import com.iti4.retailhub.features.productdetails.viewmodel.ProductDetailsViewModel
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.HomeProducts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductSearchFragment : Fragment(), OnClickGoToDetails {
    private val viewModel: ProductSearchViewModel by viewModels()
    private lateinit var binding: FragmentProducSearchBinding
    private var currentList = emptyList<HomeProducts>()
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val productDetailsViewModel by viewModels<ProductDetailsViewModel>()
    private val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0


    //    private var isListView = true
    var isratingbarevisible = false
    lateinit var productSearchListViewAdapter : ProductSearchListViewAdapter

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
        currencyCode = viewModel.getCurrencyCode()
        conversionRate = viewModel.getConversionRates(currencyCode)
        if (!userAuthViewModel.isguestMode()) {
            productSearchListViewAdapter = ProductSearchListViewAdapter(this,currencyCode,conversionRate,false)
            favoritesViewModel.getFavorites()
            setupUserDataListener()
        }
        else {
            productSearchListViewAdapter = ProductSearchListViewAdapter(this,currencyCode,conversionRate,true)
            setupGuestDataListener()
        }
        if (arguments != null) {
            viewModel.searchProducts(arguments?.getString("query").toString())
        }
        setupRecycleViewWithAdapter()
        searchBarTextChangeListner()
        sliderAndFilterListner()
//        onSwitchViewClicked()
    }

    private fun sliderAndFilterListner() {
        binding.filter.setOnClickListener {
            if (isratingbarevisible) {
                binding.rangeSlider.visibility = View.GONE
                binding.filter.setImageResource(R.drawable.baseline_filter_list_24)
                isratingbarevisible=false
            }else{
                binding.searchView.setQuery("",true)
                binding.rangeSlider.visibility = View.VISIBLE
                productSearchListViewAdapter.submitList(emptyList())
                binding.filter.setImageResource(R.drawable.filter_list_off)
                isratingbarevisible=true
            }
        }
        binding.rangeSlider2.addOnChangeListener { _, minValue, maxValue ->
            Log.d("search", "sliderAndFilterListner:${minValue} ${maxValue} ")
//            binding.maxtool.text=maxValue.toString()
            val formattedNumber = java.lang.String.format("%.1f", minValue)
            binding.maxtool.text="Max price:${formattedNumber}"
            viewModel.searchProducts("price:<=${minValue}")
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
        binding.searchView.setOnClickListener {
            binding.rangeSlider.visibility = View.GONE
            binding.filter.setImageResource(R.drawable.filter_list_off)
            isratingbarevisible=false
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle the search query submission, if needed
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isEmpty()) {
                        productSearchListViewAdapter.submitList(emptyList())
                    } else {
                        binding.rangeSlider.visibility = View.GONE
                        isratingbarevisible = false
                        Log.d("search", "onQueryTextChange: start")
                        //search by brand + product title
//                   viewModel.search("title:${"vans"}* *${newText}*")
                        //search by products name
                        viewModel.searchProducts("${newText}")
                    }

                }
                return true
            }
        })
    }

    private fun setupUserDataListener() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchList.combine(favoritesViewModel.savedFavortes) { products, favorites ->
                    handleProductsAndFavoritesCombination(products, favorites)
                }.collect {
                    when (it) {
                        is ApiState.Success<*> -> {
                            Log.d("search", "setupDataListener:${it} ")
//                            currentList=it.data as List<HomeProducts>
//                            handleDataResult(currentList)
                            productSearchListViewAdapter.submitList(it.data as List<HomeProducts>)
                        }

                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiState.Loading -> {

                        }
                    }
                }
            }
        }
    }

    private fun setupGuestDataListener(){
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchList.collect {
                    when (it) {
                        is ApiState.Success<*> -> {
                            Log.d("search", "setupDataListener:${it} ")
                            productSearchListViewAdapter.submitList(it.data as List<HomeProducts>)
                        }
                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is ApiState.Loading -> {
                        }
                    }
                }
            }
        }
    }

    private fun handleProductsAndFavoritesCombination(products: ApiState, favorites: ApiState): ApiState{
        if (products is ApiState.Success<*> && favorites is ApiState.Success<*>){
            val productsList = products.data as List<HomeProducts>
            val data = favorites.data as GetCustomerFavoritesQuery.Customer
            val favoritesList = data.metafields.nodes.filter { it.key == "favorites" }
            val combinedList = productsList.map { product ->
                favoritesList.forEach {
                        node -> if(node.key == "favorites" && node.value == product.id){
                    product.favID = node.id
                    product.isFav = true
                }
                }
                return@map product
            }
            return ApiState.Success(combinedList)
        }
        else if (products is ApiState.Loading || favorites is ApiState.Loading){
            return ApiState.Loading
        }
        else
            return if (products is ApiState.Error) products else favorites
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
        findNavController().navigate(
            R.id.productDetailsFragment,
            bundleOf("productid" to productId)
        )
    }

    override fun saveToFavorites(
        productId: String,
        productTitle: String,
        selectedImage: String,
        price: String
    ) {
        if (userAuthViewModel.isguestMode()){
            showGuestDialog()
        }
        else {
            productDetailsViewModel.saveToFavorites(
                productId, productId,
                productTitle, selectedImage, price
            )
            Toast.makeText(requireContext(), "Added to your favorites", Toast.LENGTH_SHORT).show()
        }
    }

    override fun deleteFromCustomerFavorites(pinFavorite: String) {
        if (userAuthViewModel.isguestMode()){
            showGuestDialog()
        }
        else {
            favoritesViewModel.deleteFavorites(pinFavorite)
            Toast.makeText(requireContext(), "Deleted from Favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGuestDialog(){
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.guest_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnYes: Button = dialog.findViewById(R.id.btn_okayd)
        val btnNo: Button = dialog.findViewById(R.id.btn_canceld)
        val messag=dialog.findViewById<TextView>(R.id.messaged)
        messag.text="login to add to your favorites"
        btnYes.setOnClickListener {
            val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
            intent.putExtra("guest","guest")
            startActivity(intent)
            requireActivity().finish()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}