package com.iti4.retailhub.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.iti4.retailhub.BuildConfig
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.Repository
import com.iti4.retailhub.datastorage.network.ApiService
import com.iti4.retailhub.datastorage.network.ApiServiceForCurrencyRates
import com.iti4.retailhub.datastorage.network.ApiServiceForLocation
import com.iti4.retailhub.datastorage.network.ApiServiceForLocationGeocoding
import com.iti4.retailhub.datastorage.network.RemoteDataSource
import com.iti4.retailhub.datastorage.network.RemoteDataSourceImpl
import com.iti4.retailhub.datastorage.network.RetrofitDataSource
import com.iti4.retailhub.datastorage.network.RetrofitDataSourceImp
import com.iti4.retailhub.datastorage.network.RetrofitHelper
import com.iti4.retailhub.datastorage.reviews.ReviewsDataStore
import com.iti4.retailhub.datastorage.reviews.ReviewsDataStoreInterface
import com.iti4.retailhub.datastorage.userlocalprofiledata.UserLocalProfileData
import com.iti4.retailhub.datastorage.userlocalprofiledata.UserLocalProfileDataInterface
import com.iti4.retailhub.logic.INetworkUtils
import com.iti4.retailhub.logic.NetworkUtils
import com.iti4.retailhub.userauthuntication.UserAuthuntication
import com.iti4.retailhub.userauthuntication.UserAuthunticationInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideRepository(
        remoteDataSource: RemoteDataSource,
        retrofitDataSource: RetrofitDataSource,
        userAuthuntication: UserAuthunticationInterface,
        UserLocalProfileData: UserLocalProfileDataInterface,
        reviewsDataStore: ReviewsDataStoreInterface
    ): IRepository {
        return Repository(
            remoteDataSource,
            retrofitDataSource,
            userAuthuntication,
            UserLocalProfileData,
            reviewsDataStore
        )

    }


    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://android-alex-team4.myshopify.com/admin/api/2024-10/graphql.json")
            .addHttpHeader("X-Shopify-Access-Token", BuildConfig.ADMIN_ACCESS_TOKEN)
            .build()
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(apolloClient: ApolloClient): RemoteDataSource {
        return RemoteDataSourceImpl(apolloClient)
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitHelper.retrofitInstance.create(ApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideApiServiceForLocation(): ApiServiceForLocation {
        return RetrofitHelper.retrofitInstanceForLocation.create(ApiServiceForLocation::class.java)
    }

    @Provides
    @Singleton
    fun provideApiServiceForLocationGeocoding(): ApiServiceForLocationGeocoding {
        return RetrofitHelper.retrofitInstanceForReverseLocation.create(
            ApiServiceForLocationGeocoding::class.java
        )
    }

    @Provides
    @Singleton
    fun provideApiServiceForCurrencyRates(): ApiServiceForCurrencyRates {
        return RetrofitHelper.retrofitInstanceForCurrency.create(
            ApiServiceForCurrencyRates::class.java
        )
    }

    @Provides
    @Singleton
    fun provideRetrofitDataSource(
        apiService: ApiService,
        apiServiceForLocation: ApiServiceForLocation,
        apiServiceForLocationGeocoding: ApiServiceForLocationGeocoding,
        apiServiceForCurrencyRates: ApiServiceForCurrencyRates
    ): RetrofitDataSource {
        return RetrofitDataSourceImp(
            apiService,
            apiServiceForLocation,
            apiServiceForLocationGeocoding,
            apiServiceForCurrencyRates
        )
    }

    @Provides
    @Singleton
    fun provideUserAuthuntication(@ApplicationContext context: Context): UserAuthunticationInterface {
        return UserAuthuntication(context)
    }

    @Provides
    @Singleton
    fun provideUserLocalProfileData(@ApplicationContext context: Context): UserLocalProfileDataInterface {
        return UserLocalProfileData(context)
    }

    @Provides
    @Singleton
    fun reviewsDataStore(): ReviewsDataStoreInterface {
        return ReviewsDataStore()
    }

    @Provides
    fun provideNetworkUtils(@ApplicationContext context: Context) : INetworkUtils{
        return NetworkUtils(context)
    }

}