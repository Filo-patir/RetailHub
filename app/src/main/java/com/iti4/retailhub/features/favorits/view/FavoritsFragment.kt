package com.iti4.retailhub.features.favorits.view

import android.app.Dialog
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentFavoritsBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.favorits.view.adapter.FavoritsDiffUtilAdapter
import com.iti4.retailhub.features.favorits.view.adapter.OnFavoritItemClocked
import com.iti4.retailhub.features.favorits.viewmodel.FavoritesViewModel
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritsFragment : Fragment(), OnFavoritItemClocked {
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    lateinit var binding: FragmentFavoritsBinding
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0
    lateinit var favoritesAdapter: FavoritsDiffUtilAdapter
    val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()

    override fun onStart() {
        super.onStart()
        binding.mybagAppbar.apply {
            appBar.setExpanded(false)
            collapsedPageName.visibility = View.GONE
            pageName.text = requireContext().getString(R.string.favorites)
            backButton.setOnClickListener {
                activity?.onBackPressed()
            }
            imageButton.setOnClickListener { findNavController().navigate(R.id.producSearchFragment) }
        }
        binding.mybagAppbar.imageButton.setOnClickListener {
            requireActivity().findNavController(R.id.fragmentContainerView2)
                .navigate(R.id.producSearchFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFavoritsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currencyCode = favoritesViewModel.getCurrencyCode()
        conversionRate = favoritesViewModel.getConversionRates(currencyCode)
        if (userAuthViewModel.isguestMode()){
            binding.guestf.visibility=View.VISIBLE
            binding.tvMessage.text=getString(R.string.please_login_to_use_this_feature)
            binding.btnOkayd.setOnClickListener {
                val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
                intent.putExtra("guest","guest")
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
            favoritesAdapter = FavoritsDiffUtilAdapter(
                requireContext(),
                this,
                conversionRate,
                currencyCode
            )
            binding.favoritsRecycleView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.favoritsRecycleView.adapter = favoritesAdapter


            favoritesViewModel.getFavorites()
            savedFavoritesCollect()
        }


    }

    private fun savedFavoritesCollect() {
        lifecycleScope.launch {
            favoritesViewModel.savedFavortes.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {

                        val data = item.data as GetCustomerFavoritesQuery.Customer


                        binding.favoritProgress.visibility = View.GONE



                        val favoritList = data.metafields.nodes
                        favoritesAdapter.submitList(favoritList.filter { it.key == "favorites" })


                        if (favoritList.isEmpty()) {
                            binding.lottibagAnimation.visibility = View.VISIBLE
                        } else {
                            binding.lottibagAnimation.visibility = View.GONE
                            binding.favoritsRecycleView.visibility = View.VISIBLE
                        }
                    }

                    is ApiState.Error -> {
                        if(item.exception.message != "Something went wrong")
                            Toast.makeText(requireContext(), item.exception.message, Toast.LENGTH_SHORT).show()
                    }

                    is ApiState.Loading -> {
                    }
                }
            }
        }
    }

    override fun onShowFavoritItemDetails(variantId: String) {
        val bundle = Bundle()
        bundle.putString("productid", variantId)
        findNavController().navigate(R.id.productDetailsFragment, bundle)
    }

    override fun showDeleteAlert(id: String) {
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.favorit_delete_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)
        val tvmessage = dialog.findViewById<TextView>(R.id.tvMessage)
        tvmessage.text = getString(R.string.are_you_sure_you_want_to_delete_this_product)
        btnYes.setOnClickListener {
            deleteFavoritProduct(id)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteFavoritProduct(id: String) {

        favoritesViewModel.deleteFavorites(id)

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
    }
}