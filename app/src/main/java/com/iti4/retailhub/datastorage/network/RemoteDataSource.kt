package com.iti4.retailhub.datastorage.network

import com.apollographql.apollo.api.Optional
import com.iti4.retailhub.CompleteDraftOrderMutation
import com.iti4.retailhub.CreateCustomerMutation
import com.iti4.retailhub.CreateDraftOrderMutation
import com.iti4.retailhub.CustomerEmailSearchQuery
import com.iti4.retailhub.CustomerUpdateDefaultAddressMutation
import com.iti4.retailhub.DeleteDraftOrderMutation
import com.iti4.retailhub.DraftOrderInvoiceSendMutation
import com.iti4.retailhub.GetAddressesByIdQuery
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.GetCustomerByIdQuery
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.GetDiscountsQuery
import com.iti4.retailhub.GetDraftOrdersByCustomerQuery
import com.iti4.retailhub.MarkAsPaidMutation
import com.iti4.retailhub.OrdersQuery
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.UpdateCustomerAddressesMutation
import com.iti4.retailhub.UpdateCustomerFavoritesMetafieldsMutation
import com.iti4.retailhub.UpdateDraftOrderMutation
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.CustomerAddress
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.DraftOrderInputModel
import com.iti4.retailhub.models.HomeProducts
import com.iti4.retailhub.type.CustomerInput
import com.iti4.retailhub.type.MetafieldDeleteInput
import kotlinx.coroutines.flow.Flow


interface RemoteDataSource {


    fun getMyBagProducts(query: String): Flow<List<CartProduct>>
    fun deleteMyBagItem(query: String): Flow<DeleteDraftOrderMutation.DraftOrderDelete>
    fun updateMyBagItem(cartProduct: CartProduct): Flow<UpdateDraftOrderMutation.DraftOrderUpdate>
    fun getProducts(query: String): Flow<List<HomeProducts>>
    fun getBrands(): Flow<List<Brands>>
    fun getProductTypesOfCollection(): Flow<List<Category>>
    fun getCustomerInfoById(id: String): Flow<GetCustomerByIdQuery.Customer>
    fun createCheckoutDraftOrder(draftOrderInputModel: DraftOrderInputModel): Flow<CreateDraftOrderMutation.DraftOrderCreate>
    fun emailCheckoutDraftOrder(draftOrderId: String): Flow<DraftOrderInvoiceSendMutation.DraftOrder>
    fun completeCheckoutDraftOrder(draftOrderId: String): Flow<CompleteDraftOrderMutation.DraftOrder>
    fun insertMyBagItem( varientId: String, customerId: String): Flow<CreateDraftOrderMutation.DraftOrderCreate>
    fun markOrderAsPaid(orderId: String): Flow<MarkAsPaidMutation.OrderMarkAsPaid>
    fun createUser(input: CustomerInput): Flow<CreateCustomerMutation.CustomerCreate>
    fun getCustomerIdByEmail(email: String): Flow<CustomerEmailSearchQuery.Customers>
    fun getOrders(query: String): Flow<OrdersQuery.Orders>
    fun getProductDetails(id: String): Flow<ProductDetailsQuery.OnProduct?>
    fun getAddressesById(customerId: String): Flow<GetAddressesByIdQuery.Customer>
    fun getDraftOrdersByCustomer(varientId: String): Flow<GetDraftOrdersByCustomerQuery.DraftOrders>
    fun updateCustomerAddress(
        customerId: String,
        address: List<CustomerAddress>
    ): Flow<UpdateCustomerAddressesMutation.CustomerUpdate>
    fun saveProductToFavotes(input: CustomerInput): Flow<UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate>
    fun getCustomerFavoritesoById(id: String): Flow<GetCustomerFavoritesQuery.Customer>
    fun deleteCustomerFavoritItem(id: MetafieldDeleteInput): Flow<String?>
    fun getDefaultAddress(customerId: String): Flow<GetAddressesDefaultIdQuery.Customer>
    fun updateCustomerDefaultAddress(
        customerId: String,
        addressId: String
    ): Flow<CustomerUpdateDefaultAddressMutation.Customer>

    fun getDiscounts(): Flow<List<Discount>>
}