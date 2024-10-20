package com.iti4.retailhub.datastorage


import android.content.Intent
import android.content.IntentSender
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.iti4.retailhub.AddTagsMutation
import com.iti4.retailhub.CompleteDraftOrderMutation
import com.iti4.retailhub.CreateCustomerMutation
import com.iti4.retailhub.CreateDraftOrderMutation
import com.iti4.retailhub.CustomerEmailSearchQuery
import com.iti4.retailhub.CustomerUpdateDefaultAddressMutation
import com.iti4.retailhub.DeleteDraftOrderMutation
import com.iti4.retailhub.DraftOrderInvoiceSendMutation
import com.iti4.retailhub.GetAddressesByIdQuery
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.GetCustomerByIdQuery
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.GetDraftOrdersByCustomerQuery
import com.iti4.retailhub.MarkAsPaidMutation
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.UpdateCustomerAddressesMutation
import com.iti4.retailhub.UpdateCustomerFavoritesMetafieldsMutation
import com.iti4.retailhub.UpdateDraftOrderMutation
import com.iti4.retailhub.datastorage.network.RemoteDataSource
import com.iti4.retailhub.datastorage.network.RetrofitDataSource
import com.iti4.retailhub.datastorage.reviews.ReviewsDataStoreInterface
import com.iti4.retailhub.datastorage.userlocalprofiledata.UserLocalProfileDataInterface
import com.iti4.retailhub.features.summary.PaymentRequest
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.CurrencyResponse
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.DraftOrderInputModel
import com.iti4.retailhub.models.HomeProducts
import com.iti4.retailhub.models.Order
import com.iti4.retailhub.models.OrderDetails
import com.iti4.retailhub.models.Review
import com.iti4.retailhub.modelsdata.PlaceLocation
import com.iti4.retailhub.type.CustomerInput
import com.iti4.retailhub.type.MetafieldDeleteInput
import com.iti4.retailhub.userauthuntication.UserAuthunticationInterface
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val retrofitDataSource: RetrofitDataSource,
    private val userAuthuntication: UserAuthunticationInterface,
    private val UserLocalProfileData: UserLocalProfileDataInterface,
    private val reviewsDataStore: ReviewsDataStoreInterface
) : IRepository {

    override fun getProducts(query: String): Flow<List<HomeProducts>> {
        return remoteDataSource.getProducts(query)
    }

    override fun getBrands(): Flow<List<Brands>> {
        return remoteDataSource.getBrands()
    }


    override fun getMyBagProducts(query: String): Flow<List<CartProduct>> {
        return remoteDataSource.getMyBagProducts(query)
    }

    override fun deleteCustomerFavoritItem(id: MetafieldDeleteInput): Flow<String?> {
        return remoteDataSource.deleteCustomerFavoritItem(id)
    }

    override fun setLoginStatus(loginStatus: String) {
        UserLocalProfileData.setLoginStatus(loginStatus)
    }

    override fun getLoginStatus(): String? {
        return UserLocalProfileData.getLoginStatus()
    }

    override fun deleteMyBagItem(query: String): Flow<DeleteDraftOrderMutation.DraftOrderDelete> {
        return remoteDataSource.deleteMyBagItem(query)
    }

    override fun updateMyBagItem(cartProduct: CartProduct): Flow<UpdateDraftOrderMutation.DraftOrderUpdate> {
        return remoteDataSource.updateMyBagItem(cartProduct)
    }

    override fun createStripePaymentIntent(paymentRequest: PaymentRequest): Flow<Response<ResponseBody>> {
        return retrofitDataSource.createStripePaymentIntent(paymentRequest)
    }

    override fun getLocationSuggestions(
        query: String
    ): Flow<Response<List<PlaceLocation>>> {
        return retrofitDataSource.getLocationSuggestions(query)
    }

    override fun getLocationGeocoding(
        lat: String,
        lon: String
    ): Flow<Response<com.iti4.retailhub.features.address.PlaceLocation>> {
        return retrofitDataSource.getLocationGeocoding(lat, lon)
    }

    override fun getProductTypesOfCollection(): Flow<List<Category>> {
        return remoteDataSource.getProductTypesOfCollection()
    }

    override fun getCustomerInfoById(id: String): Flow<GetCustomerByIdQuery.Customer> {
        return remoteDataSource.getCustomerInfoById(id)
    }

    override fun createCheckoutDraftOrder(draftOrderInputModel: DraftOrderInputModel): Flow<CreateDraftOrderMutation.DraftOrderCreate> {
        return remoteDataSource.createCheckoutDraftOrder(draftOrderInputModel)
    }

    override fun emailCheckoutDraftOrder(draftOrderId: String): Flow<DraftOrderInvoiceSendMutation.DraftOrder> {
        return remoteDataSource.emailCheckoutDraftOrder(draftOrderId)
    }

    override fun completeCheckoutDraftOrder(draftOrderId: String): Flow<CompleteDraftOrderMutation.DraftOrder> {
        return remoteDataSource.completeCheckoutDraftOrder(draftOrderId)
    }

    override fun markOrderAsPaid(orderId: String): Flow<MarkAsPaidMutation.OrderMarkAsPaid> {
        return remoteDataSource.markOrderAsPaid(orderId)
    }

    override fun createUser(input: CustomerInput): Flow<CreateCustomerMutation.CustomerCreate> {
        return remoteDataSource.createUser(input)
    }

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): AuthResult? {
        return userAuthuntication.createUserWithEmailAndPassword(email, password)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult? {
        return userAuthuntication.signInWithEmailAndPassword(email, password)
    }

    override  fun loginOut(): Boolean {
        val out= userAuthuntication.loginOut()
        return out
    }

    override suspend fun sendEmailVerification(user: FirebaseUser): Boolean {
        return userAuthuntication.sendEmailVerification(user)
    }

    override fun addUserName(name: String): Int {
        return UserLocalProfileData.addUserName(name)
    }

    override fun addUserData(userID: String): Int {
        return UserLocalProfileData.addUserData(userID)
    }

    override fun getUserProfileData(): String {
        return UserLocalProfileData.getUserProfileData()
    }

    override fun deleteUserData() {
        UserLocalProfileData.deleteUserData()
    }

    override fun addUserShopLocalId(id: String?) {
        UserLocalProfileData.addUserShopLocalId(id)
    }

    override fun getUserShopLocalId(): String? {
        return UserLocalProfileData.getUserShopLocalId()
    }

    override fun getCustomerIdByEmail(email: String): Flow<CustomerEmailSearchQuery.Customers> {
        return remoteDataSource.getCustomerIdByEmail(email)
    }

    override fun getProductDetails(id: String): Flow<ProductDetailsQuery.OnProduct?> {
        return remoteDataSource.getProductDetails(id)
    }

    override fun addReview(review: Review) {
        reviewsDataStore.addReview(review)
    }

    override fun getAllReviews(reviewsNumbers: Int): List<Review> {
        return reviewsDataStore.getAllReviews(reviewsNumbers)
    }

    override fun insertMyBagItem(
        varientId: String,
        customerId: String
    ): Flow<CreateDraftOrderMutation.DraftOrderCreate> {
        return remoteDataSource.insertMyBagItem(varientId, customerId)
    }


    override fun getAddressesById(customerId: String): Flow<GetAddressesByIdQuery.Customer> {
        return remoteDataSource.getAddressesById(customerId)
    }

    override fun getDefaultAddress(customerId: String): Flow<GetAddressesDefaultIdQuery.Customer> {
        return remoteDataSource.getDefaultAddress(customerId)
    }


    override fun getDraftOrdersByCustomer(customerId: String): Flow<GetDraftOrdersByCustomerQuery.DraftOrders> {
        return remoteDataSource.getDraftOrdersByCustomer(customerId)

    }

    override fun saveProductToFavotes(input: CustomerInput): Flow<UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate> {
        return remoteDataSource.saveProductToFavotes(input)
    }

    override fun getCustomerFavoritesoById(
        id: String,
        namespace: String
    ): Flow<GetCustomerFavoritesQuery.Customer> {
        return remoteDataSource.getCustomerFavoritesoById(id, namespace)

    }

    override fun updateCustomerAddress(
        customerId: String,
        address: List<CustomerAddressV2>
    ): Flow<UpdateCustomerAddressesMutation.CustomerUpdate> {
        return remoteDataSource.updateCustomerAddress(customerId, address)
    }


    override fun updateCustomerDefaultAddress(
        customerId: String,
        addressId: String
    ): Flow<CustomerUpdateDefaultAddressMutation.Customer> {
        return remoteDataSource.updateCustomerDefaultAddress(customerId, addressId)
    }

    override fun getCurrencyRates(
    ): Flow<Response<CurrencyResponse>> {
        return retrofitDataSource.getCurrencyRates()
    }

    override fun saveConversionRates(conversion_rates: Map<String, Double>) {
        UserLocalProfileData.saveConversionRates(conversion_rates)
    }

    override fun getConversionRates(code: CountryCodes): Double {
        val map = UserLocalProfileData.getConversionRates()
        var returnAmount = 1.0
        if (map != null) {
            returnAmount = when (code) {
                CountryCodes.EGP -> map["EGP"]!!
                CountryCodes.USD -> map["USD"]!!
                CountryCodes.EUR -> map["EUR"]!!
                CountryCodes.AED -> map["AED"]!!
            }
        }
        return returnAmount
    }

    override fun setCurrencyCode(currencyCode: CountryCodes) {
        UserLocalProfileData.setCurrencyCode(currencyCode)
    }

    override fun getCurrencyCode(): CountryCodes {
        return UserLocalProfileData.getCurrencyCode()
    }

    override fun getFirstTime(): Boolean {
        return UserLocalProfileData.getFirstTime()
    }

    override fun setFirstTime() {
        UserLocalProfileData.setFirstTime()
    }

    override fun getShouldIRefrechCurrency(): Boolean {
        return UserLocalProfileData.getShouldIRefrechCurrency()
    }

    override fun setRefrechCurrency() {
        UserLocalProfileData.setRefrechCurrency()
    }

    override fun getDiscounts(): Flow<List<Discount>> {
        return remoteDataSource.getDiscounts()
    }

    override fun setCustomerUsedDiscounts(
        customerId: String,
        discountCode: String
    ): Flow<AddTagsMutation.Node> {
        return remoteDataSource.setCustomerUsedDiscounts(customerId, discountCode)
    }

    override fun getCustomerUsedDiscounts(customerId: String): Flow<List<String>> {
        return remoteDataSource.getCustomerUsedDiscounts(customerId)
    }


    override fun getOrders(query: String): Flow<List<Order>> {
        return remoteDataSource.getOrders(query)
    }

    override fun getOrderDetails(id: String): Flow<OrderDetails> {
        return remoteDataSource.getOrderDetails(id)
    }
    override suspend fun signWithGoogle(idToken:String): FirebaseUser? {
        return userAuthuntication.signWithGoogle(idToken)
    }

}