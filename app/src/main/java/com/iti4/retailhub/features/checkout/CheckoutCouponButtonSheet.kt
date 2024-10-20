package com.iti4.retailhub.features.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iti4.retailhub.MainActivityViewModel
import com.iti4.retailhub.databinding.MybagbottomSheetLayoutBinding
import com.iti4.retailhub.models.Discount

class MyBottomSheetFragment(private val onClickBottomSheet: OnClickBottomSheet) :
    BottomSheetDialogFragment(),
    OnClickApply {
    private lateinit var binding: MybagbottomSheetLayoutBinding
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MybagbottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.promocodeEdittext.etPromoCode.isFocusableInTouchMode = true
        binding.promocodeEdittext.etPromoCode.isFocusable = true

        binding.promocodeEdittext.btnInsertCode.setOnClickListener {
            if (binding.promocodeEdittext.etPromoCode.text.isNotEmpty()) {
                onClickBottomSheet.passData(binding.promocodeEdittext.etPromoCode.text.toString())
                dismiss()
            }
        }

        val manager = LinearLayoutManager(requireContext())
        manager.setOrientation(RecyclerView.VERTICAL)
        binding.bottomSheetRecyclerView.layoutManager = manager

        val adapter =
            MyBagCouponRecyclerViewAdapter(
                this
            )
        binding.bottomSheetRecyclerView.adapter = adapter
        val list = mainActivityViewModel.copiedCouponsList
        if (list.size > 0) {
            adapter.submitList(
                list
            )
        } else {

        }

    }

    override fun onStart() {
        super.onStart()

        val bottomSheet =
            dialog?.findViewById<View>(R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isFitToContents = true
          //  behavior.halfExpandedRatio = 0.5f
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
    }

    override fun onClickApply(discount: Discount) {
        binding.promocodeEdittext.etPromoCode.setText(discount.title)
    }


}