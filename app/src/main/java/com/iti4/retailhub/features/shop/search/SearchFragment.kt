package com.iti4.retailhub.features.shop.search

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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.R
import com.iti4.retailhub.constants.SortBy
import com.iti4.retailhub.databinding.FragmentSearchBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.favorits.viewmodel.FavoritesViewModel
import com.iti4.retailhub.features.home.OnClickGoToDetails
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.features.productdetails.viewmodel.ProductDetailsViewModel
import com.iti4.retailhub.features.shop.search.adapter.GridViewAdapter
import com.iti4.retailhub.features.shop.search.adapter.ListViewAdapter
import com.iti4.retailhub.features.shop.search.sortby.SortByBottomSheetFragment
import com.iti4.retailhub.features.shop.search.sortby.SortByListener
import com.iti4.retailhub.features.shop.search.viewmodels.SearchViewModel
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.HomeProducts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class SearchFragment : Fragment(), OnClickGoToDetails, SortByListener {
    private val viewModel: SearchViewModel by viewModels()
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val productDetailsViewModel by viewModels<ProductDetailsViewModel>()
    private val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0
    private lateinit var binding: FragmentSearchBinding
    private var isListView = true
    private lateinit var listViewAdapter: ListViewAdapter
    private lateinit var gridViewAdapter: GridViewAdapter
    private var filterQuery: String = ""
    private var query: String = ""
    private var typeQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currencyCode = viewModel.getCurrencyCode()
        conversionRate = viewModel.getConversionRates(currencyCode)
        onSwitchViewClicked()
        filterQuery = arguments?.getString("query") ?: ""
        typeQuery = arguments?.getString("type") ?: ""
        binding.filterGroup.setOnClickListener { findNavController().navigate(R.id.filterFragment) }
        if (!userAuthViewModel.isguestMode()) {
            listViewAdapter = ListViewAdapter(this, currencyCode, conversionRate, false)
            gridViewAdapter = GridViewAdapter(this, currencyCode, conversionRate, false)
            favoritesViewModel.getFavorites()
            setupUserDataListener()
        }
        else {
            listViewAdapter = ListViewAdapter(this, currencyCode, conversionRate,true)
            gridViewAdapter = GridViewAdapter(this, currencyCode, conversionRate,true)
            setupGuestDataListener()
        }
        setupChipGroup()
        if (filterQuery.isNotEmpty()){
            search()
        }
        setupAppbar()
        clearFilters()
    }

    private fun setupUserDataListener() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchList.combine(favoritesViewModel.savedFavortes){
                        products,favorites->
                    handleProductsAndFavoritesCombination(products,favorites) }.collect {
                    when (it) {
                        is ApiState.Success<*> -> {
                            handleDataResult(it.data as List<HomeProducts>)
                        }

                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is ApiState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun setupGuestDataListener() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchList.collect {
                    when (it) {
                        is ApiState.Success<*> -> {
                            handleDataResult(it.data as List<HomeProducts>)
                        }
                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is ApiState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun handleDataResult(data: List<HomeProducts>) {
        Log.d("Filo", "handleDataResult: ${data.size}")
        if (isListView) {
            binding.searchRV.apply {
                adapter = listViewAdapter
                layoutManager = LinearLayoutManager(requireContext())
                listViewAdapter.submitList(data)
                smoothScrollToPosition(0)
            }
        } else {
            binding.searchRV.apply {
                adapter = gridViewAdapter
                layoutManager = GridLayoutManager(requireContext(), 2)
                gridViewAdapter.submitList(data)
                smoothScrollToPosition(0)
            }
        }
    }

    private fun onSwitchViewClicked() {
        binding.switchView.setOnClickListener {
            isListView = !isListView
            if (isListView) {
                binding.searchRV.apply {
                    adapter = listViewAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    listViewAdapter.submitList(gridViewAdapter.currentList)
                }
                binding.switchView.setImageResource(R.drawable.view_by_grid)
            } else {
                binding.searchRV.apply {
                    adapter = gridViewAdapter
                    layoutManager = GridLayoutManager(requireContext(), 2)
                    gridViewAdapter.submitList(listViewAdapter.currentList)
                }
                binding.switchView.setImageResource(R.drawable.view_by_list)
            }
        }
    }

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
        } else {
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
        } else {
            favoritesViewModel.deleteFavorites(pinFavorite)
            Toast.makeText(requireContext(), "Deleted from Favorites", Toast.LENGTH_SHORT).show()
        }
    }


    private fun search(){
        val finalQuery = StringBuilder().append(query).append(" $filterQuery").append(" AND $typeQuery").toString()
        Log.d("Filo", "search: $finalQuery")
        viewModel.searchProducts(finalQuery)
    }

    private fun setupAppbar() {
        binding.apply {
            appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val totalScrollRange = appBarLayout.totalScrollRange
                if (abs(verticalOffset.toDouble()) >= totalScrollRange) {
                    // Collapsed
                    toolbar.background = ResourcesCompat.getDrawable(resources, R.color.white, null)
                    pageName.visibility = View.GONE
                    collapsedPageName.visibility = View.VISIBLE
                } else {
                    // Expanded
                    toolbar.background = ResourcesCompat.getDrawable(resources, R.color.white, null)
                    pageName.visibility = View.VISIBLE
                    collapsedPageName.visibility = View.GONE
                }
            }
            backButton.setOnClickListener { findNavController().navigateUp() }
            searchBtn.setOnClickListener {
                findNavController().navigate(R.id.producSearchFragment)
            }
            sortGroup.setOnClickListener {
                SortByBottomSheetFragment(this@SearchFragment).show(childFragmentManager, "sort")
            }
        }
    }


    private fun setupChipGroup(){
        when (typeQuery.lowercase(Locale.ROOT)){
            "shoes" -> binding.Shoes.isChecked = true
            "t-shirts" -> binding.Tops.isChecked = true
            "accessories" -> binding.accsChip.isChecked = true
            else -> binding.allChip.isChecked = true
        }
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            typeQuery = ""
            Log.d("Filo", "setupChipGroup: ${checkedIds.size}")
            if(checkedIds.isNotEmpty() && checkedIds.size!=3) {
                typeQuery = view?.findViewById<Chip>(checkedIds[0])?.text.toString()
                checkedIds.removeFirst()
                checkedIds.map { id -> typeQuery += " OR " + view?.findViewById<Chip>(id)?.text.toString() }
                binding.allChip.isChecked = false
            }
            else  {
                typeQuery = ""
                binding.apply{
                    accsChip.isChecked = false
                    Tops.isChecked = false
                    Shoes.isChecked = false
                    allChip.isChecked = true
                }
                binding.chipGroup.clearCheck()
            }
            search()
        }
        binding.allChip.setOnCheckedChangeListener{
            _, isChecked ->
                if (isChecked) {
                    binding.apply{
                        accsChip.isChecked = false
                        Tops.isChecked = false
                        Shoes.isChecked = false
                    }
                    typeQuery = ""
                    search()
                }
            else{
                if (binding.chipGroup.checkedChipIds.isEmpty()){
                    binding.allChip.isChecked = true
                }
            }
        }
    }

    private fun clearFilters() {
        binding.imageView8.setOnClickListener {
            binding.imageView8.visibility = View.GONE
            filterQuery = ""
            search()
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

    override fun onSortBySelected(sortBy: SortBy) {
        Log.d("Filo", "onSortBySelected: $sortBy")
        when(sortBy){
            SortBy.PRICE_ASC -> {
                binding.textView15.text = getString(R.string.price_low_to_high)
                if (isListView){
                    listViewAdapter.submitList(listViewAdapter.currentList.sortedBy { it.maxPrice.toDouble() })
                } else {
                    gridViewAdapter.submitList(gridViewAdapter.currentList.sortedBy { it.maxPrice.toDouble() })
                }
            }
            SortBy.PRICE_DESC -> {
                binding.textView15.text = getString(R.string.price_high_to_low)
                if (isListView){
                    listViewAdapter.submitList(listViewAdapter.currentList.sortedByDescending { it.maxPrice.toDouble() })
                } else {
                    gridViewAdapter.submitList(gridViewAdapter.currentList.sortedByDescending { it.maxPrice.toDouble() })
                }
            }
            SortBy.RELEVANCE -> {
                binding.textView15.text = getString(R.string.sort_by_relevance)
                if (isListView){
                    listViewAdapter.submitList(listViewAdapter.currentList.sortedBy { it.title?.split(" | ")?.get(1) })
                    Log.d("Filo", "onSortBySelected: ${listViewAdapter.currentList}")
                } else {
                    gridViewAdapter.submitList(gridViewAdapter.currentList.sortedBy { it.title?.split("|")?.get(1) })
                }
            }
        }
        binding.searchRV.smoothScrollToPosition(0)
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