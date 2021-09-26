package com.example.isync.services

import android.graphics.Bitmap

class ImageResizer {

     fun reduceBitmapSize(bitmap: Bitmap, Max_size:Int) : Bitmap{


        val bitmap_height = bitmap.height
        val bitmap_width = bitmap.width
        val ratioImage: Double = ((bitmap_height*bitmap_width) / Max_size).toDouble()
        if(ratioImage <= 1){
            return bitmap
        }

        val ratio:Double = Math.sqrt(ratioImage)
        val newHeight : Int= Math.round(bitmap_height/ratio).toInt()
        val newWidth : Int= Math.round(bitmap_width/ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,true)


    }
}