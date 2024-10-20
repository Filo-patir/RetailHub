package com.iti4.retailhub.features.productSearch.viewmodel

import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.Repository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.models.HomeProducts
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class ProductSearchViewModelTest{

    private lateinit var viewModel:ProductSearchViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<IRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductSearchViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchProducts emits loading and success state`() = runTest {

        coEvery { repository.getProducts(any()) } returns flow {
            emit(
                listOf(
                    mockk<HomeProducts>()
                )
            )
        }

        var collectedState = mutableListOf<ApiState>()

        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.searchList.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.searchProducts("test query")
        testDispatcher.scheduler.advanceUntilIdle()

        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)

        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as List<HomeProducts>
        assertThat(retrivedData.size, `is`(1))
        job.cancel()
    }
    @Test
    fun `searchProducts emits error state on failure`() = runTest {
            coEvery { repository.getProducts(any()) } returns flow {
                throw Exception("can't retrieve products")
            }
            var collectedState = mutableListOf<ApiState>()
            val completionSignal = CompletableDeferred<Unit>()
            val job = launch {
                viewModel.searchList.collect {
                    collectedState.add(it)
                    if (it is ApiState.Error)
                        completionSignal.complete(Unit)
                }
            }
            viewModel.searchProducts("test query")
            testDispatcher.scheduler.advanceUntilIdle()
            completionSignal.await()
            assertTrue(collectedState[0] is ApiState.Loading)
            assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
            job.cancel()
        }

}