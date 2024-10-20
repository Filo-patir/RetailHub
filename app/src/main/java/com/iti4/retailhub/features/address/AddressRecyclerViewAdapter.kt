package com.iti4.retailhub.features.address

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iti4.retailhub.CustomDialog
import com.iti4.retailhub.databinding.RvAddressesItemBinding
import com.iti4.retailhub.models.CustomerAddress
import com.iti4.retailhub.models.CustomerAddressV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddressRecyclerViewAdapter(
    private val handleAction: OnClickAddress, var listData: MutableList<CustomerAddressV2>
) :
    RecyclerView.Adapter<AddressRecyclerViewAdapter.ViewHolder>() {
    private val TAG :String = "AddressRecyclerViewAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvAddressesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listData[position]
        with(holder.binding) {
            if (item.isDefault) {
                btnDeleteAddress.visibility = View.INVISIBLE
                cbAddressDefault.visibility = View.VISIBLE
                cbAddressDefault.isChecked = true
            } else {
                btnDeleteAddress.visibility = View.VISIBLE
                cbAddressDefault.isChecked = false
                cbAddressDefault.visibility = View.VISIBLE
            }
            // if checked do this if not do this dont let it clickable if checked
            cbAddressDefault.setOnClickListener {
                handleAction.setDefaultAddress(position)
            }
            tvAddressName.text = item.name
            tvAddress1Add.text = item.address1
            tvAddress2Add.text = "${item.address2}  ${item.city} ${item.country}"
            tvAddressPhone.text = item.phone
            tvAddressEdit.setOnClickListener {
                handleAction.editDetails(item)
            }
            btnDeleteAddress.setOnClickListener {
                val dialog =
                    CustomDialog(it.context, handleAction, null, "address")
                dialog.show()
                dialog.getData(item.id ?: "null")

            }
            root.setOnClickListener {
                handleAction.checkoutClickedAnAddress(item)
            }
        }
    }

    fun updateData(addressesList: MutableList<CustomerAddressV2>) {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                listData = addressesList
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(val binding: RvAddressesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}