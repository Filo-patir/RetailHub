package com.iti4.retailhub.datastorage

import com.iti4.retailhub.CollectionsQuery
import com.iti4.retailhub.GetDraftOrdersByCustomerQuery
import com.iti4.retailhub.ProductsQuery
import com.iti4.retailhub.datastorage.network.ApiState
import kotlinx.coroutines.flow.Flow

interface IRepository {
    fun getProducts(query: String): Flow<ProductsQuery.Products>
    fun getBrands(): Flow<CollectionsQuery.Collections>
    fun getMyBagProducts(query: String):Flow<GetDraftOrdersByCustomerQuery.DraftOrders>
}