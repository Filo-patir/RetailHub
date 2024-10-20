package com.iti4.retailhub.features.home

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.snackbar.Snackbar
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.MainActivityViewModel
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentHomeBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.favorits.viewmodel.FavoritesViewModel
import com.iti4.retailhub.features.home.adapter.AdsViewPagerAdapter
import com.iti4.retailhub.features.home.adapter.BrandAdapter
import com.iti4.retailhub.features.home.adapter.DotsIndicatorDecoration
import com.iti4.retailhub.features.home.adapter.NewItemAdapter
import com.iti4.retailhub.features.login_and_signup.view.CustomLoadingDialog
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.features.productdetails.viewmodel.ProductDetailsViewModel
import com.iti4.retailhub.features.shop.adapter.OnClickNavigate
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.HomeProducts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), OnClickGoToDetails , OnClickAddCopyCoupon, OnClickNavigate {
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel by viewModels<HomeViewModel>()
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val productDetailsViewModel by viewModels<ProductDetailsViewModel>()
    val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
lateinit var adapter: NewItemAdapter
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0
    private var currentPosition = 0
    private var autoScrollJob: Job? = null // Job for the coroutine
    private var networkState: Boolean = true
    private lateinit var adsAdapter: AdsViewPagerAdapter
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

           /* binding.guest.visibility=View.VISIBLE
            binding.messagef.text="login first to see your favorites"
            binding.btnOkayp.setOnClickListener {
                val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
                startActivity(intent)
            }
            binding.btnCancelp.setOnClickListener {
                Navigation.findNavController(view).navigate(R.id.homeFragment)
            }*/

        currencyCode = viewModel.getCurrencyCode()
        conversionRate = viewModel.getConversionRates(currencyCode)
        if (!userAuthViewModel.isguestMode()) {
            adapter = NewItemAdapter(this@HomeFragment,currencyCode,conversionRate,false)
            favoritesViewModel.getFavorites()
            getUserHomeProducts()
        }
        else {
            adapter = NewItemAdapter(this@HomeFragment,currencyCode,conversionRate,true)
            getGuestHomeProducts()
        }
        displayAds()
        brandsSetup()
        couponsSetup()
        // viewModel.getFavorites()
        // lifecycleScope.launch {
        //     viewModel.savedFavortes.collect { item ->
        //         when (item) {
        //             is ApiState.Success<*> -> {
        //                 val data = item.data as GetCustomerFavoritesQuery.Customer
        //                 val favoritList = data.metafields.nodes.filter { it.key == "favorites" }
        //                 getHomeProducts(favoritList)
        //             }
        //             is ApiState.Error -> {
        //                 Toast.makeText(
        //                     requireContext(),
        //                     item.exception.message,
        //                     Toast.LENGTH_SHORT
        //                 )
        //                     .show()
        //             }

        //             is ApiState.Loading -> {}
        //         }
        //     }
        // }

    }
//private fun getFavorites(){
//    favoritesViewModel.getFavorites()
//    lifecycleScope.launch {
//        favoritesViewModel.savedFavortes.collect { item ->
//            when (item) {
//                is ApiState.Success<*> -> {
//                    val data = item.data as GetCustomerFavoritesQuery.Customer
//                    val favoritList = data.metafields.nodes.filter { it.key == "favorites" }
//
//                    adapter.updateFavorites(favoritList)
//
//                }
//                is ApiState.Error -> {
//                    Toast.makeText(
//                        requireContext(),
//                        item.exception.message,
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//
//                is ApiState.Loading -> {}
//            }
//        }
//    }
//}
    private fun getUserHomeProducts() {
        val loadingDialog = CustomLoadingDialog(requireContext())
    GlobalScope.launch(Dispatchers.Main)  {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.products.combine(favoritesViewModel.savedFavortes){
                    products,favorites->
                    handleProductsAndFavoritesCombination(products,favorites) }.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
//                            binding.animationView.visibility = View.GONE
                            val data = item.data as List<HomeProducts>
                            loadingDialog.dismiss()
                            displayNewItemRowData(data)
                        }

                        is ApiState.Error -> {
                            if (item.exception.message == "Something went wrong"){
                                networkState = false
                            }
                            if (networkState){
                                Toast.makeText(
                                    requireContext(),
                                    item.exception.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            loadingDialog.dismiss()
                        }

                        is ApiState.Loading -> {
                            loadingDialog.show()
                        }
                    }
                }
            }
        }
    }

    private fun brandsSetup(){
        GlobalScope.launch(Dispatchers.Main)  {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.brands.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            val data = item.data as List<Brands>
                            displayBrandsRowData(data)
                        }
                        is ApiState.Error -> {
                            if (item.exception.message == "Something went wrong"){
                                networkState = false
                            }
                            if (networkState){
                                Toast.makeText(
                                    requireContext(),
                                    item.exception.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is ApiState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun couponsSetup(){
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.couponsState.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {
                        val data = item.data as List<Discount>
                        adsAdapter.setData(data)

                    }
                    is ApiState.Error -> {
                        if (item.exception.message == "Something went wrong"){
                            networkState = false
                        }
                        if (networkState){
                            Toast.makeText(
                                requireContext(),
                                item.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Loading -> {}
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

    private fun displayNewItemRowData(
        data: List<HomeProducts>
    ) {
        binding.newItemRow.apply {
            title.text = getString(R.string.new_item)
            subtitle.text = getString(R.string.you_ve_never_seen_it_before)
            recyclerView.adapter = adapter
            adapter.submitList(data)
        }
    }

    private fun displayBrandsRowData(data: List<Brands>) {
        binding.brandItemRow.apply {
            title.text = getString(R.string.brands)
            subtitle.text = getString(R.string.brands_subtitle)
            val adapter = BrandAdapter(this@HomeFragment)
            recyclerView.layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter
            adapter.submitList(data)
        }
    }


    private fun displayAds() {
        val manager = LinearLayoutManager(this.requireContext())
        manager.orientation = LinearLayoutManager.HORIZONTAL
        binding.vpHomeAds.layoutManager = manager
        adsAdapter = AdsViewPagerAdapter(listOf(), this@HomeFragment)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.vpHomeAds)
        binding.vpHomeAds.adapter = adsAdapter
        binding.vpHomeAds.addItemDecoration(
            DotsIndicatorDecoration(
                colorInactive = ContextCompat.getColor(this.requireContext(), R.color.red_color),
                colorActive = ContextCompat.getColor(this.requireContext(), R.color.red_color)
            )
        )
        startAutoScroll()
    }


    private fun startAutoScroll() {
        autoScrollJob = lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                delay(3000)
                currentPosition++
                if (currentPosition == adsAdapter.itemCount) {
                    currentPosition = 0
                }
                binding.vpHomeAds.smoothScrollToPosition(currentPosition)
            }
        }
    }


    override fun goToDetails(productId: String) {
        val bundle = Bundle()
        bundle.putString("productid", productId)
        findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment2, bundle)
    }

    override fun saveToFavorites(
        productId: String,
        productTitle: String,
        selectedImage: String,
        price: String
    ) {
        productDetailsViewModel.saveToFavorites(
            productId,productId,
            productTitle,selectedImage,price
        )
        Toast.makeText(requireContext(), "Added to your favorites", Toast.LENGTH_SHORT).show()
//        favoritesViewModel.getFavorites()
//        lifecycleScope.launch {
//            productDetailsViewModel.saveProductToFavortes.collect { item ->
//                when (item) {u
//                    is ApiState.Success<*> -> {
//                        val data =
//                            item.data as UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate
//                        Toast.makeText(
//                            requireContext(),
//                            "Add to your favorites",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                        Log.d("fav", "onViewCreated:${data} ")
//                        favoritesViewModel.getFavorites()
//                    }
//
//                    is ApiState.Error -> {
//                        Toast.makeText(
//                            requireContext(),
//                            item.exception.message,
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }
//
//                    is ApiState.Loading -> {}
//                }
//            }
//        }

    }

    override fun navigate(filter: String, productType: String) {
        findNavController().navigate(
            R.id.searchFragment,
            bundleOf("query" to filter, "type" to productType)
        )
    }
    override fun deleteFromCustomerFavorites(pinFavorite: String) {
        if (!userAuthViewModel.isguestMode()){
            favoritesViewModel.deleteFavorites(pinFavorite)
            Toast.makeText(requireContext(), "Deleted from Favorites", Toast.LENGTH_SHORT).show()
        }else{
            showGuestDialog()
        }
//        favoritesViewModel.getFavorites()
//        lifecycleScope.launch {
//            favoritesViewModel.deletedFavortes.collect { item ->
//
//                when (item) {
//
//                    is ApiState.Success<*> -> {
//                        Toast.makeText(
//                            requireContext(),
//                            "Product Is Deleted",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                        favoritesViewModel.getFavorites()
//                    }
//
//                    is ApiState.Error -> {
//                        Toast.makeText(
//                            requireContext(),
//                            item.exception.message,
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }
//
//                    is ApiState.Loading -> {}
//                }
//            }
//        }
    }
    override fun copyCoupon(coupon: Discount) {
        if(mainActivityViewModel.copiedCouponsList.none{ it.value==coupon.value}){
            mainActivityViewModel.copiedCouponsList.add(coupon)
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", coupon.title)
            clipboard.setPrimaryClip(clip)
            Snackbar.make(binding.root, "Discount Code Copied !", Snackbar.LENGTH_SHORT).show()
        }else{
            Snackbar.make(binding.root, "Code already in your clipboard !", Snackbar.LENGTH_SHORT).show()
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
    private fun getGuestHomeProducts(){
        val loadingDialog = CustomLoadingDialog(requireContext())
        GlobalScope.launch(Dispatchers.Main)  {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.products.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
//                            binding.animationView.visibility = View.GONE
                            val data = item.data as List<HomeProducts>
                            loadingDialog.dismiss()
                            displayNewItemRowData(data)
                        }
                        is ApiState.Error -> {
                            if (item.exception.message == "Something went wrong"){
                                networkState = false
                            }
                            if (networkState){
                                Toast.makeText(
                                    requireContext(),
                                    item.exception.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            loadingDialog.dismiss()
                        }
                        is ApiState.Loading -> {
                            loadingDialog.show()
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        autoScrollJob?.cancel()
    }
}