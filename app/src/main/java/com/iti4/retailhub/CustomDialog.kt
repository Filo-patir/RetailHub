package com.iti4.retailhub

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable

import android.view.LayoutInflater
import com.iti4.retailhub.databinding.DialogConfirmationLayoutBinding
import com.iti4.retailhub.features.address.OnClickAddress
import com.iti4.retailhub.features.mybag.OnClickMyBag
import com.iti4.retailhub.models.CartProduct


class CustomDialog(
    context: Context,
    private val actionHandlerAddress: OnClickAddress? = null,
    private val actionHandlerMyBag: OnClickMyBag? = null,
    private val type: String
) : Dialog(context) {
    private val binding = DialogConfirmationLayoutBinding.inflate(LayoutInflater.from(context))
    private lateinit var dataAddress: String
    private lateinit var dataMybag: CartProduct
    private lateinit var deleteMyBagItem: ((CartProduct) -> Unit)

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        binding.btnYes.setOnClickListener {
            this.dismiss()
            if (type == "address") {
                if (dataAddress != "null") {
                    actionHandlerAddress!!.deleteAddress(dataAddress)
                }

            } else if (type == "mybag") {
                actionHandlerMyBag!!.deleteMyBagItem(dataMybag)
                deleteMyBagItem.invoke(dataMybag)
            }
        }
        binding.btnNo.setOnClickListener {
            this.dismiss()
        }
    }

    fun getData(
        id: String? = null,
        cartProduct: CartProduct? = null,
        deleteMyBagItem: ((CartProduct) -> Unit)? = null
    ) {
        if (type == "address")
            dataAddress = id!!
        else if (type == "mybag"){
            dataMybag = cartProduct!!
            this.deleteMyBagItem=deleteMyBagItem!!
        }

    }


}