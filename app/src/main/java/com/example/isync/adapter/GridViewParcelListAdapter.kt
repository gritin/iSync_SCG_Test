package com.example.isync.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.isync.R
import com.squareup.picasso.Picasso

class GridViewParcelListAdapter(

    val context: Context,
    private val imageList : ArrayList<Uri>,
    private val imageTypeList : ArrayList<String>,
    private val statusList :ArrayList<String>
) : BaseAdapter() {

    private var layoutInflater :LayoutInflater? = null
    private lateinit var imageView: ImageView
    private lateinit var statusView : ImageView
    private lateinit var imageType : TextView



    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {

        var converView = view
        if (layoutInflater == null){
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (converView == null){
            converView = layoutInflater!!.inflate(R.layout.view_upload_image,null)
        }

        imageView = converView!!.findViewById(R.id.view_upload_image)
        statusView = converView!!.findViewById(R.id.view_status)
        imageType = converView!!.findViewById(R.id.view_image_type)

        Picasso.get().load(imageList[position]).into(imageView)

        imageType.text = imageTypeList[position]

        if (statusList[position] == "online") {
            statusView.setImageResource(R.drawable.ic_img_status_online)
        }else{
            statusView.setImageResource(R.drawable.ic_img_status_offline)
        }

        return converView
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        Log.d("update adapter", "notifyDataSetChanged: active")
    }



}