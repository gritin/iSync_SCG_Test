package com.example.isync.services

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.google.android.gms.fido.fido2.api.common.RequestOptions
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.util.*

class DownloadAndSaveImageTask(context: Context,type:String) : AsyncTask<String, Unit, Unit>() {
    private var mContext: WeakReference<Context> = WeakReference(context)
    private var ImageType : String = type

    override fun doInBackground(vararg params: String?) {
        val url = params[0]
        val requestOptions = com.bumptech.glide.request.RequestOptions.overrideOf(1048576)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)

        mContext.get()?.let {
            val bitmap = Glide.with(it)
                .asBitmap()
                .load(url)
                .apply(requestOptions)
                .submit()
                .get()

            try {
                val desc = "${Environment.getExternalStorageDirectory()}${File.separator}/"
                var file = File(desc,"iSyncImage")
                if(!file.exists()){
                    file.mkdir()
                }
                file = File(file, "img_${UUID.randomUUID()}.$ImageType")
                Log.d("Seiggailion", "doInBackground: $file")
                val out = FileOutputStream(file)
                if (ImageType == "jpg"){
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }else if(ImageType == "png"){
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }else{
                    return@let
                }
                out.flush()
                out.close()
                Log.i("Seiggailion", "Image saved.")
                return@let
            } catch (e: Exception) {
                Log.i("Seiggailion", "Failed to save image.")
            }
        }
    }
}