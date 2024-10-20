package com.iti4.retailhub.datastorage.network

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.iti4.retailhub.AddTagsMutation
import com.iti4.retailhub.CollectionsQuery
import com.iti4.retailhub.CompleteDraftOrderMutation
import com.iti4.retailhub.CreateCustomerMutation
import com.iti4.retailhub.CreateDraftOrderMutation
import com.iti4.retailhub.CustomerEmailSearchQuery
import com.iti4.retailhub.CustomerUpdateDefaultAddressMutation
import com.iti4.retailhub.DeleteCustomerFavoritItemMutation
import com.iti4.retailhub.DeleteDraftOrderMutation
import com.iti4.retailhub.DraftOrderInvoiceSendMutation
import com.iti4.retailhub.GetAddressesByIdQuery
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.GetCustomerByIdQuery
import com.iti4.retailhub.GetCustomerFavoritesQuery
import com.iti4.retailhub.GetCustomerUsedDiscountsQuery
import com.iti4.retailhub.GetDiscountsQuery
import com.iti4.retailhub.GetDraftOrdersByCustomerQuery
import com.iti4.retailhub.GetProductTypesOfCollectionQuery
import com.iti4.retailhub.MarkAsPaidMutation
import com.iti4.retailhub.OrderDetailsQuery
import com.iti4.retailhub.OrdersQuery
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.ProductsQuery
import com.iti4.retailhub.UpdateCustomerAddressesMutation
import com.iti4.retailhub.UpdateCustomerFavoritesMetafieldsMutation
import com.iti4.retailhub.UpdateDraftOrderMutation
import com.iti4.retailhub.logic.customerAddressV2ToMailingAddressInput
import com.iti4.retailhub.logic.toBrandsList
import com.iti4.retailhub.logic.toCategory
import com.iti4.retailhub.logic.toDiscountList
import com.iti4.retailhub.logic.toOrder
import com.iti4.retailhub.logic.toOrderDetails
import com.iti4.retailhub.logic.toProductsList
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.DraftOrderInputModel
import com.iti4.retailhub.models.HomeProducts
import com.iti4.retailhub.models.Order
import com.iti4.retailhub.models.OrderDetails
import com.iti4.retailhub.type.CustomerInput
import com.iti4.retailhub.type.DraftOrderAppliedDiscountInput
import com.iti4.retailhub.type.DraftOrderAppliedDiscountType
import com.iti4.retailhub.type.DraftOrderDeleteInput
import com.iti4.retailhub.type.DraftOrderInput
import com.iti4.retailhub.type.DraftOrderLineItemInput
import com.iti4.retailhub.type.MailingAddressInput
import com.iti4.retailhub.type.MetafieldDeleteInput
import com.iti4.retailhub.type.OrderMarkAsPaidInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import okio.IOException
import javax.inject.Inject


class RemoteDataSourceImpl @Inject constructor(private val apolloClient: ApolloClient) :
    RemoteDataSource {
    override fun getProducts(query: String): Flow<List<HomeProducts>> = flow {
        val response = apolloClient.query(ProductsQuery(query)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data!!.products.toProductsList())
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }.retryWhen { cause, _ -> cause.message != "Something went wrong" }

    override fun getCustomerIdByEmail(email: String): Flow<CustomerEmailSearchQuery.Customers> =
        flow {
            val response = apolloClient.query(CustomerEmailSearchQuery(email)).execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customers)
            } else {
                throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
            }
        }

    override fun getProductDetails(id: String): Flow<ProductDetailsQuery.OnProduct?> = flow {
        val response = apolloClient.query(ProductDetailsQuery(id)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data?.node?.onProduct)
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun deleteCustomerFavoritItem(id: MetafieldDeleteInput): Flow<String?> = flow {
        val response = apolloClient.mutation(DeleteCustomerFavoritItemMutation(id)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data?.metafieldDelete?.deletedId)
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun createUser(input: CustomerInput): Flow<CreateCustomerMutation.CustomerCreate> =
        flow {
            val response = apolloClient.mutation(CreateCustomerMutation(input)).execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customerCreate!!)
            } else {
                throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
            }

        }

    override fun getBrands(): Flow<List<Brands>> = flow {
        val response = apolloClient.query(CollectionsQuery()).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data!!.collections.toBrandsList())
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun getProductTypesOfCollection(): Flow<List<Category>> = flow {
        val response = apolloClient.query(GetProductTypesOfCollectionQuery()).execute()
        if (!response.hasErrors() && response.data != null) {
            val list = ArrayList<Category>()
            response.data!!.collections.nodes.forEach {
                list.add(it.toCategory())
            }
            list.removeFirst()
            list.reverse()
            emit(list)
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun getOrders(query: String): Flow<List<Order>> = flow {
        val response = apolloClient.query(OrdersQuery(query)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data!!.orders.nodes.map {
                it.toOrder()
            })
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }


    override fun getMyBagProducts(query: String): Flow<List<CartProduct>> = flow {
        val response = apolloClient.query(GetDraftOrdersByCustomerQuery(query)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(extractCart(response.data!!.draftOrders))
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun getCustomerInfoById(id: String): Flow<GetCustomerByIdQuery.Customer> = flow {
        val response = apolloClient.query(GetCustomerByIdQuery(id)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data!!.customer!!)
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun getCustomerFavoritesoById(
        id: String,
        namespace: String
    ): Flow<GetCustomerFavoritesQuery.Customer> = flow {
        val response =
            apolloClient.query(GetCustomerFavoritesQuery(id, namespace)).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data!!.customer!!)
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun createCheckoutDraftOrder(draftOrderInputModel: DraftOrderInputModel): Flow<CreateDraftOrderMutation.DraftOrderCreate> =
        flow {
            Log.i("here", "remote: " + draftOrderInputModel)
            val draftOrderInput = toGraphQLDraftOrderInput(draftOrderInputModel)

            val response =
                apolloClient.mutation(CreateDraftOrderMutation(draftOrderInput))
                    .execute()

            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrderCreate!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun saveProductToFavotes(input: CustomerInput): Flow<UpdateCustomerFavoritesMetafieldsMutation.CustomerUpdate> =
        flow {
            val response =
                apolloClient.mutation(UpdateCustomerFavoritesMetafieldsMutation(input))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customerUpdate!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }

        }


    override fun markOrderAsPaid(orderId: String): Flow<MarkAsPaidMutation.OrderMarkAsPaid> =
        flow {
            val orderMarkAsPaidInput = OrderMarkAsPaidInput(id = orderId)
            val response =
                apolloClient.mutation(MarkAsPaidMutation(orderMarkAsPaidInput))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.orderMarkAsPaid!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun insertMyBagItem(
        varientId: String,
        customerId: String
    ): Flow<CreateDraftOrderMutation.DraftOrderCreate> =
        flow {
            val draftOrderInput = createDraftOrderFromVairentOnly(varientId, customerId)
            val response =
                apolloClient.mutation(CreateDraftOrderMutation(draftOrderInput))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrderCreate!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }


    override fun emailCheckoutDraftOrder(draftOrderId: String): Flow<DraftOrderInvoiceSendMutation.DraftOrder> =
        flow {
            val response =
                apolloClient.mutation(DraftOrderInvoiceSendMutation(draftOrderId))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrderInvoiceSend!!.draftOrder!!)

            }
        }

    override fun getDraftOrdersByCustomer(query: String): Flow<GetDraftOrdersByCustomerQuery.DraftOrders> =
        flow {
            val response =
                apolloClient.query(GetDraftOrdersByCustomerQuery(query))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrders)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun getAddressesById(customerId: String): Flow<GetAddressesByIdQuery.Customer> =
        flow {
            val response =
                apolloClient.query(GetAddressesByIdQuery(customerId))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customer!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun getDefaultAddress(customerId: String): Flow<GetAddressesDefaultIdQuery.Customer> =
        flow {
            val response =
                apolloClient.query(GetAddressesDefaultIdQuery(customerId))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customer!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }


    override fun completeCheckoutDraftOrder(draftOrderId: String): Flow<CompleteDraftOrderMutation.DraftOrder> =
        flow {
            val response =
                apolloClient.mutation(CompleteDraftOrderMutation(draftOrderId))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrderComplete!!.draftOrder!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }


    override fun deleteMyBagItem(query: String): Flow<DeleteDraftOrderMutation.DraftOrderDelete> =
        flow {
            val deleteDraftOrderInput = DraftOrderDeleteInput(id = query)
            val deleteDraftOrderMutation =
                DeleteDraftOrderMutation(deleteDraftOrderInput)
            val response =
                apolloClient.mutation(deleteDraftOrderMutation).execute()

            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrderDelete!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun updateMyBagItem(cartProduct: CartProduct): Flow<UpdateDraftOrderMutation.DraftOrderUpdate> =
        flow {
            val draftOrderInput = com.iti4.retailhub.type.DraftOrderInput(
                lineItems = Optional.present(
                    listOf(
                        DraftOrderLineItemInput(
                            variantId = Optional.present(cartProduct.itemId),
                            quantity = cartProduct.itemQuantity
                        )
                    )
                )
            )
            val updateDraftOrderMutation =
                UpdateDraftOrderMutation(
                    cartProduct.draftOrderId,
                    draftOrderInput
                )
            val response =
                apolloClient.mutation(updateDraftOrderMutation).execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.draftOrderUpdate!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }


    override fun updateCustomerAddress(
        customerId: String,
        address: List<CustomerAddressV2>
    ): Flow<UpdateCustomerAddressesMutation.CustomerUpdate> =
        flow {
            val customerInput = CustomerInput(
                id = Optional.present(customerId),
                addresses = Optional.present(
                    address.customerAddressV2ToMailingAddressInput(
                        address
                    )
                )
            )
            val response =
                apolloClient.mutation(UpdateCustomerAddressesMutation(customerInput))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customerUpdate!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun updateCustomerDefaultAddress(
        customerId: String,
        addressId: String
    ): Flow<CustomerUpdateDefaultAddressMutation.Customer> =
        flow {
            val response =
                apolloClient.mutation(
                    CustomerUpdateDefaultAddressMutation(
                        addressId,
                        customerId
                    )
                )
                    .execute()
            Log.i("here", "updateCustomerDefaultAddress:  updated" + addressId)
            Log.i("here", "updateCustomerDefaultAddress:  updated" + response.errors)
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.customerUpdateDefaultAddress!!.customer!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

    override fun setCustomerUsedDiscounts(
        customerId: String,
        discountCode: String
    ): Flow<AddTagsMutation.Node> =
        flow {
            val response =
                apolloClient.mutation(AddTagsMutation(customerId, listOf(discountCode)))
                    .execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.tagsAdd!!.node!!)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }


    override fun getDiscounts(): Flow<List<Discount>> = flow {
        val response = apolloClient.query(GetDiscountsQuery()).execute()
        if (!response.hasErrors() && response.data != null) {
            emit(response.data!!.codeDiscountNodes.toDiscountList())
        } else {
            throw Exception(response.errors?.get(0)?.message ?: "Something went wrong")
        }
    }

    override fun getCustomerUsedDiscounts(customerId: String): Flow<List<String>> =
        flow {
            val response =
                apolloClient.query(GetCustomerUsedDiscountsQuery(customerId)).execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response!!.data!!.customer!!.tags)
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }


    private fun extractCart(item: GetDraftOrdersByCustomerQuery.DraftOrders): List<CartProduct> {

        return item.nodes.map {
            val product = it.lineItems.nodes.get(0)
            val variant = product.variant
            val variantSize = variant!!.selectedOptions[0]
            val variantColor = variant!!.selectedOptions[1]
            val variantImage =
                variant.product.media.nodes[0].onMediaImage!!.image!!.url
            CartProduct(
                it!!.id,
                variant!!.id,
                product.quantity,
                variant.inventoryQuantity!!,
                product.title,
                variantSize.value,
                variantColor.value,
                variant.price as String,
                variantImage as String
            )
        }
    }


    private fun toGraphQLDraftOrderInput(draftOrderInputModel: DraftOrderInputModel): DraftOrderInput {
        return DraftOrderInput(
            lineItems = Optional.present(draftOrderInputModel.lineItems.map { lineItem ->
                DraftOrderLineItemInput(
                    variantId = Optional.present(lineItem.variantId),
                    quantity = lineItem.quantity
                )
            }),
            appliedDiscount = if (draftOrderInputModel.appliedDiscount != null) Optional.present(
                DraftOrderAppliedDiscountInput(
                    value = draftOrderInputModel.appliedDiscount!!.value,
                    valueType = DraftOrderAppliedDiscountType.PERCENTAGE,
                )
            ) else Optional.present(null),
            customerId = Optional.present(draftOrderInputModel.customer!!.id),
            shippingAddress = draftOrderInputModel.shippingAddress!!.let {
                Optional.present(
                    MailingAddressInput(
                        firstName = Optional.present(draftOrderInputModel.customer.firstName),
                        address1 = Optional.present(it.address1),
                        address2 = Optional.present(it.address2),
                        city = Optional.present(it.city),
                        country = Optional.present(it.country),
                        phone = Optional.present(draftOrderInputModel.customer.phone),
                        zip = Optional.present(it.zip)
                    )
                )
            },
            email = Optional.present(draftOrderInputModel.customer!!.email),
            taxExempt = Optional.present(true)
        )
    }


    private fun createDraftOrderFromVairentOnly(
        varientId: String,
        customerId: String
    ): DraftOrderInput {
        return DraftOrderInput(
            lineItems = Optional.present(
                listOf(
                    DraftOrderLineItemInput(
                        variantId = Optional.present(varientId), quantity = 1
                    )
                )
            ),
            customerId = Optional.present(customerId)
        )
    }

    override fun getOrderDetails(orderId: String): Flow<OrderDetails> =
        flow {
            val response = apolloClient.query(OrderDetailsQuery(orderId)).execute()
            if (!response.hasErrors() && response.data != null) {
                emit(response.data!!.order!!.toOrderDetails())
            } else {
                throw Exception(
                    response.errors?.get(0)?.message ?: "Something went wrong"
                )
            }
        }

}

