package com.example.isync.dialog

import android.app.Activity
import android.app.Dialog
import android.opengl.Visibility
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.isync.R

class OneButtonDialog {

    var onOKButtonClickListener:(() -> Unit)? = null

    private lateinit var contentText : TextView

    fun showDialog(activity: Activity?, title: String?,FirstbtnText:String?,is_message:Boolean=false,msg:String="") {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )


        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_one_button)
        val titleText = dialog.findViewById<View>(R.id.dialog_one_title) as TextView
        titleText.text = title

        val subTitleText = dialog.findViewById<View>(R.id.dialog_one_subtitle) as TextView
        subTitleText.text = msg
        if (is_message){
            subTitleText.visibility = View.VISIBLE
        }else{
            subTitleText.visibility = View.INVISIBLE
        }

        val dialogOKButton: Button = dialog.findViewById<View>(R.id.dialog_one_ok) as Button
        dialogOKButton.setText(FirstbtnText)


        dialogOKButton.setOnClickListener {
            onOKButtonClickListener?.invoke()
            dialog.dismiss()
        }


        dialog.show()


    }



}