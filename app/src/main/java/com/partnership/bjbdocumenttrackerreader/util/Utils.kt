package com.partnership.bjbdocumenttrackerreader.util

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import com.partnership.bjbdocumenttrackerreader.R


object Utils {

    private var dialog: Dialog? = null

    fun showLoading(context: Context, message: String = "Loading...") {
        if (dialog?.isShowing == true) return

        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        dialog?.setContentView(view)

        val tvMessage = view.findViewById<TextView>(R.id.tvLoadingMessage)
        tvMessage.text = message

        dialog?.show()
    }

    fun dismissLoading() {
        dialog?.let {
            if (it.isShowing) {
                try {
                    it.dismiss()
                } catch (e: Exception) {
                    Log.e("Utils", "Dismiss error: ${e.message}")
                } finally {
                    dialog = null
                }
            }
        }
    }

}
