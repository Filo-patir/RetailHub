package com.iti4.retailhub.features.checkout

import com.iti4.retailhub.AddTagsMutation
import com.iti4.retailhub.CompleteDraftOrderMutation
import com.iti4.retailhub.CreateDraftOrderMutation
import com.iti4.retailhub.DeleteDraftOrderMutation
import com.iti4.retailhub.DraftOrderInvoiceSendMutation
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.GetCustomerByIdQuery
import com.iti4.retailhub.MarkAsPaidMutation
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.models.CustomerInputModel
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.DiscountInput
import com.iti4.retailhub.models.DraftOrderInputModel
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CheckoutViewModel
    private val repository = mockk<IRepository>()


    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { repository.getUserShopLocalId() } returns "123"
        Dispatchers.setMain(testDispatcher)
        viewModel = CheckoutViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `getCustomerData, valid customer data, emits success`() = runTest {
        val customer =
            GetCustomerByIdQuery.Customer("fname1", "fname2", "test@gmail.com", "123123123")
        coEvery { repository.getCustomerInfoById(any()) } returns flowOf(customer)
        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.customerResponse.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>) completionSignal.complete(Unit)
            }
        }
        viewModel.getCustomerData()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assert(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val successData = collectedState[1] as ApiState.Success<*>
        val retrievedCustomer = successData.data as GetCustomerByIdQuery.Customer
        assertThat(retrievedCustomer.email, `is`("test@gmail.com"))
        job.cancel()

    }

    @Test
    fun `getCustomerData, invalid customer data, emits error`() = runTest {
        val customer = GetCustomerByIdQuery.Customer(null, null, null, null)

        coEvery { repository.getCustomerInfoById(any()) } returns flowOf(customer)
        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.customerResponse.collect {
                println("THE IT IS " + it)
                collectedState.add(it)
                if (it is ApiState.Error) completionSignal.complete(Unit)
            }
        }
        assertTrue(collectedState.isEmpty())
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(collectedState[0] is ApiState.Loading)

        viewModel.getCustomerData()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()

    }

    @Test
    fun `getCustomerData, network error ,  data, emits error`() = runTest {


        coEvery { repository.getCustomerInfoById(any()) } returns flow {
            throw Exception("Network Error")
        }
        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.customerResponse.collect {
                collectedState.add(it)
                if (it is ApiState.Error) completionSignal.complete(Unit)
            }
        }
        viewModel.getCustomerData()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assert(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
    }

    @Test
    fun `getDefaultAddress, Valid Id,it (returns address) AND (api state is success)`() = runTest {
        coEvery { repository.getDefaultAddress(any()) } returns flowOf(mockk<GetAddressesDefaultIdQuery.Customer>())
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.addressesState.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.getDefaultAddress()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))
        val result = collectedState[1] as ApiState.Success<*>
        assertThat(result.data, `is`(instanceOf(GetAddressesDefaultIdQuery.Customer::class.java)))

        job.cancel()
    }


    @Test
    fun `getDefaultAddress, Invalid Id,it (returns exception) AND (api state is error)`() =
        runTest {
            coEvery { repository.getDefaultAddress(any()) } returns
                    flow {
                        throw Exception("Network or data not found exception")
                    }
            var collectedState = mutableListOf<ApiState>()
            val completionSignal = CompletableDeferred<Unit>()
            val job = launch {
                viewModel.addressesState.collect {
                    collectedState.add(it)
                    if (it is ApiState.Error)
                        completionSignal.complete(Unit)
                }
            }

            viewModel.getDefaultAddress()
            testDispatcher.scheduler.advanceUntilIdle()
            completionSignal.await()
            assertTrue(collectedState[0] is ApiState.Loading)
            assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))

            job.cancel()
        }

    @Test
    fun `deleteCartItems, listOfCartProducts,  No Errors Occurs and Repo Function called with the correct paramters`() =
        runTest {
            val listOfCartProduct = listOf(
                mockk<CartProduct>() {
                    every { draftOrderId } returns "order1"
                },
                mockk<CartProduct>() {
                    every { draftOrderId } returns "order2"
                }
            )
            every { repository.deleteMyBagItem(any()) } answers {
                val draftOrderId = firstArg<String>()
                flowOf(mockk<DeleteDraftOrderMutation.DraftOrderDelete>() {
                    every { deletedId } returns draftOrderId
                })
            }

            viewModel.deleteCartItems(listOfCartProduct)

            coVerify(exactly = 1) { repository.deleteMyBagItem("order1") }
            coVerify(exactly = 1) { repository.deleteMyBagItem("order2") }
            coVerify(exactly = 0) { repository.deleteMyBagItem("nonExistentOrder") }
        }

    @Test
    fun `deleteCartItems, listOfCartProducts,  Empty List, Repo Never called `() =
        runTest {
            val listOfCartProduct = listOf<CartProduct>()
            every { repository.deleteMyBagItem(any()) } answers {
                val draftOrderId = firstArg<String>()
                flowOf(mockk<DeleteDraftOrderMutation.DraftOrderDelete>() {
                    every { deletedId } returns draftOrderId
                })
            }
            viewModel.deleteCartItems(listOfCartProduct)
            coVerify(exactly = 0) { repository.deleteMyBagItem(any()) }
        }

    @Test
    fun `deleteCartItems, listOfCartProducts, Wrong Id , Throw Error`() =
        runTest {
            val listOfCartProduct = listOf(
                mockk<CartProduct>() {
                    every { draftOrderId } returns "order1"
                },
                mockk<CartProduct>() {
                    every { draftOrderId } returns "order2"
                }
            )
            every { repository.deleteMyBagItem(any()) } throws Exception("Network Error")
            try {
                viewModel.deleteCartItems(listOfCartProduct)
            } catch (e: Exception) {
                assertEquals("Network Error", e.message)
            }
        }

    @Test
    fun `getDiscountInput, selectedDiscount is not null , Returns  DiscountInput `() =
        runTest {

            val selectedDiscount = mockk<Discount>()
            every { selectedDiscount.getDiscountAsDouble() } returns 1.0
            every { selectedDiscount.title } returns "test"
            viewModel.selectedDiscount = selectedDiscount
            val result = viewModel.getDiscountInput()

            assertNotNull(result)
            assertEquals(1.0, result?.value)
            assertEquals("test", result?.valueType)
            assertThat(result, `is`(instanceOf(DiscountInput::class.java)))

        }

    @Test
    fun `getDiscountInput, selectedDiscount null , Returns  null `() =
        runTest {
            viewModel.selectedDiscount = null
            val result = viewModel.getDiscountInput()
            assertNull(result)
        }

    @Test
    fun `getAddressInputModel returns AddressInputModel when checkoutAddress is provided`() {

        val checkoutAddress = mockk<CustomerAddressV2> {
            every { name } returns "ahmed"
            every { phone } returns "123871231"
            every { address1 } returns "addr1"
            every { address2 } returns "addr2"
            every { city } returns "Alexandria"
            every { country } returns "Cairo"
        }
        val customerInputModel = CustomerInputModel("123", "john@example.com")

        val result = viewModel.getAddressInputModel(checkoutAddress, null, customerInputModel)

        assertNotNull(result)
        assertEquals(result.address1, "addr1")
        assertEquals(result.city, "Alexandria")
        assertEquals(customerInputModel.firstName, "ahmed")
        assertEquals(customerInputModel.phone, "123871231")
    }

    @Test
    fun `getAddressInputModel returns AddressInputModel when checkoutDefaultAddress is provided`() {

        val checkoutDefaultAddress = mockk<GetAddressesDefaultIdQuery.DefaultAddress> {
            every { name } returns "ahmed"
            every { phone } returns "123871231"
            every { address1 } returns "addr1"
            every { address2 } returns "addr2"
            every { city } returns "Alexandria"
            every { country } returns "Cairo"
        }
        val customerInputModel = CustomerInputModel("123", "ahmed@gmail.com")

        val result =
            viewModel.getAddressInputModel(null, checkoutDefaultAddress, customerInputModel)

        assertNotNull(result)
        assertEquals(result.address1, "addr1")
        assertEquals(result.city, "Alexandria")
        assertEquals(customerInputModel.firstName, "ahmed")
        assertEquals(customerInputModel.phone, "123871231")
    }

    @Test
    fun `getAddressInputModel throws exception when required fields are null`() {
        val checkoutAddress = mockk<CustomerAddressV2> {
            every { name } returns "ahmed"
            every { phone } returns "123871231"
            every { address1 } returns null
            every { address2 } returns "addr2"
            every { city } returns "Alexandria"
            every { country } returns "Cairo"
        }
        val customerInputModel = CustomerInputModel("123", "ahmed@gmail.com")
        assertThrows(NullPointerException::class.java) {
            viewModel.getAddressInputModel(checkoutAddress, null, customerInputModel)
        }
    }


    @Test
    fun `createDraftOrderInputModel with valid checkoutAddress should return correct DraftOrderInputModel`() {

        val listOfCartProduct = listOf(mockk<CartProduct>() {
            every { itemQuantity } returns 4
            every { itemId } returns "hi"
        })
        viewModel.customerEmail = "ahmed@gmail.com"
        val checkoutAddress = mockk<CustomerAddressV2> {
            every { name } returns "ahmed"
            every { phone } returns "123871231"
            every { address1 } returns "addr1"
            every { address2 } returns "addr2"
            every { city } returns "Alexandria"
            every { country } returns "Cairo"
        }
        val draftOrderInputModel = viewModel.createDraftOrderInputModel(
            listOfCartProduct,
            checkoutAddress,
            null
        )
        assertNotNull(draftOrderInputModel)
        assertEquals("123", draftOrderInputModel.customer!!.id)
        assertEquals("ahmed@gmail.com", draftOrderInputModel.email)
    }


    @Test
    fun `setCustomerUsedDiscount, discount is set successfully`() = runTest {
        val discountTitle = "test"
        viewModel.selectedDiscount = mockk {
            every { title } returns discountTitle
        }
        coEvery {
            repository.setCustomerUsedDiscounts(
                any(),
                any()
            )
        } returns flowOf(mockk<AddTagsMutation.Node>())
        viewModel.setCustomerUsedDiscount()
        coVerify { repository.setCustomerUsedDiscounts(viewModel.customerId, discountTitle) }
    }

    @Test
    fun `setCustomerUsedDiscount, handles exception when discount setting fails`() = runTest {
        val discountTitle = "test"
        viewModel.selectedDiscount = mockk {
            every { title } returns discountTitle
        }

        coEvery {
            repository.setCustomerUsedDiscounts(
                any(),
                any()
            )
        } throws Exception("Network error")
        try {
            viewModel.setCustomerUsedDiscount()
        } catch (e: Exception) {
            assert(e.message == "Network error")
        }
        coVerify { repository.setCustomerUsedDiscounts(viewModel.customerId, discountTitle) }
    }

    @Test
    fun `markAsPaidIfCard, calls markOrderAsPaid when isCard is true`() = runTest {
        val orderId = "testOrderId"
        coEvery { repository.markOrderAsPaid(orderId) } returns flowOf(mockk<MarkAsPaidMutation.OrderMarkAsPaid>())
        viewModel.markAsPaidIfCard(orderId, true)
        coVerify { repository.markOrderAsPaid(orderId) }
    }

    @Test
    fun `finalizeOrder emits success when deleteMyBagItem succeeds`() = runTest {
        val orderId = "testOrderId"
        viewModel.markAsPaidIfCard(orderId, false)
        coVerify(exactly = 0) { repository.markOrderAsPaid(orderId) }
    }


    @Test
    fun `finalizeOrder, Valid Item Id  ,(api state is Success)`() = runTest {
        val draftOrderDeleteMock = mockk<DeleteDraftOrderMutation.DraftOrderDelete>()
        coEvery { repository.deleteMyBagItem(any()) } returns flow {
            emit(
                draftOrderDeleteMock
            )
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.checkoutDraftOrderCreated.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        assertTrue(collectedState.isEmpty())
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(collectedState[0] is ApiState.Loading)

        viewModel.finalizeOrder("fake id")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()

        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))
        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as DeleteDraftOrderMutation.DraftOrderDelete
        assertThat(
            retrivedData,
            `is`(instanceOf(DeleteDraftOrderMutation.DraftOrderDelete::class.java))
        )
        job.cancel()
    }

    @Test
    fun `finalizeOrder does not emit success when deleteMyBagItem fails`() = runTest {
        coEvery { repository.deleteMyBagItem(any()) } returns flow {
            throw Exception("Network error or Invalid Item Id")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.checkoutDraftOrderCreated.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.finalizeOrder("fake id")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        job.cancel()
    }

    @Test
    fun `completeCheckout should call finalizeOrder on successful checkout`() = runTest {

        every { repository.emailCheckoutDraftOrder(any()) } returns flow {
            emit(DraftOrderInvoiceSendMutation.DraftOrder("order"))
        }
        every { repository.completeCheckoutDraftOrder(any()) } returns flow {
            emit(
                CompleteDraftOrderMutation.DraftOrder(
                    "orderid",
                    mockk<CompleteDraftOrderMutation.Order>() {
                        every { id } returns "id"
                    })
            )
        }
        every { repository.markOrderAsPaid(any()) } returns flow {
            emit(mockk<MarkAsPaidMutation.OrderMarkAsPaid>())
        }
        every { repository.deleteMyBagItem(any()) } returns flow {
            emit(mockk<DeleteDraftOrderMutation.DraftOrderDelete>())
        }

        viewModel.completeCheckout("id", true)
        coVerify(exactly = 1) { repository.emailCheckoutDraftOrder(any()) }
        coVerify(exactly = 1) { repository.completeCheckoutDraftOrder(any()) }
    }


    @Test
    fun `completeCheckout, will not call finalizeOrder on successful checkout and an exception is thrown`() =
        runTest {

            every { repository.emailCheckoutDraftOrder(any()) } returns flow {
                throw Exception("Network error")
            }
            every { repository.completeCheckoutDraftOrder(any()) } returns flow {
                emit(
                    CompleteDraftOrderMutation.DraftOrder(
                        "orderid",
                        mockk<CompleteDraftOrderMutation.Order>() {
                            every { id } returns "id"
                        })
                )
            }
            every { repository.markOrderAsPaid(any()) } returns flow {
                emit(mockk<MarkAsPaidMutation.OrderMarkAsPaid>())
            }
            every { repository.deleteMyBagItem(any()) } returns flow {
                emit(mockk<DeleteDraftOrderMutation.DraftOrderDelete>())
            }


            try {
                viewModel.completeCheckout("id", true)
            } catch (e: Exception) {
                assert(e.message == "Network error")
            }
            coVerify(exactly = 0) { viewModel.finalizeOrder(any()) }
        }

    @Test
    fun `handleCheckoutProcess calls no error state change  on success`() = runTest {

        val listOfCartProduct = listOf(mockk<CartProduct>() {
            every { itemQuantity } returns 4
            every { itemId } returns "123"
        })
        viewModel.customerEmail = "ahmed@gmail.com"
        val checkoutAddress = mockk<CustomerAddressV2> {
            every { name } returns "ahmed"
            every { phone } returns "123871231"
            every { address1 } returns "addr1"
            every { address2 } returns "addr2"
            every { city } returns "Alexandria"
            every { country } returns "Cairo"
        }
        val draftOrderInputModel = viewModel.createDraftOrderInputModel(
            listOfCartProduct,
            checkoutAddress,
            null
        )

        coEvery { repository.createCheckoutDraftOrder(draftOrderInputModel) } returns flow {
            emit(mockk<CreateDraftOrderMutation.DraftOrderCreate>() {
                every { draftOrder } returns mockk<CreateDraftOrderMutation.DraftOrder>() {
                    every { id } returns "123"
                }
            })
        }

        every { repository.emailCheckoutDraftOrder(any()) } returns flow {
            emit(DraftOrderInvoiceSendMutation.DraftOrder("123"))
        }
        every { repository.completeCheckoutDraftOrder(any()) } returns flow {
            emit(
                CompleteDraftOrderMutation.DraftOrder(
                    "123",
                    mockk<CompleteDraftOrderMutation.Order>() {
                        every { id } returns "123"
                    })
            )
        }
        every { repository.markOrderAsPaid(any()) } returns flow {
            emit(mockk<MarkAsPaidMutation.OrderMarkAsPaid>())
        }
        every { repository.deleteMyBagItem(any()) } returns flow {
            emit(mockk<DeleteDraftOrderMutation.DraftOrderDelete>())
        }
        viewModel.handleCheckoutProcess(draftOrderInputModel, isCard = true)
        val getData = viewModel.checkoutDraftOrderCreated
        assertThat(
            getData.value,
            `not`(instanceOf(ApiState.Error::class.java))
        )
    }

    @Test
    fun `handleCheckoutProcess, state changes to error `() = runTest {

        val listOfCartProduct = listOf(mockk<CartProduct>() {
            every { itemQuantity } returns 4
            every { itemId } returns "123"
        })
        viewModel.customerEmail = "ahmed@gmail.com"
        val checkoutAddress = mockk<CustomerAddressV2> {
            every { name } returns "ahmed"
            every { phone } returns "123871231"
            every { address1 } returns "addr1"
            every { address2 } returns "addr2"
            every { city } returns "Alexandria"
            every { country } returns "Cairo"
        }
        val draftOrderInputModel = viewModel.createDraftOrderInputModel(
            listOfCartProduct,
            checkoutAddress,
            null
        )

        coEvery { repository.createCheckoutDraftOrder(draftOrderInputModel) } returns flow {
            throw Exception("Network Error")
        }

        every { repository.emailCheckoutDraftOrder(any()) } returns flow {
            throw Exception("Network Error")
        }

        every { repository.completeCheckoutDraftOrder(any()) } returns flow {
            throw Exception("Network Error")
        }

        every { repository.markOrderAsPaid(any()) } returns flow {
            throw Exception("Network Error")
        }

        every { repository.deleteMyBagItem(any()) } returns flow {
            throw Exception("Network Error")
        }


        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.checkoutDraftOrderCreated.collect {
                collectedState.add(it)
                if (it is ApiState.Error) completionSignal.complete(Unit)
            }
        }
        viewModel.handleCheckoutProcess(draftOrderInputModel, isCard = true)
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        val getData = viewModel.checkoutDraftOrderCreated

        assertThat(
            getData.value,
            `is`(instanceOf(ApiState.Error::class.java))
        )
        job.cancel()
    }

    @Test
    fun `createCheckoutDraftOrder should emit success state on successful checkout`() = runTest {
        val listOfCartProduct =
            listOf(CartProduct("123", "123", 4, 4, "123", "123", "123", "123", "123"))
        val checkoutAddress = GetAddressesDefaultIdQuery.DefaultAddress(
            id = "sfasd",
            name = "ahmed",
            phone = "123871231",
            address1 = "addr1",
            address2 = "addr2",
            city = "Alexandria",
            country = "Cairo"
        )
        viewModel.customerEmail = "ahmed@gmail.com"
        val draftOrderInputModel = mockk<DraftOrderInputModel>() // Mock your DraftOrderInputModel
        coEvery {
            viewModel.createDraftOrderInputModel(
                listOfCartProduct,
                null,
                checkoutAddress
            )
        } returns draftOrderInputModel
        coEvery { viewModel.deleteCartItems(listOfCartProduct) } just Runs
        coEvery { viewModel.handleCheckoutProcess(draftOrderInputModel, true) } just Runs
        val emittedStates = mutableListOf<ApiState>()
        val job = launch {
            viewModel.checkoutDraftOrderCreated.collect { state ->
                emittedStates.add(state)
            }
        }
        viewModel.createCheckoutDraftOrder(listOfCartProduct, true, null, checkoutAddress)
        withTimeout(2000) {
            while (emittedStates.isEmpty()) {
                delay(50)
            }
        }
        assertTrue(emittedStates.isNotEmpty())
        job.cancel()
    }

}

