package com.example.isync.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.isync.R
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso


class SliderImageAdapter(private var mImageList : ArrayList<Uri>) : RecyclerView.Adapter<SliderImageAdapter.ImageViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_image_slider,parent,false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(mImageList[position])
    }


    override fun getItemCount(): Int = mImageList.size

    class ImageViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val sliderImageView = itemView.findViewById<ShapeableImageView>(R.id.view_image_slider_image)

        fun bind(data:Uri){
            Picasso.get().load(data).into(sliderImageView)
        }
    }

}