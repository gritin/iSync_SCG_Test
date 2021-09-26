package com.example.isync.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.isync.R

class BottomSelectGetImageSolutions {

    var onUploadClickListener:(() -> Unit)? = null
    var onTakePhotoClickListener:(() -> Unit)? = null
    var onCancelClickListener:(() -> Unit)? = null

    lateinit var dialog : Dialog
    private lateinit var contentText : TextView

    fun showDialog(activity: Activity?) {

        dialog = Dialog(activity!!)


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        var layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(dialog.window?.attributes)

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window?.attributes = layoutParams


        dialog.window?.setGravity(Gravity.BOTTOM)

        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_bottom_get_image_solution)


        val uploadPhoto = dialog.findViewById<View>(R.id.bottom_get_image_solution_upload_photo) as TextView
        val takePhoto = dialog.findViewById<View>(R.id.bottom_get_image_solution_take_a_photo) as TextView
        val cancel = dialog.findViewById<View>(R.id.bottom_get_image_solution_cancel) as AppCompatButton


        uploadPhoto.setOnClickListener {
            onUploadClickListener?.invoke()

        }

        takePhoto.setOnClickListener {
            onTakePhotoClickListener?.invoke()
        }

        cancel.setOnClickListener {
            onCancelClickListener?.invoke()
        }

        dialog.show()
    }


    fun onDismiss(){
        dialog.dismiss()
    }

}