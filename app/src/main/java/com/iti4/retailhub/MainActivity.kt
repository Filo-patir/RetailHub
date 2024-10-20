package com.iti4.retailhub

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.iti4.retailhub.databinding.ActivityMainBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.address.AddressViewModel
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.models.CurrencyResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Response


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: AddressViewModel by viewModels()
    private val viewModel by viewModels<MainActivityViewModel>()
    val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.getDiscount()
        if (!userAuthViewModel.isguestMode()){
            viewModel.getUsedDiscounts()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.navigationView.apply {
            setOnItemSelectedListener { menuItem ->
                val navController = Navigation.findNavController(this@MainActivity, R.id.fragmentContainerView2)
                if (menuItem.itemId != navController.currentDestination?.id) {
                    Log.d("Filo", "Navigating to: ${menuItem.itemId}")
                    navController.navigate(menuItem.itemId, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, false).build())
                    true
                } else {
                    false
                }
            }
        }
        lifecycleScope.launch {
            val fragments = supportFragmentManager.findFragmentById(R.id.fragmentContainerView2)?.childFragmentManager?.fragments
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isConnectedToNetwork.collect{ networkState ->
                    if(networkState){
                        fragments?.forEach{
                            it.view?.isEnabled = true
                        }
                        binding.navigationView.visibility = View.VISIBLE
                        findViewById<View>(R.id.fragmentContainerView2).visibility = View.VISIBLE
                        binding.networkAnimView.visibility = View.GONE
                    }else{
                        fragments?.forEach{
                            it.view?.isEnabled = false
                        }
                        binding.navigationView.visibility = View.GONE
                        findViewById<View>(R.id.fragmentContainerView2).visibility = View.GONE
                        binding.networkAnimView.visibility = View.VISIBLE
                    }
                }
            }
        }

        if (viewModel.getFirstTime() || viewModel.getShouldIRefrechCurrency()) {
            viewModel.setFirstTime()
            viewModel.setRefrechCurrency()
            viewModel.getCurrencyRates()
            initCurrencyRatesListen()
        }
    }

    override fun onBackPressed() {
        val navController = Navigation.findNavController(this, R.id.fragmentContainerView2)
        if (navController.previousBackStackEntry != null) {

            navController.navigateUp()
            if(navController.currentDestination?.id == R.id.homeFragment){
                binding.navigationView.menu[0].isChecked = true
            }
        } else {
            super.onBackPressed()
        }
    }


    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = currentFocus // Get the currently focused view
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    fun hideBottomNavBar() {
        binding.navigationView.visibility = View.GONE;
    }

    fun showBottomNavBar() {
        binding.navigationView.visibility = View.VISIBLE;
    }

    fun initCurrencyRatesListen() {
        lifecycleScope.launch {
            viewModel.currencyState.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {
                        if (item.data != null) {
                            val response = item.data as Response<CurrencyResponse>
                            if (response.isSuccessful) {
                                val datas = response.body()!!.conversion_rates
                                viewModel.saveConversionRates(datas)
                            }
                        }
                    }

                    is ApiState.Error -> {
                        Log.i("here", "error: ")
                    }

                    is ApiState.Loading -> {}
                }
            }

        }
    }
}


