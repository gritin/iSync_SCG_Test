package com.example.isync.activity

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.isync.R
import com.example.isync.adapter.SliderImageAdapter
import com.example.isync.databinding.ActivityMainBinding
import com.example.isync.databinding.ActivityPreviewImageBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import android.provider.MediaStore
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.isync.services.DownloadAndSaveImageTask
import com.example.manager.ImageStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList
import com.google.firebase.database.DataSnapshot





class PreviewActivity : AppCompatActivity() {

    private lateinit var SliderImageAdapter : SliderImageAdapter
    private lateinit var mViewPager: ViewPager2
    private lateinit var binding: ActivityPreviewImageBinding
    private var selectedPosition : Int = 0
    private lateinit var statusList : ArrayList<String>
    private lateinit var imageTypeList : ArrayList<String>
    private lateinit var imageList : ArrayList<Uri>
    private lateinit var imageUploadList : ArrayList<String>
    private var removeList:ArrayList<Int> = arrayListOf()
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var storageReference : StorageReference
    lateinit var databaseReference: DatabaseReference


    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        SliderImageAdapter.notifyDataSetChanged()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreviewImageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // get image Data
         selectedPosition = intent.getSerializableExtra("ImageIndex") as Int
         statusList  = intent.getSerializableExtra("statusArray") as ArrayList<String>
         imageTypeList  = intent.getSerializableExtra("typeArray") as ArrayList<String>
         imageList  = intent.getSerializableExtra("imageList") as ArrayList<Uri>
         imageUploadList  = intent.getStringArrayListExtra("uploadList") as ArrayList<String>

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        storageReference = firebaseStorage.getReferenceFromUrl("gs://isync-3c487.appspot.com/images")

        mViewPager = binding.previewViewPager
        SliderImageAdapter = SliderImageAdapter(imageList)

        mViewPager.apply {
            adapter = SliderImageAdapter
        }

        mViewPager.setCurrentItem(selectedPosition)

        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.d("preview", "onCreate: $position")
                onSetImageDescription(position)
            }
        })

        binding.previewClose.setOnClickListener {
            onSetCallback()
        }

        binding.previewSave.setOnClickListener {
            val index = mViewPager.currentItem
            onSaveImage(imageList[index],imageTypeList[index],statusList[index])
        }

        binding.previewDelete.setOnClickListener {
            val index = mViewPager.currentItem
            onDeleteImage(index,statusList[index])
        }

    }

    fun onSetCallback(){
        if (removeList.isEmpty()){
            intent.putExtra("isChange",false)
        }else{
            intent.putExtra("isChange",true)
        }

        setResult(RESULT_OK,intent)
        this.finish()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        onSetCallback()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDeleteImage(index: Int, status: String) {
        Log.d("delete", "onDeleteImage: $status")
        if (status == "online"){
            storageReference = firebaseStorage.getReferenceFromUrl(imageList[index].toString())
            databaseReference = firebaseDatabase.getReference("images/${firebaseAuth.currentUser!!.uid}")
            val rawImageName : String = imageList[index].toString()
            val ImageNameAndToken = rawImageName.split("images%2F")
            val ImageNameSp = ImageNameAndToken[1].split("?alt")
            var ImageName = ImageNameSp[0]
            val oldValue = "%3A"
            val newValue = ":"
            ImageName = ImageName.replace(oldValue,newValue)
            val query : Query = databaseReference.orderByChild("imageName").equalTo(ImageName)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (appleSnapshot in snapshot.getChildren()) {
                        appleSnapshot.ref.removeValue()
                    }
                    storageReference.delete().addOnSuccessListener {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

                Toast.makeText(this,"Image deleted",Toast.LENGTH_SHORT).show()

            removeList.add(index)

            if(imageList.size==1){
                onSetCallback()
            }else {
                imageList.removeAt(index)
                statusList.removeAt(index)
                imageTypeList.removeAt(index)
                imageUploadList.removeAt(index)
                SliderImageAdapter.notifyDataSetChanged()
            }
            }
        else{
            var offlineList : ArrayList<Int> = arrayListOf()

             for(i in 0..statusList.size-1){
                 if(statusList[i] == "offline"){
                     offlineList.add(i)
                 }
             }

            val offlineRealIndex = offlineList.indexOf(index)
            ImageStoreManager(this).setINumCacheIndex(offlineList.size-1)

            for (i in offlineRealIndex..offlineList.size-1){
                if(i == offlineList.size-1){
                    ImageStoreManager(this).deleteImageCache(i, firebaseAuth.currentUser!!.uid)
                }
                val newUri = ImageStoreManager(this).getImageCache(i+1,firebaseAuth.currentUser!!.uid)
                ImageStoreManager(this).addImageCache(i,firebaseAuth.currentUser!!.uid, Uri.parse(newUri))
            }
            Toast.makeText(this,"Image deleted",Toast.LENGTH_SHORT).show()

            removeList.add(index)

            if(imageList.size==1){
                onSetCallback()
            }else {
                imageList.removeAt(index)
                statusList.removeAt(index)
                imageTypeList.removeAt(index)
                imageUploadList.removeAt(index)
                SliderImageAdapter.notifyDataSetChanged()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    private fun onSetImageDescription(position: Int) {
        // set up date

        var rawDate = imageUploadList[position]
        var dateTime = "-"
        if (rawDate != "-") {
            var date = rawDate.split("T")
            var time = date[1].split(":")
            dateTime = date[0] + " " + time[0] + ":" + time[1]
        }

        binding.previewImageType.text = imageTypeList[position]
        binding.previewUploadDate.text = dateTime
        binding.previewImageStatus.text = statusList[position]

        if(statusList[position] == "online"){
            binding.previewImageStatus.setTextColor(getColor(R.color.status_green))
        }else{
            binding.previewImageStatus.setTextColor(getColor(R.color.status_red))
        }

    }

    private fun onSaveImage(imageUri:Uri,type:String, status:String) {
        if(status == "offline") {
            val desc = "${Environment.getExternalStorageDirectory()}${File.separator}/"

            var file = File(desc,"iSyncImage")
            if(!file.exists()){
                file.mkdir()
            }
            file = File(file,"img_${UUID.randomUUID()}.$type")
            val inputStream = contentResolver.openInputStream(imageUri)
            val out: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1048576)
            var len: Int
            while (inputStream!!.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            inputStream.close()
            Toast.makeText(this,"Image Saved",Toast.LENGTH_SHORT).show()
        }else{
            // load Image
            DownloadAndSaveImageTask(this,type).execute(imageUri.toString()).let {
                if (type != "heic") {
                    Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this,"Sorry this image type not available to download now.",Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}