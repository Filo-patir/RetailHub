package com.iti4.retailhub.features.home

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.R
import com.iti4.retailhub.UpdateCustomerFavoritesMetafieldsMutation
import com.iti4.retailhub.databinding.FragmentHomeBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.favorits.viewmodel.FavoritesViewModel
import com.iti4.retailhub.features.home.adapter.AdsViewPagerAdapter
import com.iti4.retailhub.features.home.adapter.BrandAdapter
import com.iti4.retailhub.features.home.adapter.DotsIndicatorDecoration
import com.iti4.retailhub.features.home.adapter.NewItemAdapter
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.features.productdetails.viewmodel.ProductDetailsViewModel
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.HomeProducts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), OnClickGoToDetails {

    private val viewModel by viewModels<HomeViewModel>()
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val productDetailsViewModel by viewModels<ProductDetailsViewModel>()
    val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
lateinit var adapter: NewItemAdapter
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0
    private var currentPosition = 0
    private var autoScrollJob: Job? = null // Job for the coroutine

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
        adapter = NewItemAdapter(this@HomeFragment, emptyList(),currencyCode,conversionRate)
        displayAds()
        getHomeProducts()
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

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.brands.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            val data = item.data as List<Brands>
                            displayBrandsRowData(data)
                        }

                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                item.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiState.Loading -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.couponsState.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {
                        val data = item.data as List<Discount>
                        adsAdapter.setData(data)

                    }

                    is ApiState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            item.exception.message,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    is ApiState.Loading -> {}
                }
            }
        }
    }
private fun getFavorites(){
    favoritesViewModel.getFavorites()
    lifecycleScope.launch {
        favoritesViewModel.savedFavortes.collect { item ->
            when (item) {
                is ApiState.Success<*> -> {
                    val data = item.data as GetCustomerFavoritesQuery.Customer
                    val favoritList = data.metafields.nodes.filter { it.key == "favorites" }

                    adapter.updateFavorites(favoritList)

                }
                is ApiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        item.exception.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                is ApiState.Loading -> {}
            }
        }
    }
}
    private fun getHomeProducts() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.products.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            binding.animationView.visibility = View.GONE
                            val data = item.data as List<HomeProducts>
                            displayNewItemRowData(data)
                        }

                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                item.exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun displayNewItemRowData(
        data: List<HomeProducts>
    ) {
        binding.newItemRow.apply {
            title.text = getString(R.string.new_item)
            subtitle.text = getString(R.string.you_ve_never_seen_it_before)
            recyclerView.adapter = adapter
            adapter.submitList(data)
            if (!userAuthViewModel.isguestMode()) {
                getFavorites()
            }
        }
    }

    private fun displayBrandsRowData(data: List<Brands>) {
        binding.brandItemRow.apply {
            title.text = getString(R.string.brands)
            subtitle.text = getString(R.string.brands_subtitle)
            val adapter = BrandAdapter()
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
        adsAdapter = AdsViewPagerAdapter(listOf())
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.vpHomeAds)
        binding.vpHomeAds.adapter = adsAdapter
        binding.vpHomeAds.addItemDecoration(
            DotsIndicatorDecoration(
                colorInactive = ContextCompat.getColor(this.requireContext(), R.color.red_color),
                colorActive = ContextCompat.getColor(this.requireContext(), R.color.black_variant)
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
       if (!userAuthViewModel.isguestMode()){
           productDetailsViewModel.saveToFavorites(
               productId,productId,
               productTitle,selectedImage,price
           )
           lifecycleScope.launch {
               productDetailsViewModel.saveProductToFavortes.collect { item ->
                   when (item) {
                       is ApiState.Success<*> -> {
                           val data =
                               item.data as UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate
                           Toast.makeText(
                               requireContext(),
                               "Add to your favorites",
                               Toast.LENGTH_SHORT
                           )
                               .show()
                           Log.d("fav", "onViewCreated:${data} ")
                           favoritesViewModel.getFavorites()
                       }

                       is ApiState.Error -> {
                           Toast.makeText(
                               requireContext(),
                               item.exception.message,
                               Toast.LENGTH_SHORT
                           )
                               .show()
                       }

                       is ApiState.Loading -> {}
                   }
               }
           }
       }else{
           showGuestDialog()
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
    override fun deleteFromCustomerFavorites(pinFavorite: String) {
        if (!userAuthViewModel.isguestMode()) {
            favoritesViewModel.deleteFavorites(pinFavorite)
            lifecycleScope.launch {
                favoritesViewModel.deletedFavortes.collect { item ->

                    when (item) {

                        is ApiState.Success<*> -> {
                            Toast.makeText(
                                requireContext(),
                                "Product Is Deleted",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            favoritesViewModel.getFavorites()
                        }

                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                item.exception.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is ApiState.Loading -> {}
                    }
                }
            }
        }else{
            showGuestDialog()
        }

}
    override fun onDestroy() {
        super.onDestroy()
        autoScrollJob?.cancel()
    }
}