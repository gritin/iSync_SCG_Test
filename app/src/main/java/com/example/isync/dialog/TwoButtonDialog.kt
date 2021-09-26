package com.example.isync.dialog

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.isync.R

class TwoButtonDialog {

    var onOKButtonClickListener:(() -> Unit)? = null
    var onCloseButtonClickListener:(() -> Unit)? = null

    private lateinit var contentText : TextView

    fun showDialog(activity: Activity?, title: String?,FirstbtnText:String?, SecondBtnText:String?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )


        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_two_button)
        val titleText = dialog.findViewById<View>(R.id.dialog_title) as TextView
        titleText.text = title

        val dialogOKButton: Button = dialog.findViewById<View>(R.id.dialog_two_ok) as Button
        val dialogCloseButton: Button = dialog.findViewById<View>(R.id.dialog_two_close) as Button
        dialogOKButton.setText(FirstbtnText)
        dialogCloseButton.setText(SecondBtnText)


        dialogOKButton.setOnClickListener {
            onOKButtonClickListener?.invoke()
            dialog.dismiss()
        }

        dialogCloseButton.setOnClickListener {
            onCloseButtonClickListener?.invoke()
            dialog.dismiss()
        }

        dialog.show()


    }

}