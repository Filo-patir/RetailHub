package com.iti4.retailhub.features.address

import com.iti4.retailhub.GetAddressesByIdQuery
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class AddressViewModelTest {


    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AddressViewModel
    private val repository = mockk<IRepository>()


    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { repository.getUserShopLocalId() } returns "123"
        every { repository.getAddressesById(any()) } returns flowOf(
            GetAddressesByIdQuery.Customer(
                listOf()
            )
        )
        every { repository.getDefaultAddress("123") } returns flow {
            throw Exception("Network or data not found exception")
        }

        Dispatchers.setMain(testDispatcher)
        viewModel = AddressViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `getDefaultAddress, Valid Id, it returns address AND api state is success`() = runTest {
        // Mock valid response
        every { repository.getAddressesById(any()) } returns flowOf(
            GetAddressesByIdQuery.Customer(
                listOf()
            )
        )
        coEvery { repository.getDefaultAddress(any()) } returns flowOf(mockk<GetAddressesDefaultIdQuery.Customer>())

        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.defaultAddressState.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>) {
                    completionSignal.complete(Unit)
                }
            }
        }
        viewModel.getDefaultAddress()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Success<*>)
        job.cancel()
    }

    @Test
    fun `getDefaultAddress, Invalid Id, it returns exception AND api state is error`() = runTest {
        // Mock exception response
        every { repository.getAddressesById("123") } returns flowOf(
            GetAddressesByIdQuery.Customer(
                listOf()
            )
        )
        coEvery { repository.getDefaultAddress("123") } returns flow {
            throw Exception("Network or data not found exception")
        }


        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.defaultAddressState.collect {
                collectedState.add(it)
                if (it is ApiState.Error) {
                    completionSignal.complete(Unit)
                }
            }
        }

        viewModel.getDefaultAddress()
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()

        assertTrue(collectedState[0] is ApiState.Error)

        job.cancel()
    }


    @Test
    fun `getLocationGeocoding, Valid Coordinates, it returns address AND api state is success`() = runTest {
        val mockResponse = mockk<Response<PlaceLocation>> {
            every { isSuccessful } returns true
            every { body() } returns mockk()
        }
        coEvery { repository.getLocationGeocoding(any(), any()) } returns flowOf(mockResponse)
        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()
        val job = launch {
            viewModel.addressGeocoding.collect {
                collectedState.add(it)
                if (it is ApiState.Success<*>) {
                    completionSignal.complete(Unit)
                }
            }
        }
        viewModel.getLocationGeocoding("12.34", "56.78")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[1] is ApiState.Success<*>)
        job.cancel()
    }

    @Test
    fun `getLocationGeocoding, Invalid latitude longitude, emits error state`() = runTest {
        coEvery { repository.getLocationGeocoding(any(), any()) } returns
                flow{ emit((throw Exception("Invalid coordinates")))}

        val collectedState = mutableListOf<ApiState>()
        val completionSignal = CompletableDeferred<Unit>()

        val job = launch {
            viewModel.addressGeocoding.collect {
                collectedState.add(it)
                if (it is ApiState.Error) {
                    completionSignal.complete(Unit)
                }
            }
        }

        viewModel.getLocationGeocoding("123.2", "123.2")
        testDispatcher.scheduler.advanceUntilIdle()
        completionSignal.await()
        assertTrue(collectedState[0] is ApiState.Error)
        job.cancel()
    }

}
