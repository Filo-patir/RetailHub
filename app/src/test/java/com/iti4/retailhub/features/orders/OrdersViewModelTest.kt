package com.iti4.retailhub.features.orders

import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.mybag.viewmodel.MyBagViewModel
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.Order
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
class OrdersViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: OrdersViewModel
    private val repository = mockk<IRepository>()


    @Before
    fun setup() {
        every { repository.getUserShopLocalId() } returns ""
        Dispatchers.setMain(testDispatcher)
        viewModel = OrdersViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getOrders,( api state is success)`() = runTest {
        coEvery { repository.getOrders(any()) } returns flow {
            emit(
                listOf(
                    mockk<Order>(),
                    mockk<Order>()
                )
            )
        }
        var collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.orders.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>)
                    completionSignal.complete(Unit)
            }
        }

        viewModel.getOrders()
        testDispatcher.scheduler.advanceUntilIdle()
        // wait before assertion
        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Loading)

        assertThat(collectedState[1], `is`(instanceOf(ApiState.Success::class.java)))

        val result = collectedState[1] as ApiState.Success<*>
        val retrivedData = result.data as List<Order>
        assertThat(retrivedData.size, `is`(2))
        job.cancel()
    }


    @Test
    fun `getOrders,(api state is error)`() =
        runTest {
            coEvery { repository.getOrders(any()) } returns flow {
                throw Exception("Network error or Invalid User")
            }
            var collectedState = mutableListOf<ApiState>()
            val completionSignal = CompletableDeferred<Unit>()
            val job = launch {
                viewModel.orders.collect {
                    collectedState.add(it)
                    if (it is ApiState.Error)
                        completionSignal.complete(Unit)
                }
            }
            viewModel.getOrders()
            testDispatcher.scheduler.advanceUntilIdle()
            completionSignal.await()
            assertTrue(collectedState[0] is ApiState.Loading)
            assertThat(collectedState[1], `is`(instanceOf(ApiState.Error::class.java)))
            job.cancel()
        }
}