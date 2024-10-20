package com.iti4.retailhub.features.productdetails.view

import android.annotation.SuppressLint
import com.iti4.retailhub.R
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.weathercast.alarmandnotification.view.ProductDetailsDiffUtilAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.GetDraftOrdersByCustomerQuery
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.databinding.FragmentProductDetailsBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.favorits.viewmodel.FavoritesViewModel
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.features.productdetails.view.bottom_dialog_adapter.BottomDialogDiffUtilAdapter
import com.iti4.retailhub.features.productdetails.view.bottom_dialog_adapter.ButtomDialogOnClickListn
import com.iti4.retailhub.features.productdetails.viewmodel.ProductDetailsViewModel
import com.iti4.retailhub.features.reviwes.view.ReviewsDiffUtilAdapter
import com.iti4.retailhub.features.reviwes.viewmodel.ReviewsViewModel
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProductDetailsFragment : Fragment(), ButtomDialogOnClickListn {
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val productDetailsViewModel by viewModels<ProductDetailsViewModel>()
    private val reviewsViewModel by viewModels<ReviewsViewModel>()
    private val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels()

    lateinit var dialog:Dialog
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0

    lateinit var binding: FragmentProductDetailsBinding

    lateinit var produsctDetailsAdapter: ProductDetailsDiffUtilAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    var productId = ""

    var idTodelete = ""

    var addToFavoritsFirstClick = true


    var productVariants: List<ProductDetailsQuery.Edge>? = null
    var selectedProductVariantId: String = ""
    var selectedProductColor: String = ""
    var selectedProductSize: String = ""
    lateinit var allColors: List<String>
    lateinit var sizesForSelectedColor: List<String>
    lateinit var productTitle: String
    lateinit var seelectedImage: String
    var isVariantInCustomerDraftOrders = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString("productid") as String
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)



        currencyCode = productDetailsViewModel.getCurrencyCode()
        conversionRate = productDetailsViewModel.getConversionRates(currencyCode)


        binding.sesMoreReviews.setOnClickListener {
            findNavController().navigate(R.id.reviewsFragment)
        }

//        val viewPager: ViewPager2 = binding.productImages
//        val circleIndicator = findViewById<CircleIndicator2>(R.id.circleIndicator)

// Set up adapter
//        val adapter = YourAdapter(items) // Assuming you have a list of items
//        viewPager.adapter = adapter

// Link CircleIndicator2 with ViewPager2
//        circleIndicator.setViewPager(viewPager)

       /*val viewAdapter= ViewAdapter(requireContext(), emptyList())
        binding.viewPager.setAdapter(viewAdapter);
        binding.dot1.setViewPager(viewPager);*/
        /*produsctDetailsAdapter = ProductDetailsDiffUtilAdapter(requireContext())
        binding.productImages.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.productImages.adapter = produsctDetailsAdapter*/



        showProductDetails()
        binding.frameLayout3.setOnClickListener{
            setupBottomDialog(sizesForSelectedColor,"size",selectedProductSize)
        }
        binding.frameLayout4.setOnClickListener{
            setupBottomDialog(allColors,"color",selectedProductColor)
        }



        getReviewes()

        binding.addtocard.setOnClickListener {
            if (userAuthViewModel.isguestMode()) {
                showGuestAlert("login to add to your bag")
            }
        }
        binding.cardView3.setOnClickListener {
            if (!userAuthViewModel.isguestMode()) {
                if (addToFavoritsFirstClick) {
                    addToFavoritsFirstClick = false
                    addToFavorits()
                } else {
                    showDeleteAlert()

                }
            }else{
                showGuestAlert("login to add to your favorites")
            }
        }
    }

    private fun showProductDetails() {
        productDetailsViewModel.getProductDetails(productId)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                productDetailsViewModel.productDetails.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {

                            val data = item.data as ProductDetailsQuery.OnProduct

                            //get all variant that inventory>0
                            productVariants =
                                data.variants.edges.filter { it.node.inventoryQuantity!! > 0 }

                            //set product details
                            productTitle = data.title

                            //productvariant
                            selectedProductVariantId = productVariants!!.get(0).node.id
                            //get favorites
                            if (!userAuthViewModel.isguestMode()) {
                                searchInCustomerFavorites()
                                searcheInBag()
                            }

                            val productTitleList = productTitle.split("|")
                            binding.prand.text = productTitleList[0]
                            binding.title.text = productTitleList.drop(1).joinToString(" | ")

                            //images
//                            produsctDetailsAdapter.submitList(data.images.edges)
                            seelectedImage = data.images.edges[0].node.url.toString()
                            // Set up your adapter
                            val adapter = ProductDetailsAdapter(data.images.edges)
                            viewPager.adapter = adapter
                            binding.circular.setViewPager(viewPager)
                            // Link TabLayout with ViewPager2
                            TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()


                            binding.productDescription.text = data.description
                            binding.productPrice.text =
                                ((productVariants!!.get(0).node.presentmentPrices.edges[0].node.price.amount.toString().toDouble())
                                        *(conversionRate)).toTwoDecimalPlaces() + " " + currencyCode.name
                            "In Inventory: ${productVariants!![0].node.inventoryQuantity}".also { binding.inInventory.text = it }


                            //get all colors and sizes from variant
                           /* sizesForSelectedColor = productVariants!!
                                .mapNotNull { it.node.selectedOptions.find { option -> option.name == "Size" }?.value }
                                .distinct()
                            binding.spinnersize.text = sizesForSelectedColor[0]
                            selectedProductSize = sizesForSelectedColor[0]*/
                            allColors = productVariants!!
                                .mapNotNull { it.node.selectedOptions.find { option -> option.name == "Color" }?.value }
                                .distinct()
                            setColor(allColors[0])
//                            binding.spinnercolor.text = allColors[0]
                            selectedProductColor = allColors[0]



                            // Get all sizes for the first color
                            val selectedColor = allColors[0]
                             sizesForSelectedColor = productVariants!!
                                .filter { variant ->
                                    variant.node.selectedOptions.any { option -> option.name == "Color" && option.value == selectedColor }
                                }
                                .mapNotNull { it.node.selectedOptions.find { option -> option.name == "Size" }?.value }
                                .distinct()

                            binding.spinnersize.text = sizesForSelectedColor[0]
                            selectedProductSize = sizesForSelectedColor[0]

                        }

                        is ApiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                "Can't get product details, please reload page",
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

    private fun deleteFromFavorites() {

        favoritesViewModel.deleteFavorites(idTodelete)
        lifecycleScope.launch {
            favoritesViewModel.deletedFavortes.collect { item ->

                when (item) {

                    is ApiState.Success<*> -> {
                        searchInCustomerFavorites()
                        binding.imageView5oFavorits.setImageResource(R.drawable.baseline_favorite_border_24)
                        addToFavoritsFirstClick = true
                        Toast.makeText(
                            requireContext(),
                            "Product Is Deleted",
                            Toast.LENGTH_SHORT
                        )
                            .show()
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

    private fun searchInCustomerFavorites() {

        productDetailsViewModel.searchProductInCustomerFavorites(productId)

        lifecycleScope.launch {

            productDetailsViewModel.productInFavorites.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {

                        val data = item.data as GetCustomerFavoritesQuery.Customer
                        Log.d("details", "searchInCustomerFavorites:${data.metafields.nodes} ")
                        if (data.metafields.nodes.isNotEmpty()) {
                            val node = data.metafields.nodes.find { it.namespace.contains(productId) }

                            if(node?.namespace?.contains(productId) == true) {
                                addToFavoritsFirstClick=false
                                binding.imageView5oFavorits.setImageResource(com.iti4.retailhub.R.drawable.fav_filled)
                                idTodelete = node.id
                            }
                        }

                    }

                    is ApiState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error, can't find in favorites please try again",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    is ApiState.Loading -> {}
                }
            }
        }
    }

    private fun getReviewes() {
        val allReviews = reviewsViewModel.getAllReviews(3)
        val reviewsAdapter = ReviewsDiffUtilAdapter(requireContext())
        binding.reviewsRecycleView.layoutManager = LinearLayoutManager(context)
        binding.reviewsRecycleView.adapter = reviewsAdapter
        reviewsAdapter.submitList(allReviews)
        binding.ratingBar3.rating = allReviews.map { it.rate }.average().toFloat()
    }
     fun showDeleteAlert() {
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.favorit_delete_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)
        val tvmessage=dialog.findViewById<TextView>(R.id.tvMessage)
        tvmessage.text="Are you sure you want to delete this product?"
        btnYes.setOnClickListener {
            deleteFromFavorites()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun searcheInBag() {
        productDetailsViewModel.getDraftOrdersByCustomer("${productTitle} - $selectedProductColor / $selectedProductSize")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            productDetailsViewModel.customerDraftOrders.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {
                        val data = item.data as GetDraftOrdersByCustomerQuery.DraftOrders



                        if (data.nodes.isNotEmpty()) {
                            val productVariantInBag = data.nodes[0].lineItems.nodes[0].variant?.id

                            if (!productVariantInBag.isNullOrEmpty()) {

                                isVariantInCustomerDraftOrders = true
//                                binding.addtocard.text = "Open In Your Bag"
                                addToBagButtonClickListner(true)
                            }
                        } else {
//                            binding.addtocard.text = "Add To Bag"
                            addToBagButtonClickListner(false)
                        }
                    }

                    is ApiState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            item.exception.message,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    is ApiState.Loading -> {

                    }
                }
            }
        }
        }
    }

    private fun addToFavorits() {
        productDetailsViewModel.saveToFavorites(
            productId,selectedProductVariantId,
            productTitle,seelectedImage,binding.productPrice.text.toString()
        )
        lifecycleScope.launch {
            productDetailsViewModel.saveProductToFavortes.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {

//                        val data = item.data as  UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate

                        searchInCustomerFavorites()

                        Toast.makeText(
                            requireContext(),
                           "Added to your favorites",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        binding.imageView5oFavorits.setImageResource( com.iti4.retailhub.R.drawable.fav_filled)

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

//---------------
    private fun addToBagButtonClickListner(isAdd:Boolean){
        binding.addtocard.setOnClickListener {
            if (!userAuthViewModel.isguestMode()) {
                if (isAdd) {
//                    findNavController().navigate(com.iti4.retailhub.R.id.myBagFragment)
                    Toast.makeText(requireContext(), "Item is in your bag", Toast.LENGTH_SHORT).show()
                } else {
                    addToBag()
                }
            }
        }
    }
    private fun showGuestAlert(message: String) {
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        dialog.setContentView(R.layout.guest_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val messag=dialog.findViewById<TextView>(R.id.messaged)
        messag.text=message
        val btnYes: Button = dialog.findViewById(R.id.btn_okayd)
        val btnNo: Button = dialog.findViewById(R.id.btn_canceld)
        btnYes.setOnClickListener {
            val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
            intent.putExtra("guest","guest")
            startActivity(intent)
            requireActivity().finish()
            requireActivity().finish()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addToBag(){
        Log.d("choose", "choosedItem:${selectedProductVariantId} ")
        productDetailsViewModel.addToCart(selectedProductVariantId)
            lifecycleScope.launch {
                productDetailsViewModel.createDraftOrder.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
//                            val data = item.data as CreateDraftOrderMutation.DraftOrderCreate
//                            binding.addtocard.text = "Open In Your Bag"
                            addToBagButtonClickListner(true)
                            Toast.makeText(requireContext(), "Item add to your bag", Toast.LENGTH_SHORT).show()
                        }
                        is ApiState.Error -> {
                            Toast.makeText(requireContext(),"Can't add to bag, try again", Toast.LENGTH_SHORT)
                                .show()
                        }
                        is ApiState.Loading -> {}
                    }
                }
            }
        }




    private fun setupBottomDialog(List: List<String>,type:String,selected:String) {
        dialog= Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.product_details_bottom_sheet)

        val recucleView = dialog.findViewById<RecyclerView>(R.id.sizeRV)
        val text = dialog.findViewById<TextView>(R.id.select)
        text.text="Select $type"
        val adapter = BottomDialogDiffUtilAdapter(type,this,selected)
        recucleView.layoutManager = GridLayoutManager(requireContext(),3)
        recucleView.adapter = adapter
        adapter.submitList(List)

        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    override fun choosedItem(position: Int, item: String?, type: String) {
        if(type=="size"){
            selectedProductSize=item.toString()
            binding.spinnersize.text=item.toString()
            /*selectedProductVariantId=productVariants!!.find { it.node.selectedOptions.find { option -> option.name == "Size" }?.value == item }!!.node.id
            */
            selectedProductVariantId = productVariants!!.find { variant ->
                variant.node.selectedOptions.any { option -> option.name == "Size" && option.value == item } &&
                        variant.node.selectedOptions.any { option -> option.name == "Color" && option.value == selectedProductColor }
            }?.node?.id.toString()

        }else{
            selectedProductColor=item.toString()
            setColor(item.toString())
            sizesForSelectedColor = productVariants!!
                .filter { variant ->
                    variant.node.selectedOptions.any { option -> option.name == "Color" && option.value == selectedProductColor }
                }
                .mapNotNull { it.node.selectedOptions.find { option -> option.name == "Size" }?.value }
                .distinct()
//            binding.spinnercolor.text=item.toString()
            /*selectedProductVariantId=productVariants!!.find { it.node.selectedOptions.find { option -> option.name == "Color" }?.value == item }!!.node.id
            Log.d("choose", "choosedItem:${productVariants!!.find { it.node.selectedOptions.find { option -> option.name == "Color" }?.value == item }!!.node.id} ")
        */
            selectedProductVariantId = productVariants!!.find { variant ->
                variant.node.selectedOptions.any { option -> option.name == "Size" && option.value == selectedProductSize } &&
                        variant.node.selectedOptions.any { option -> option.name == "Color" && option.value == item }
            }?.node?.id.toString()
        }
        binding.inInventory.text= "In Inventory: ${productVariants!!.find { it.node.selectedOptions.find { option -> option.name == "Size" }?.value == item }!!.node.inventoryQuantity}".also { binding.inInventory.text = it }
        if(!userAuthViewModel.isguestMode()) {
            searcheInBag()
        }
        dialog.dismiss()
    }
    private fun setColor(color: String) {
        when (color.toLowerCase()) {
            "burgandy" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.burgandy))
            "red" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.red_color))
            "white" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.white))
            "blue" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.blue))
            "black" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.black))
            "gray" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.gray))
            "light_brown" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.light_brown))
            "beige" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.beige))
            "yellow" -> binding.spinnercolor.setBackgroundColor(binding.root.context.getColor(com.iti4.retailhub.R.color.yellow))
        }
    }
}



//                binding.back.setOnClickListener {
//                    activity?.supportFragmentManager?.popBackStack()
//                }