package com.iti4.retailhub.features.favorits.viewmodel

import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.productdetails.viewmodel.ProductDetailsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class FavoritesViewModelTest{
    private lateinit var viewModel: FavoritesViewModel
    private val repository = mockk<IRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        every { repository.getUserShopLocalId() } returns ""
        Dispatchers.setMain(testDispatcher)
        viewModel = FavoritesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getFavorites emits loading and success state`() = runTest {
        coEvery { repository.getCustomerFavoritesoById(any(),any()) } returns flow {
            emit(mockk<GetCustomerFavoritesQuery.Customer>())
        }

        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.savedFavortes.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.getFavorites()
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
    fun `getFavorites emits error state on failure`() = runTest {
     coEvery { repository.getCustomerFavoritesoById(any(),any()) } returns flow {
            throw Exception("can't retrieve products details")
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.savedFavortes.collect {
                collectedState.add(it)
                if (it is ApiState.Error)
                    completionSignal.complete(Unit)
            }
        }
        viewModel.getFavorites()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
        job.cancel()
        }

    //---------------------
    @ExperimentalCoroutinesApi
    @Test
    fun `deleteFavorites emits loading and success state`() = runTest {
        // Mock repository to return a successful flow
        coEvery { repository.deleteCustomerFavoritItem(any()) } returns flow {
            emit("dsa") // Emitting a successful response
        }

        // Override Dispatchers.IO with the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Variable to collect emitted states
        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()

        // Start collecting the flow from the ViewModel
        val job = launch {
            viewModel.deletedFavortes.collect { state ->
                collectedState.add(state)
                if (state is ApiState.Success<*> || state is ApiState.Error) {
                    completionSignal.complete(Unit)
                }
            }
        }

        // Trigger the function under test
        viewModel.deleteFavorites("test_id")

        // Wait for the flow to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Await the completion signal
        completionSignal.await()

        // Assertions
        assertTrue(collectedState[0] is ApiState.Loading)
        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        // Clean up the job
        job.cancel()

        // Reset Dispatchers.IO back to default after the test
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteFavorites emits error state on failure`() = runTest {
        coEvery { repository.deleteCustomerFavoritItem(any()) } returns flow {
            throw Exception("can't retrieve products details")
        }
            var collectedState = mutableListOf<ApiState>()
            val completionSignal = CompletableDeferred<Unit>()
            val job = launch {
                viewModel.deletedFavortes.collect {
                    collectedState.add(it)
                    if (it is ApiState.Error)
                        completionSignal.complete(Unit)
                }
            }
            viewModel.deleteFavorites("test_id")
            testDispatcher.scheduler.advanceUntilIdle()
            completionSignal.await()
            assertTrue(collectedState[0] is ApiState.Loading)
            assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
            job.cancel()
        }
    }
