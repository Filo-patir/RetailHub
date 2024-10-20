package com.iti4.retailhub.datastorage


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
import com.iti4.retailhub.datastorage.userlocalprofiledata.UserLocalProfileData
import com.iti4.retailhub.features.address.PlaceLocation
import com.iti4.retailhub.features.summary.PaymentRequest
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.DraftOrderInputModel
import com.iti4.retailhub.models.HomeProducts
import com.iti4.retailhub.models.Order
import com.iti4.retailhub.models.OrderDetails
import com.iti4.retailhub.models.Review
import com.iti4.retailhub.type.CustomerInput
import com.iti4.retailhub.userauthuntication.UserAuthunticationInterface
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class RepositoryTest {

    private lateinit var repository: Repository
    private val remoteDataSource: RemoteDataSource = mockk()
    val retrofitDataSource: RetrofitDataSource= mockk()
    val userAuthuntication: UserAuthunticationInterface= mockk()
    val UserLocalProfileData: UserLocalProfileData = mockk(relaxed = true)
    val reviewsDataStore: ReviewsDataStoreInterface= mockk()
    @Before
    fun setUp() {
        repository = Repository(remoteDataSource,retrofitDataSource,userAuthuntication,UserLocalProfileData,reviewsDataStore)
    }

    @Test
    fun `createUser success`() = runTest {
        val input = mockk<CustomerInput>()
        val customerCreate = mockk<CreateCustomerMutation.CustomerCreate>()

        coEvery { remoteDataSource.createUser(input) } returns flow {
            emit(customerCreate)
        }

        val result = repository.createUser(input)

        result.collect { response ->
            assertEquals(customerCreate, response)
        }

        // Verify that createUser was called on the remoteDataSource
        coVerify { remoteDataSource.createUser(input) }
    }

    @Test
    fun `createUser failure`() = runTest {

        val input = mockk<CustomerInput>()

        val exception = Exception("Failed to create user")
        coEvery { remoteDataSource.createUser(input) } returns flow {
            throw exception
        }

        try {
            repository.createUser(input).collect {

            }
            fail("Exception was expected")
        } catch (e: Exception) {
            assertEquals("Failed to create user", e.message)
        }

        // Verify that createUser was called on the remoteDataSource
        coVerify { remoteDataSource.createUser(input) }
    }

    @Test
    fun `addUserShopLocalId calls UserLocalProfileData addUserShopLocalId`() {
        // Given
        val shopId = "shop123"

        // When
        repository.addUserShopLocalId(shopId)

        // Then
        verify { UserLocalProfileData.addUserShopLocalId(shopId) }
    }

    @Test
    fun `getUserShopLocalId returns correct id`() {
        // Given
        val expectedShopId = "shop123"
        every { UserLocalProfileData.getUserShopLocalId() } returns expectedShopId

        // When
        val shopId = repository.getUserShopLocalId()

        // Then
        assertEquals(expectedShopId, shopId)
        verify { UserLocalProfileData.getUserShopLocalId() }
    }
    @Test
    fun `addUserShopLocalId should call UserLocalProfileData addUserShopLocalId with correct id`() {
        // Given
        val shopId = "shop123"

        // When
        repository.addUserShopLocalId(shopId)

        // Then
        verify { UserLocalProfileData.addUserShopLocalId(shopId) }
        confirmVerified(UserLocalProfileData)
    }

    @Test
    fun `addUserShopLocalId should handle null id gracefully`() {
        // Given
        val shopId: String? = null

        // When
        repository.addUserShopLocalId(shopId)

        // Then
        verify { UserLocalProfileData.addUserShopLocalId(shopId) }
        confirmVerified(UserLocalProfileData)
    }

    @Test
    fun `getProducts returns correct products`() = runBlocking {
        val query = "test"
        val expectedProducts = listOf<HomeProducts>() // Populate with expected data

        coEvery { remoteDataSource.getProducts(query) } returns flow { emit(expectedProducts) }

        val actualProducts = repository.getProducts(query)
        actualProducts.collect {
            assertEquals(expectedProducts, it)
        }
    }

    @Test
    fun `getBrands returns correct brands`() = runBlocking {
        val expectedBrands = listOf<Brands>() // Populate with expected data

        coEvery { remoteDataSource.getBrands() } returns flow { emit(expectedBrands) }

        val actualBrands = repository.getBrands()
        actualBrands.collect {
            assertEquals(expectedBrands, it)
        }
    }

    @Test
    fun `getProductTypesOfCollection returns correct categories`() = runBlocking {
        val expectedCategories = listOf<Category>() // Populate with expected data

        coEvery { remoteDataSource.getProductTypesOfCollection() } returns flow { emit(expectedCategories) }

        val actualCategories = repository.getProductTypesOfCollection()
        actualCategories.collect {
            assertEquals(expectedCategories, it)
        }
    }

    @Test
    fun `getOrders returns correct orders`() = runBlocking {
        val query = "test"
        val expectedOrders = listOf<Order>() // Populate with expected data

        coEvery { remoteDataSource.getOrders(query) } returns flow { emit(expectedOrders) }

        val actualOrders = repository.getOrders(query)
        actualOrders.collect {
            assertEquals(expectedOrders, it)
        }
    }

    @Test
    fun `getOrderDetails returns correct order details`() = runBlocking {
        val orderId = "123"
        val expectedOrderDetails = mockk<OrderDetails>()

        coEvery { remoteDataSource.getOrderDetails(orderId) } returns flow { emit(expectedOrderDetails) }

        val actualOrderDetails = repository.getOrderDetails(orderId)
        actualOrderDetails.collect {
            assertEquals(expectedOrderDetails, it)
        }
    }

    @Test
    fun `getMyBagProducts returns correct cart products`() = runBlocking {
        val query = "test"
        val expectedCartProducts = listOf<CartProduct>() // Populate with expected data

        coEvery { remoteDataSource.getMyBagProducts(query) } returns flow { emit(expectedCartProducts) }

        val actualCartProducts = repository.getMyBagProducts(query)
        actualCartProducts.collect {
            assertEquals(expectedCartProducts, it)
        }
    }

    @Test
    fun `deleteMyBagItem deletes correctly`() = runBlocking {
        val query = "test_query"
        val expectedResponse = mockk<DeleteDraftOrderMutation.DraftOrderDelete>()

        coEvery { remoteDataSource.deleteMyBagItem(query) } returns flow { emit(expectedResponse) }

        val actualResponse = repository.deleteMyBagItem(query)
        actualResponse.collect {
            assertEquals(expectedResponse, it)
        }
    }

    @Test
    fun `updateMyBagItem updates correctly`() = runBlocking {
        val cartProduct = mockk<CartProduct>()
        val expectedUpdateResponse = mockk<UpdateDraftOrderMutation.DraftOrderUpdate>()

        coEvery { remoteDataSource.updateMyBagItem(cartProduct) } returns flow { emit(expectedUpdateResponse) }

        val actualUpdateResponse = repository.updateMyBagItem(cartProduct)
        actualUpdateResponse.collect {
            assertEquals(expectedUpdateResponse, it)
        }
    }

    @Test
    fun `createStripePaymentIntent returns correct response`() = runBlocking {
        val paymentRequest = mockk<PaymentRequest>()
        val expectedResponse = Response.success<ResponseBody>(mockk())

        coEvery { retrofitDataSource.createStripePaymentIntent(paymentRequest) } returns flow { emit(expectedResponse) }

        val actualResponse = repository.createStripePaymentIntent(paymentRequest)
        actualResponse.collect {
            assertEquals(expectedResponse, it)
        }
    }

    @Test
    fun `getLocationSuggestions returns correct suggestions`() = runBlocking {
        val query = "test"
        val expectedSuggestions = Response.success(listOf<com.iti4.retailhub.modelsdata.PlaceLocation>()) // Populate with expected data

        coEvery { retrofitDataSource.getLocationSuggestions(query) } returns flow { emit(expectedSuggestions) }

        val actualSuggestions = repository.getLocationSuggestions(query)
        actualSuggestions.collect {
            assertEquals(expectedSuggestions, it)
        }
    }

    @Test
    fun `getLocationGeocoding returns correct location`() = runBlocking {
        val lat = "12.34"
        val lon = "56.78"
        val expectedLocation = Response.success(mockk<PlaceLocation>())

        coEvery { retrofitDataSource.getLocationGeocoding(lat, lon) } returns flow { emit(expectedLocation) }

        val actualLocation = repository.getLocationGeocoding(lat, lon)
        actualLocation.collect {
            assertEquals(expectedLocation, it)
        }
    }

    @Test
    fun `getCustomerInfoById returns correct customer info`() = runBlocking {
        val customerId = "123"
        val expectedCustomer = mockk<GetCustomerByIdQuery.Customer>()

        coEvery { remoteDataSource.getCustomerInfoById(customerId) } returns flow { emit(expectedCustomer) }

        val actualCustomer = repository.getCustomerInfoById(customerId)
        actualCustomer.collect {
            assertEquals(expectedCustomer, it)
        }
    }

    @Test
    fun `createCheckoutDraftOrder returns correct draft order`() = runBlocking {
        val draftOrderInput = mockk<DraftOrderInputModel>()
        val expectedDraftOrder = mockk<CreateDraftOrderMutation.DraftOrderCreate>()

        coEvery { remoteDataSource.createCheckoutDraftOrder(draftOrderInput) } returns flow { emit(expectedDraftOrder) }

        val actualDraftOrder = repository.createCheckoutDraftOrder(draftOrderInput)
        actualDraftOrder.collect {
            assertEquals(expectedDraftOrder, it)
        }
    }

    @Test
    fun `emailCheckoutDraftOrder sends email correctly`() = runBlocking {
        val draftOrderId = "order_123"
        val expectedResponse = mockk<DraftOrderInvoiceSendMutation.DraftOrder>()

        coEvery { remoteDataSource.emailCheckoutDraftOrder(draftOrderId) } returns flow { emit(expectedResponse) }

        val actualResponse = repository.emailCheckoutDraftOrder(draftOrderId)
        actualResponse.collect {
            assertEquals(expectedResponse, it)
        }
    }

    @Test
    fun `completeCheckoutDraftOrder completes order correctly`() = runBlocking {
        val draftOrderId = "order_123"
        val expectedCompletedOrder = mockk<CompleteDraftOrderMutation.DraftOrder>()

        coEvery { remoteDataSource.completeCheckoutDraftOrder(draftOrderId) } returns flow { emit(expectedCompletedOrder) }

        val actualCompletedOrder = repository.completeCheckoutDraftOrder(draftOrderId)
        actualCompletedOrder.collect {
            assertEquals(expectedCompletedOrder, it)
        }
    }

    @Test
    fun `markOrderAsPaid marks order correctly`() = runBlocking {
        val orderId = "order_123"
        val expectedMarkedOrder = mockk<MarkAsPaidMutation.OrderMarkAsPaid>()

        coEvery { remoteDataSource.markOrderAsPaid(orderId) } returns flow { emit(expectedMarkedOrder) }

        val actualMarkedOrder = repository.markOrderAsPaid(orderId)
        actualMarkedOrder.collect {
            assertEquals(expectedMarkedOrder, it)
        }
    }

    @Test
    fun `createUser returns correct user creation result`() = runBlocking {
        val customerInput = CustomerInput(/* Initialize with test data */)
        val expectedUserCreation = mockk<CreateCustomerMutation.CustomerCreate>()

        coEvery { remoteDataSource.createUser(customerInput) } returns flow { emit(expectedUserCreation) }

        val actualUserCreation = repository.createUser(customerInput)
        actualUserCreation.collect {
            assertEquals(expectedUserCreation, it)
        }
    }

    @Test
    fun `insertMyBagItem returns correct draft order`() = runBlocking {
        val variantId = "variant_123"
        val customerId = "customer_123"
        val expectedDraftOrder = mockk<CreateDraftOrderMutation.DraftOrderCreate>()

        coEvery { remoteDataSource.insertMyBagItem(variantId, customerId) } returns flow { emit(expectedDraftOrder) }

        val actualDraftOrder = repository.insertMyBagItem(variantId, customerId)
        actualDraftOrder.collect {
            assertEquals(expectedDraftOrder, it)
        }
    }

    @Test
    fun `getAddressesById returns correct addresses`() = runBlocking {
        val customerId = "customer_123"
        val expectedAddresses = mockk<GetAddressesByIdQuery.Customer>()

        coEvery { remoteDataSource.getAddressesById(customerId) } returns flow { emit(expectedAddresses) }

        val actualAddresses = repository.getAddressesById(customerId)
        actualAddresses.collect {
            assertEquals(expectedAddresses, it)
        }
    }

    @Test
    fun `getDefaultAddress returns correct default address`() = runBlocking {
        val customerId = "customer_123"
        val expectedAddress = mockk<GetAddressesDefaultIdQuery.Customer>()

        coEvery { remoteDataSource.getDefaultAddress(customerId) } returns flow { emit(expectedAddress) }

        val actualAddress = repository.getDefaultAddress(customerId)
        actualAddress.collect {
            assertEquals(expectedAddress, it)
        }
    }

    @Test
    fun `getCustomerFavoritesById returns correct favorites`() = runBlocking {
        val customerId = "customer_123"
        val namespace = "namespace"
        val expectedFavorites = mockk<GetCustomerFavoritesQuery.Customer>()

        coEvery { remoteDataSource.getCustomerFavoritesoById(customerId, namespace) } returns flow { emit(expectedFavorites) }

        val actualFavorites = repository.getCustomerFavoritesoById(customerId, namespace)
        actualFavorites.collect {
            assertEquals(expectedFavorites, it)
        }
    }

    @Test
    fun `updateCustomerAddress updates correctly`() = runBlocking {
        val customerId = "customer_123"
        val addresses = listOf<CustomerAddressV2>() // Populate with test addresses
        val expectedUpdateResponse = mockk<UpdateCustomerAddressesMutation.CustomerUpdate>()

        coEvery { remoteDataSource.updateCustomerAddress(customerId, addresses) } returns flow { emit(expectedUpdateResponse) }

        val actualUpdateResponse = repository.updateCustomerAddress(customerId, addresses)
        actualUpdateResponse.collect {
            assertEquals(expectedUpdateResponse, it)
        }
    }

    @Test
    fun `updateCustomerDefaultAddress updates correctly`() = runBlocking {
        val customerId = "customer_123"
        val addressId = "address_123"
        val expectedResponse = mockk<CustomerUpdateDefaultAddressMutation.Customer>()

        coEvery { remoteDataSource.updateCustomerDefaultAddress(customerId, addressId) } returns flow { emit(expectedResponse) }

        val actualResponse = repository.updateCustomerDefaultAddress(customerId, addressId)
        actualResponse.collect {
            assertEquals(expectedResponse, it)
        }
    }



    @Test
    fun `getDiscounts returns correct discounts`() = runBlocking {
        val expectedDiscounts = listOf<Discount>() // Populate with expected data

        coEvery { remoteDataSource.getDiscounts() } returns flow { emit(expectedDiscounts) }

        val actualDiscounts = repository.getDiscounts()
        actualDiscounts.collect {
            assertEquals(expectedDiscounts, it)
        }
    }

    @Test
    fun `setCustomerUsedDiscounts sets discounts correctly`() = runBlocking {
        val customerId = "customer_123"
        val discountCode = "DISCOUNT_10"
        val expectedNode = mockk<AddTagsMutation.Node>()

        coEvery { remoteDataSource.setCustomerUsedDiscounts(customerId, discountCode) } returns flow { emit(expectedNode) }

        val actualNode = repository.setCustomerUsedDiscounts(customerId, discountCode)
        actualNode.collect {
            assertEquals(expectedNode, it)
        }
    }

    @Test
    fun `getCustomerUsedDiscounts returns correct discounts`() = runBlocking {
        val customerId = "customer_123"
        val expectedDiscounts = listOf("DISCOUNT_10", "DISCOUNT_20")

        coEvery { remoteDataSource.getCustomerUsedDiscounts(customerId) } returns flow { emit(expectedDiscounts) }

        val actualDiscounts = repository.getCustomerUsedDiscounts(customerId)
        actualDiscounts.collect {
            assertEquals(expectedDiscounts, it)
        }
    }


    @Test
    fun `test getDraftOrdersByCustomer returns correct data`() = runBlocking {
        // Arrange
        val customerId = "customer123"
        val expectedDraftOrders = mockk<GetDraftOrdersByCustomerQuery.DraftOrders>()
        coEvery { remoteDataSource.getDraftOrdersByCustomer(customerId) } returns flowOf(expectedDraftOrders)

        // Act
        val result = repository.getDraftOrdersByCustomer(customerId).toList()

        // Assert
        assert(result.contains(expectedDraftOrders))
        verify { remoteDataSource.getDraftOrdersByCustomer(customerId) }
    }

    @Test
    fun `test saveProductToFavotes returns correct data`() = runBlocking {
        // Arrange
        val input = CustomerInput(/* mock data */)
        val expectedUpdate = mockk<UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate>()
        coEvery { remoteDataSource.saveProductToFavotes(input) } returns flowOf(expectedUpdate)

        // Act
        val result = repository.saveProductToFavotes(input).toList()

        // Assert
        assert(result.contains(expectedUpdate))
        verify { remoteDataSource.saveProductToFavotes(input) }
    }

    @Test
    fun `test getCustomerIdByEmail returns correct data`() = runBlocking {
        // Arrange
        val email = "test@example.com"
        val expectedCustomers = mockk<CustomerEmailSearchQuery.Customers>()
        coEvery { remoteDataSource.getCustomerIdByEmail(email) } returns flowOf(expectedCustomers)

        // Act
        val result = repository.getCustomerIdByEmail(email).toList()

        // Assert
        assert(result.contains(expectedCustomers))
        verify { remoteDataSource.getCustomerIdByEmail(email) }
    }

    @Test
    fun `test getProductDetails returns correct data`() = runBlocking {
        // Arrange
        val productId = "product123"
        val expectedProductDetails = mockk<ProductDetailsQuery.OnProduct>()
        coEvery { remoteDataSource.getProductDetails(productId) } returns flowOf(expectedProductDetails)

        // Act
        val result = repository.getProductDetails(productId).toList()

        // Assert
        assert(result.contains(expectedProductDetails))
        verify { remoteDataSource.getProductDetails(productId) }
    }


    @Test
    fun `test getAllReviews returns correct number of reviews`() {
        // Arrange
        val reviewsNumbers = 5
        val expectedReviews = listOf(mockk<Review>(), mockk<Review>())
        every { reviewsDataStore.getAllReviews(reviewsNumbers) } returns expectedReviews

        // Act
        val result = repository.getAllReviews(reviewsNumbers)

        // Assert
        assert(result == expectedReviews)
        verify { reviewsDataStore.getAllReviews(reviewsNumbers) }
    }

}