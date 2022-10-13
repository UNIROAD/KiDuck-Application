package com.uniroad.kiduck

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class LoadingDialog
    constructor(contexxt: Context): Dialog(contexxt) {
        init {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            setContentView(R.layout.dialog_loading)
        }
}