package com.example.isync.dialog

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.example.isync.R

class LoadingDialog(private val activity: Activity) {
    private var dialog : AlertDialog? = null

    fun startLoadingDialog(){
        val builder = AlertDialog.Builder(activity)
        val inflater : LayoutInflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_loading,null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog?.show()
    }

    fun dismiss(){
        dialog!!.dismiss()
    }
}