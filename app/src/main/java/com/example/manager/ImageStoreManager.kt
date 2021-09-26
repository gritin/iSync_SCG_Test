package com.example.manager

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

open class ImageStoreManager(context: Context) {

    private val PREF_NAME = "isync"
    private val IMAGE_CACHE = "image_cache"
    private lateinit var mInstance : ImageStoreManager
    private var mContext = context
    private var  mSharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)

    fun addImageCache(index:Int,owner:String,imageUri:Uri){
        val key = IMAGE_CACHE+"_"+index+"_"+owner
        mSharedPreferences.edit().putString(key,imageUri.toString()).commit()
    }


    fun getImageCache(index: Int,owner: String) : String{
        val key = IMAGE_CACHE+"_"+index+"_"+owner
        return mSharedPreferences.getString(key,"").toString()
    }

    fun deleteImageCache(index: Int, owner: String){
        val key = IMAGE_CACHE+"_"+index+"_"+owner
        mSharedPreferences.edit().remove(key).apply()
    }

    fun setINumCacheIndex(num:Int){
        mSharedPreferences.edit().putInt(IMAGE_CACHE,num).apply()
    }

    fun getNumCacheIndex() : Int{
       return mSharedPreferences.getInt(IMAGE_CACHE,0)
    }



}