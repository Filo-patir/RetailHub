package com.iti4.retailhub.features.checkout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.MainActivityViewModel
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentCheckoutBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.summary.PaymentIntentResponse
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.logic.extractNumbersFromString
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.CustomerAddressV2
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckoutFragment : Fragment(), OnClickBottomSheet {
    private val currencyCode by lazy { mainActivityViewModel.getCurrencyCode() }
    private lateinit var binding: FragmentCheckoutBinding
    private var checkoutAddress: CustomerAddressV2? = null
    private var checkoutDefaultAddress: GetAddressesDefaultIdQuery.DefaultAddress? = null
    private lateinit var cartProducts: List<CartProduct>
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var appearance: PaymentSheet.Appearance
    private var conversionRate: Double? = null
    private var totalPrice: Double? = null
    private var totalSummary: Double? = null
    private var totalPriceInCents: Int? = null
    private val viewModel by viewModels<CheckoutViewModel>()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private lateinit var requiredView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conversionRate = mainActivityViewModel.getConversionRates(currencyCode)
        requiredView = view
        cartProducts =
            arguments?.getParcelableArrayList<CartProduct>("data") as MutableList<CartProduct>
        totalPrice = arguments?.getDouble("totalprice")
        totalPrice = totalPrice!!
        totalSummary = totalPrice
        totalPriceInCents = totalPrice!!.times(100).toInt()
        binding.tvDiscountPrice.text = "0 ${currencyCode.name}"
        binding.tvOrderPrice.text = totalPrice!!.toTwoDecimalPlaces() + " ${currencyCode.name}"
        binding.tvSummary.text =
            totalSummary!!.toTwoDecimalPlaces() + " ${currencyCode.name}"

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        viewModel.getCustomerData()
        listenToPIChanges()
        listenToCustomerDataResponse()
        listenToCustomerAddress()
        binding.btnCheckoutAddNewAddress.setOnClickListener {
            val bundle = Bundle().apply {
                putString("reason", "changeShipping")
            }
            requireActivity().findNavController(R.id.fragmentContainerView2)
                .navigate(R.id.addressFragment, bundle)
        }
        binding.btnChangeAddress.setOnClickListener {
            val bundle = Bundle().apply {
                putString("reason", "changeShipping")
            }
            requireActivity().findNavController(R.id.fragmentContainerView2)
                .navigate(R.id.addressFragment, bundle)
        }

        binding.btnSubmitOrder.setOnClickListener {
            if (checkoutAddress != null || checkoutDefaultAddress != null) {
                binding.tvNoAddressAtCheckout.visibility = View.INVISIBLE
                val discountCode = binding.promocodeEdittext.etPromoCode.text.toString()
                if (discountCode.isNullOrEmpty()) {
                    binding.tvInvalidDiscount.visibility = View.INVISIBLE
                    proceedWithCheckout()
                } else {
                    if (checkDiscount(discountCode)) {
                        binding.tvInvalidDiscount.visibility = View.INVISIBLE
                        proceedWithCheckout()
                    } else {
                        binding.tvInvalidDiscount.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.tvNoAddressAtCheckout.visibility = View.VISIBLE
            }
        }
        binding.promocodeEdittext.btnInsertCode.setOnClickListener {
            val bottomSheet = MyBottomSheetFragment(this)
            bottomSheet.show(this.requireActivity().supportFragmentManager, bottomSheet.tag)
        }
        binding.promocodeEdittext.btnDeleteCode.setOnClickListener {
            binding.tvInvalidDiscount.visibility = View.VISIBLE
            binding.promocodeEdittext.btnInsertCode.visibility = View.VISIBLE
            binding.promocodeEdittext.btnDeleteCode.visibility = View.GONE
            binding.promocodeEdittext.etPromoCode.setText("")
            binding.promocodeEdittext.etPromoCode.isFocusableInTouchMode = false
            binding.promocodeEdittext.etPromoCode.isFocusable = false
        }
        binding.promocodeEdittext.etPromoCode.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.promocodeEdittext.etPromoCode.isFocusableInTouchMode = true
                binding.promocodeEdittext.etPromoCode.isFocusable = true
                val bottomSheet = MyBottomSheetFragment(this)
                bottomSheet.show(this.requireActivity().supportFragmentManager, bottomSheet.tag)
                return@setOnTouchListener true
            }
            false
        }
    }

    override fun passData(data: String) {
        binding.promocodeEdittext.btnInsertCode.visibility = View.GONE
        binding.promocodeEdittext.btnDeleteCode.visibility = View.VISIBLE
        binding.promocodeEdittext.etPromoCode.setText(data)
        if (checkDiscount(data)) {
            val discountRate = viewModel.selectedDiscount!!.getDiscountAsDouble() / 100
            val discountAmount = totalPrice!! * discountRate
            val totalSummary = totalPrice!! - discountAmount
            totalPriceInCents = totalSummary.times(100).toInt()
            binding.tvDiscountPrice.text =
                "- " + discountAmount.toTwoDecimalPlaces() + " ${currencyCode.name}"
            binding.tvSummary.text = totalSummary.toTwoDecimalPlaces() + " ${currencyCode.name}"
            binding.tvInvalidDiscount.visibility = View.INVISIBLE
        } else
            binding.tvInvalidDiscount.visibility = View.VISIBLE
    }

    private fun listenToPIChanges() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.paymentIntentResponse.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            val paymentIntentResponse = item.data as PaymentIntentResponse
                            customerConfig = PaymentSheet.CustomerConfiguration(
                                paymentIntentResponse.customerId,
                                paymentIntentResponse.ephemeralKey
                            )
                            paymentIntentClientSecret = paymentIntentResponse.clientSecret
                            initPaymentSheetAppearance()
                            presentPaymentSheet()
                        }

                        is ApiState.Error -> {}
                        is ApiState.Loading -> {}
                    }
                }
            }
        }

    }

    private fun listenToCustomerDataResponse() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.customerResponse.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            viewModel.getDefaultAddress()
                            // show customer address
                            //load customer discounts
                        }

                        is ApiState.Error -> {}
                        is ApiState.Loading -> {}
                    }
                }
            }
        }

    }

    private fun listenToCustomerAddress() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.addressesState.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            if (!mainActivityViewModel.customerChoseAnAddressNotDefault) {
                                val customer = item.data as GetAddressesDefaultIdQuery.Customer
                                var address = customer.defaultAddress
                                if (address == null) {
                                    binding.groupNoAddress.visibility = View.VISIBLE
                                    binding.cvAddressCheckout.visibility = View.INVISIBLE
                                } else {
                                    binding.groupNoAddress.visibility = View.INVISIBLE
                                    binding.cvAddressCheckout.visibility = View.VISIBLE
                                    checkoutAddress = null
                                    checkoutDefaultAddress = address
                                }
                                binding.tvAddressAddress.text = address?.address1 ?: " "
                                binding.tvAddressRestOfAddress.text = address?.address2 ?: " "
                                binding.tvAddressFullName.text = address?.name ?: " "
                                binding.tvPhone.text = address?.phone ?: " "

                                binding.shimmerCheckout.stopShimmer()
                                binding.shimmerCheckout.hideShimmer()


                                // show customer address
                                //load customer discounts
                            } else {
                                var address = mainActivityViewModel.customerChosenAddress
                                binding.groupNoAddress.visibility = View.INVISIBLE
                                binding.cvAddressCheckout.visibility = View.VISIBLE
                                binding.tvAddressAddress.text = address?.address1 ?: " "
                                binding.tvAddressRestOfAddress.text = address?.address2 ?: " "
                                binding.tvAddressFullName.text = address?.name ?: " "
                                binding.tvPhone.text = address?.phone ?: " "

                                binding.shimmerCheckout.stopShimmer()
                                binding.shimmerCheckout.hideShimmer()

                                checkoutAddress = address
                                // show customer address
                                //load customer discounts
                            }

                        }

                        is ApiState.Error -> {}
                        is ApiState.Loading -> {}
                    }
                }
            }
        }

    }

    private fun listenToCreateCheckoutDraftOrder() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.checkoutDraftOrderCreated.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            binding.lottieAnimSubmitOrder.visibility = View.GONE
                            binding.lottieAnimSubmitOrder.pauseAnimation()
                            binding.btnSubmitOrder.isEnabled = true
                            binding.btnSubmitOrder.text = "CONTINUE"
                            requireActivity().findNavController(R.id.fragmentContainerView2)
                                .navigate(R.id.summaryFragment)

                            Toast.makeText(
                                this@CheckoutFragment.requireActivity(),
                                "Order Submitted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiState.Error -> {}
                        is ApiState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "RetailHub",
                customer = customerConfig,
                appearance = appearance
            )
        )
    }

    private fun initPaymentSheetAppearance() {
        appearance = PaymentSheet.Appearance(
            colorsLight = PaymentSheet.Colors(
                primary = resources.getColor(R.color.red_color),
                component = resources.getColor(R.color.white),
                surface = resources.getColor(R.color.background_color),
                componentBorder = resources.getColor(R.color.transparent),
                componentDivider = resources.getColor(R.color.black_variant),
                onComponent = resources.getColor(R.color.black_variant),
                subtitle = resources.getColor(R.color.black_variant),
                placeholderText = resources.getColor(R.color.grey),
                onSurface = resources.getColor(R.color.black_variant),
                appBarIcon = resources.getColor(R.color.black_variant),
                error = resources.getColor(R.color.red_color),
            ),
            shapes = PaymentSheet.Shapes(
                cornerRadiusDp = 12.0f,
                borderStrokeWidthDp = 0.5f
            ),
            typography = PaymentSheet.Typography.default.copy(
                fontResId = R.font.metropolis_regular,
                sizeScaleFactor = 1.10f
            ),
            primaryButton = PaymentSheet.PrimaryButton(
                shape = PaymentSheet.PrimaryButtonShape(
                    cornerRadiusDp = 20f
                ),
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                viewModel.createCheckoutDraftOrder(
                    cartProducts,
                    true,
                    checkoutAddress,
                    checkoutDefaultAddress
                )
                listenToCreateCheckoutDraftOrder()
            }

            is PaymentSheetResult.Canceled -> {
                binding.lottieAnimSubmitOrder.visibility = View.GONE
                binding.lottieAnimSubmitOrder.pauseAnimation()
                binding.btnSubmitOrder.isEnabled = true
                binding.btnSubmitOrder.text = "SUBMIT ORDER"
                Toast.makeText(this.requireActivity(), "Payment Canceled", Toast.LENGTH_SHORT)
                    .show()
            }

            is PaymentSheetResult.Failed -> {
                binding.lottieAnimSubmitOrder.visibility = View.GONE
                binding.lottieAnimSubmitOrder.pauseAnimation()
                binding.btnSubmitOrder.isEnabled = true
                binding.btnSubmitOrder.text = "SUBMIT ORDER"
                Toast.makeText(this.requireActivity(), "Payment Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.navigationView).visibility =
            View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.navigationView).visibility =
            View.GONE

        ToolbarSetup.setupToolbarMini(
            binding.checkoutAppbar,
            "Checkout",
            resources,
            { requireActivity().onBackPressed() }
        )

    }

    private fun checkDiscount(discountCode: String): Boolean {
        binding.tvInvalidDiscount.text = resources.getString(R.string.DiscountInvalid)
        if (!mainActivityViewModel.discountList.isNullOrEmpty()) {
            val foundDiscount = mainActivityViewModel.discountList!!.filter {
                it.title == discountCode
            }
            if (foundDiscount.isNotEmpty()) {
                mainActivityViewModel.usedDiscountCodesList.forEach {
                    if (it == discountCode) {
                        binding.tvInvalidDiscount.text = resources.getString(R.string.DiscountUsed)
                        return false
                    }

                }
                "discount found"
                viewModel.selectedDiscount = foundDiscount[0]
                return true
            } else {
                "no discount avaialbe"
                return false
            }

        } else {
            return false
        }
    }

    private fun proceedWithCheckout() {
        if (binding.radioGroupPaymentMethod.checkedRadioButtonId != -1) {
            when (requiredView.findViewById<RadioButton>(binding.radioGroupPaymentMethod.checkedRadioButtonId).text.toString()) {
                getString(R.string.CashOnDelivery) -> {
                    binding.lottieAnimSubmitOrder.visibility = View.VISIBLE
                    binding.btnSubmitOrder.isEnabled = false
                    binding.btnSubmitOrder.text = ""
                    viewModel.createCheckoutDraftOrder(
                        cartProducts,
                        false,
                        checkoutAddress,
                        checkoutDefaultAddress
                    )
                    listenToCreateCheckoutDraftOrder()
                }

                getString(R.string.PayWithCard) -> {
                    binding.lottieAnimSubmitOrder.visibility = View.VISIBLE
                    binding.btnSubmitOrder.isEnabled = false
                    binding.btnSubmitOrder.text = ""
                    viewModel.createPaymentIntent(totalPriceInCents!!)
                }
            }
        }
    }
}