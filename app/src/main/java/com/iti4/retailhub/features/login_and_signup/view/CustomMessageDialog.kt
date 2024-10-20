package com.iti4.retailhub.features.login_and_signup.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import com.iti4.retailhub.R
class CustomMessageDialog(context: Context) : Dialog(context) {

    private var loadingTextView: TextView
    private var loadingTextView2: TextView

    init {
        setContentView(R.layout.message_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        findViewById<View>(R.id.dialog_layout).setOnClickListener { dismiss() } // Dismiss on click
        findViewById<View>(R.id.button).setOnClickListener { dismiss() } // Dismiss on click
        loadingTextView = findViewById(R.id.messageTextCustomDialog)
        loadingTextView2 = findViewById(R.id.messageTextCustomDialog2)
    }

    fun setText(text: String,message: String) {
        loadingTextView.text = text
        loadingTextView2.text=message
    }
}