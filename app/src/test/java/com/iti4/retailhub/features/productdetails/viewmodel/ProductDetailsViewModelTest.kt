package com.iti4.retailhub.features.productdetails.viewmodel
import com.iti4.retailhub.CreateDraftOrderMutation
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.GetDraftOrdersByCustomerQuery
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.UpdateCustomerFavoritesMetafieldsMutation
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.models.HomeProducts
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductDetailsViewModelTest {

    private lateinit var viewModel: ProductDetailsViewModel
    private val repository = mockk<IRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        every { repository.getUserShopLocalId() } returns ""
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductDetailsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProductDetails emits loading and success state`() = runTest {
        coEvery { repository.getProductDetails(any()) } returns flow {
            emit(mockk<ProductDetailsQuery.OnProduct>())
        }

        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.productDetails.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.getProductDetails("test query")
        testDispatcher.scheduler.advanceUntilIdle()

        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as ProductDetailsQuery.OnProduct
        assertThat(retrivedData, notNullValue())
        job.cancel()
    }
    @Test
    fun `getProductDetails emits error state on failure`() = runTest {
        coEvery { repository.getProductDetails(any()) } returns flow {
            throw Exception("can't retrieve products details")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.productDetails.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.getProductDetails("test query")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
    }


//---------------------
    @Test
    fun `getDraftOrdersByCustomer emits loading and success state`() = runTest {
        coEvery { repository.getDraftOrdersByCustomer(any()) } returns flow {
            emit(mockk<GetDraftOrdersByCustomerQuery.DraftOrders>())
        }

        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.customerDraftOrders.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.getDraftOrdersByCustomer("test_product_title")
        testDispatcher.scheduler.advanceUntilIdle()

        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as GetDraftOrdersByCustomerQuery.DraftOrders
        assertThat(retrivedData, notNullValue())
        job.cancel()
    }
    @Test
    fun `getDraftOrdersByCustomer emits error state on failure`() = runTest {
        coEvery { repository.getDraftOrdersByCustomer(any()) } returns flow {
            throw Exception("can't retrieve customer draft orders")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.customerDraftOrders.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.getDraftOrdersByCustomer("test_product_title")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
    }
    //--------------------------------------------------------------------

    @Test
    fun `addToCart emits loading and success state`() = runTest {
        coEvery { repository.insertMyBagItem(any(),any()) } returns flow {
            emit(mockk<CreateDraftOrderMutation.DraftOrderCreate>())
        }


        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.createDraftOrder.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.addToCart("test_product_title")
        testDispatcher.scheduler.advanceUntilIdle()

        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as CreateDraftOrderMutation.DraftOrderCreate
        assertThat(retrivedData, notNullValue())
        job.cancel()
    }
    @Test
    fun `addToCart emits error state on failure`() = runTest {
        coEvery { repository.insertMyBagItem(any(),any()) } returns flow {
            throw Exception("can't retrieve customer draft orders")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.createDraftOrder.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.addToCart("test_product_title")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
    }

    //---------------------
    @Test
    fun `saveToFavorites emits loading and success state`() = runTest {
        coEvery { repository.saveProductToFavotes(any()) } returns flow {
            emit(mockk<UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate>())
        }

        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.saveProductToFavortes.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.saveToFavorites("test_id","vid","title","url","0.5")
        testDispatcher.scheduler.advanceUntilIdle()

        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate
        assertThat(retrivedData, notNullValue())
        job.cancel()
    }
    @Test
    fun `saveToFavorites emits error state on failure`() = runTest {
        coEvery {  repository.saveProductToFavotes(any()) } returns flow {
            throw Exception("can't retrieve customer draft orders")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.saveProductToFavortes.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.saveToFavorites("test_id","vid","title","url","0.5")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
    }

    //---------------------
    @Test
    fun `searchProductInCustomerFavorites emits loading and success state`() = runTest {
        coEvery { repository.getCustomerFavoritesoById(any(),any()) } returns flow {
            emit(mockk<GetCustomerFavoritesQuery.Customer>())
        }

        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.productInFavorites.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.searchProductInCustomerFavorites("test_id")
        testDispatcher.scheduler.advanceUntilIdle()

        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as GetCustomerFavoritesQuery.Customer
        assertThat(retrivedData, notNullValue())
        job.cancel()
    }
    @Test
    fun `searchProductInCustomerFavorites emits error state on failure`() = runTest {
        coEvery {  repository.getCustomerFavoritesoById(any(),any()) } returns flow {
            throw Exception("can't retrieve customer draft orders")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.productInFavorites.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.searchProductInCustomerFavorites("test_id")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
    }
}
