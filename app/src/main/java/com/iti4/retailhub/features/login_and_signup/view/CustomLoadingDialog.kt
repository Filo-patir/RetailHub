package com.iti4.retailhub.features.login_and_signup.view

import android.app.Dialog
import android.content.Context
import com.iti4.retailhub.R

class CustomLoadingDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.loading_dialog)
        setCancelable(false)
    }
}