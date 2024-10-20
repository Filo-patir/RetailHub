package com.iti4.retailhub.datastorage.network


import android.util.Log
import com.iti4.retailhub.BuildConfig
import com.iti4.retailhub.features.summary.Customer
import com.iti4.retailhub.features.summary.PaymentRequest
import com.iti4.retailhub.models.CurrencyResponse
import com.iti4.retailhub.modelsdata.PlaceLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RetrofitDataSourceImp @Inject constructor(
    private val apiService: ApiService,
    private val apiServiceForLocation: ApiServiceForLocation,
    private val apiServiceForLocationGeocoding: ApiServiceForLocationGeocoding,
    private val apiServiceForCurrencyRates: ApiServiceForCurrencyRates
) :
    RetrofitDataSource {

    private val Location_API_KEY = BuildConfig.Location_API_KEY

    override suspend fun createStripeUser(
        name: String, email: String
    ): Response<Customer> {
        val user = HashMap<String, String>();
        user.put("email", "user@example.com");
        user.put("name", "User Name");
        return apiService.createStripeUser(user)
    }

    override fun createStripePaymentIntent(
        paymentRequest: PaymentRequest
    ): Flow<Response<ResponseBody>> = flow {
        emit(apiService.getStripePaymentIntent(paymentRequest))
    }

    override fun getLocationSuggestions(
        query: String
    ): Flow<Response<List<PlaceLocation>>> = flow {
        emit(apiServiceForLocation.getLocationSuggestions(Location_API_KEY, query, 5, 1))
    }

    override fun getCurrencyRates(): Flow<Response<CurrencyResponse>> = flow {
        emit(apiServiceForCurrencyRates.getCurrencyExchangeRates())
    }


    override fun getLocationGeocoding(
        lat: String,
        lon: String
    ): Flow<Response<com.iti4.retailhub.features.address.PlaceLocation>> = flow {
        emit(
            apiServiceForLocationGeocoding.getLocationGeocoding(
                Location_API_KEY,
                lat,
                lon,
                "Json"
            )
        )
    }


}