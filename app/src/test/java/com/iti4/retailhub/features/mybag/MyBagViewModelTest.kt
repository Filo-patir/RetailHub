package com.iti4.retailhub.features.mybag

import com.iti4.retailhub.DeleteDraftOrderMutation
import com.iti4.retailhub.UpdateDraftOrderMutation
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.mybag.viewmodel.MyBagViewModel
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.CountryCodes
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.junit.After
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class MyBagViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MyBagViewModel
    private val repository = mockk<IRepository>()


    @Before
    fun setup() {
        every { repository.getUserShopLocalId() } returns ""
        Dispatchers.setMain(testDispatcher)
        viewModel = MyBagViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getMyBagProducts, ValidId,it (returns list) AND (api state is success)`() = runTest {
        // init repo function.

        coEvery { repository.getMyBagProducts(any()) } returns flow {
            emit(
                listOf(
                    mockk<CartProduct>(),
                    mockk<CartProduct>()
                )
            )
        }
        // to collect states of flow
        var collectedState = mutableListOf<ApiState>()
        // to block until collection happens
        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.myBagProductsState.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.getMyBagProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        // wait before assertion
        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)

        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as List<CartProduct>
        assertThat(retrivedData.size, `is`(2))
        job.cancel()
    }


    @Test
    fun `getMyBagProducts, Invalid Id ,it (returns throws an exception ) AND (api state is error)`() =
        runTest {
            coEvery { repository.getMyBagProducts(any()) } returns flow {
                throw Exception("Network error or Invalid User")
            }
            var collectedState = mutableListOf<ApiState>()
            val completionSignal = CompletableDeferred<Unit>()
            val job = launch {
                viewModel.myBagProductsState.collect {
                    collectedState.add(it)
                    if (it is ApiState.Error)
                        completionSignal.complete(Unit)
                }
            }
            viewModel.getMyBagProducts()
            testDispatcher.scheduler.advanceUntilIdle()
            completionSignal.await()
            assertTrue(collectedState[0] is ApiState.Loading)
            assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
            job.cancel()
        }

    @Test
    fun `deleteMyBagItem, Valid Item Id  ,(api state is Success)`() = runTest {
        val draftOrderDeleteMock = mockk<DeleteDraftOrderMutation.DraftOrderDelete>()
        coEvery { repository.deleteMyBagItem(any()) } returns flow {
            emit(
                draftOrderDeleteMock
            )
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.myBagProductsRemove.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        assertTrue(collectedState.isEmpty())
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(collectedState[0] is ApiState.Loading)

        viewModel.deleteMyBagItem("fake id")
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
    fun `deleteMyBagItem, Invalid Item Id  ,(api state is Error)`() = runTest {
        val draftOrderDeleteMock = mockk<DeleteDraftOrderMutation.DraftOrderDelete>()
        coEvery { repository.deleteMyBagItem(any()) } returns flow {
            throw Exception("Network error or Invalid Item Id")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.myBagProductsRemove.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.deleteMyBagItem("fake id")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        job.cancel()
    }


    @Test
    fun `updateMyBag, Valid CartProduct  ,(api state is Success)`() = runTest {
        val draftOrderUpdateMock = mockk<UpdateDraftOrderMutation.DraftOrderUpdate>()
        coEvery { repository.updateMyBagItem(any()) } returns flow {
            emit(
                draftOrderUpdateMock
            )
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.myBagProductsUpdate.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.updateMyBagItem(mockk<CartProduct>())
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))
        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as UpdateDraftOrderMutation.DraftOrderUpdate
        assertThat(
            retrivedData,
            `is`(instanceOf(UpdateDraftOrderMutation.DraftOrderUpdate::class.java))
        )
        job.cancel()  // Clean up
    }

    @Test
    fun `updateMyBag, InValid CartProduct  ,(api state is Error)`() = runTest {
        coEvery { repository.updateMyBagItem(any()) } returns flow {
            throw Exception("Network error or Invalid Item Id")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.myBagProductsUpdate.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.updateMyBagItem(mockk<CartProduct>())
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))

        job.cancel()  // Clean up
    }


    @Test
    fun `getConversionRates ,country code , returns correct value from shared preferences`() {
        val countryCode = CountryCodes.USD
        val expectedRate = 0.02059
        every { repository.getConversionRates(countryCode) } returns expectedRate
        val actualRate = viewModel.getConversionRates(countryCode)
        assertThat(actualRate, closeTo(expectedRate, 0.0));
        verify(exactly = 1) { repository.getConversionRates(countryCode) }
    }


    @Test
    fun `test getConversionRates returns default value when no rate exists`() {
        val countryCode = CountryCodes.USD
        val defaultRate = 1.0
        every { repository.getConversionRates(countryCode) } returns defaultRate
        val actualRate = viewModel.getConversionRates(countryCode)
        assertThat(actualRate, closeTo(defaultRate, 0.0));
        verify(exactly = 1) { repository.getConversionRates(countryCode) }
    }


    @Test
    fun `test getCurrencyCode returns correct value from shared preferences`() {
        val expectedCountryCode = CountryCodes.USD
        every { repository.getCurrencyCode() } returns expectedCountryCode
        val actualCountryCode = viewModel.getCurrencyCode()
        assertThat(actualCountryCode, `is`(expectedCountryCode))
        verify(exactly = 1) { repository.getCurrencyCode() }
    }

    @Test
    fun `test getCurrencyCode returns default value from shared preferences`() {
        every { repository.getCurrencyCode() } returns CountryCodes.EGP
        val actualCountryCode = viewModel.getCurrencyCode()
        assertThat(actualCountryCode, `is`(CountryCodes.EGP))
        verify(exactly = 1) { repository.getCurrencyCode() }
    }



}