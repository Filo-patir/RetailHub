package com.iti4.retailhub.logic

import com.apollographql.apollo.api.Optional
import com.iti4.retailhub.CollectionsQuery
import com.iti4.retailhub.GetAddressesByIdQuery
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.GetDiscountsQuery
import com.iti4.retailhub.GetProductTypesOfCollectionQuery
import com.iti4.retailhub.OrderDetailsQuery
import com.iti4.retailhub.OrdersQuery
import com.iti4.retailhub.ProductsQuery
import com.iti4.retailhub.models.Brands
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.CustomerAddress
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.HomeProducts
import com.iti4.retailhub.models.Order
import com.iti4.retailhub.models.OrderDetails
import com.iti4.retailhub.models.OrderDetailsItem
import com.iti4.retailhub.type.MailingAddressInput


fun ProductsQuery.Products.toProductsList(): List<HomeProducts> {
    val list = ArrayList<HomeProducts>()
    this.edges.forEach {
        list.add(
            HomeProducts(
                it.node.id,
                it.node.title,
                it.node.totalInventory,
                it.node.media.nodes[0].onMediaImage?.image?.url.toString(),
                it.node.vendor,
                it.node.priceRangeV2.maxVariantPrice.amount.toString(),
                it.node.priceRangeV2.minVariantPrice.amount.toString(),
                it.node.priceRangeV2.minVariantPrice.currencyCode.toString()
            )
        )
    }
    return list
}

fun CollectionsQuery.Collections.toBrandsList(): List<Brands> {
    val list = ArrayList<Brands>()
    this.nodes.forEach {
        list.add(Brands(it.id, it.title, it.image?.url.toString()))
    }
    list.removeFirst()
    return list
}

fun GetProductTypesOfCollectionQuery.Node.toCategory(): Category {
    return Category(this.id, this.title, this.products.nodes.distinct().map { it.productType })
}

fun GetDiscountsQuery.CodeDiscountNodes.toDiscountList(): List<Discount> {
    val list = ArrayList<Discount>()
    this.nodes.forEach {
        val splitValue = it.codeDiscount.onDiscountCodeBasic!!.summary.split(" ")
        list.add(
            Discount(
                it.id,
                splitValue[0],
                it.codeDiscount.onDiscountCodeBasic!!.title
            )
        )
    }
    return list
}
/*data class CustomerAddress(
    var address1: String, var address2: String,
    var phone: String, var name: String,
    var newAddress: Boolean = false, var id:String?=null, var isDefault :Boolean = false
) */

fun GetAddressesDefaultIdQuery.DefaultAddress.toCustomerAddress(): CustomerAddress {
    return CustomerAddress(
        this.address1 ?: " ",
        this.address2 ?: " ",
        this.phone ?: " ",
        this.name ?: " ",
        id = this.id
    )
}

fun GetAddressesByIdQuery.Customer.toCustomerAddressList(): MutableList<CustomerAddressV2> {
    val list = this.addresses
    return list.map {
        CustomerAddressV2(
            it.address1!!,
            it.address2,
            it.city,
            it.country,
            it.phone!!,
            it.name!!,
            id = it.id
        )
    }.toMutableList()
}

fun List<CustomerAddressV2>.customerAddressV2ToMailingAddressInput(address: List<CustomerAddressV2>): List<MailingAddressInput> {
    return address.map {
        MailingAddressInput(
            address1 = Optional.present(it.address1),
            address2 = Optional.present(it.address2),
            city = Optional.present(it.city),
            country = Optional.present(it.country),
            phone = Optional.present(it.phone),
            firstName = Optional.present(it.name)
        )
    }
}

fun OrdersQuery.Node.toOrder(): Order {
    return Order(
        this.id,
        this.name,
        this.confirmationNumber,
        this.createdAt.toString().split("T")[0],
        this.currentTotalPriceSet.shopMoney.amount.toString(),
        this.currentTotalPriceSet.shopMoney.currencyCode.toString(),
        this.currentSubtotalLineItemsQuantity,
        this.displayFinancialStatus.toString(),
        this.displayFulfillmentStatus.toString()
    )
}

fun OrderDetailsQuery.Order.toOrderDetails(): OrderDetails {
    return OrderDetails(
        this.id,
        this.name,
        this.createdAt.toString().split("T")[0],
        this.confirmationNumber.toString(),
        this.displayFulfillmentStatus.toString(),
        this.currentSubtotalLineItemsQuantity.toString(),
        this.lineItems.nodes.map {
            OrderDetailsItem(
                it.id,
                it.product?.title ?: "",
                it.product?.vendor ?: "",
                it.currentQuantity.toString(),
                it.variantTitle?.split("/")?.get(0) ?: "",
                it.variantTitle?.split("/")?.get(1) ?: "",
                it.originalUnitPriceSet.shopMoney.amount.toString(),
                it.originalUnitPriceSet.shopMoney.currencyCode.toString(),
                it.product?.media?.nodes?.get(0)?.onMediaImage?.image?.url?.toString() ?: "",
            )
        },
        this.shippingAddress?.formatted?.joinToString(", ") ?: "",
        this.displayFinancialStatus.toString(),
        this.currentTotalDiscountsSet.shopMoney.amount.toString(),
        this.currentTotalPriceSet.shopMoney.amount.toString(),
        "EGP"
    )
}

