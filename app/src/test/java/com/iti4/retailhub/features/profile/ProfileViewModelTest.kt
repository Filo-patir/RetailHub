package com.iti4.retailhub.features.profile

import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.features.productSearch.viewmodel.ProductSearchViewModel
import com.iti4.retailhub.features.summary.Customer
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


class ProfileViewModelTest{



    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<IRepository>( relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signOut calls loginOut and deleteUserData on repository`() {

        viewModel.signOut()
        verify { repository.loginOut() }
        verify { repository.deleteUserData() }
    }

}