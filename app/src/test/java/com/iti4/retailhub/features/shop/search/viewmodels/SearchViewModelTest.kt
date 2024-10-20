package com.iti4.retailhub.features.shop.search.viewmodels

import com.iti4.retailhub.features.shop.ShopViewModel


import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.HomeProducts
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.junit.After
import org.junit.Before
import org.junit.Test



@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SearchViewModel
    private val repository = mockk<IRepository>()


    @Before
    fun setup() {
        every { repository.getUserShopLocalId() } returns ""
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `searchProducts  (api state is success)`() = runTest {
        // init repo function.

        coEvery { repository.getProducts(any()) } returns flow {
            emit(
                listOf(
                    mockk<HomeProducts>(),
                    mockk<HomeProducts>()
                )
            )
        }
        // to collect states of flow
        var collectedState = mutableListOf<ApiState>()
        // to block until collection happens
        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.searchList.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.searchProducts("valid")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)

        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as List<CartProduct>
        assertThat(retrivedData.size, `is`(2))
        job.cancel()
    }


    @Test
    fun `searchProducts, (returns throws an exception ) AND (api state is error)`() =
        runTest {
            coEvery { repository.getProducts(any()) } returns flow {
                throw Exception("Network error or Invalid User")
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
            viewModel.searchProducts("invalid")
            testDispatcher.scheduler.advanceUntilIdle()
            completionSignal.await()
            assertTrue(collectedState[0] is ApiState.Loading)
            assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
            job.cancel()
        }
}
