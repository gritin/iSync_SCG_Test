package com.example.isync.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.heifwriter.HeifWriter
import com.example.isync.R
import com.example.isync.adapter.GridViewParcelListAdapter
import com.example.isync.databinding.ActivityMainBinding
import com.example.isync.dialog.BottomSelectGetImageSolutions
import com.example.isync.dialog.OneButtonDialog
import com.example.isync.dialog.TwoButtonDialog
import com.example.isync.model.FirebaseGetImagesModel
import com.example.isync.services.CheckNetworkConnection
import com.example.isync.services.ImageResizer
import com.example.manager.ImageStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    private lateinit var parcelAdapter: GridViewParcelListAdapter
    private lateinit var checkNetworkConnection: CheckNetworkConnection
    private lateinit var  binding: ActivityMainBinding
    private var imageList : ArrayList<Uri> = arrayListOf()
    private var imageTypeList: ArrayList<String> = arrayListOf()
    private var statusList : ArrayList<String> = arrayListOf()
    private var uploadList : ArrayList<String> = arrayListOf()
    private var connection_checkr = false
    private var pngCount :Int = 0
    private var jpgCount :Int = 0
    private var heicCount :Int = 0
    private val CAMERA_REQUEST = 200
    private val GALLERY_REQUEST = 100
    private val PREVIEW_REQUEST = 300

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var storageReference : StorageReference
    lateinit var databaseReference: DatabaseReference
    private lateinit var responseImage : MutableList<FirebaseGetImagesModel>



    var progressDialog : ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainMenuBtn.setOnClickListener {
            onSetMainMenu(true)
        }
        binding.menuCloseBtn.setOnClickListener {
            onSetMainMenu(false)
        }



        binding.addBtn.setOnClickListener {
            val alert = BottomSelectGetImageSolutions()
            alert.showDialog(this)
            alert.onUploadClickListener = {
                onSetPermission(GALLERY_REQUEST)
                alert.onDismiss()
            }

            alert.onTakePhotoClickListener = {
                onSetPermission(CAMERA_REQUEST)
                alert.onDismiss()
            }

            alert.onCancelClickListener = {
                alert.onDismiss()
            }
        }


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.getReferenceFromUrl("gs://isync-3c487.appspot.com/images")

        databaseReference = firebaseDatabase.getReference("images/${firebaseAuth.currentUser!!.uid}")
        responseImage = mutableListOf()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Uploading")

        binding.logOutButton.setOnClickListener {
            firebaseAuth.signOut()
            this.finish()
        }

        binding.mainAccount.text = firebaseAuth.currentUser!!.email

        // set View
        onSetMainMenu(false)

        callNetworkConnection()
        // get cache image

        val cR : ContentResolver = this.contentResolver
        val mime : MimeTypeMap = MimeTypeMap.getSingleton()

            val offline_size = ImageStoreManager(this).getNumCacheIndex()
            for (i in 0..offline_size-1){
                val uri : Uri = Uri.parse(ImageStoreManager(this).getImageCache(i,firebaseAuth.currentUser!!.uid))
                var type : String? = mime.getExtensionFromMimeType(cR.getType(uri))
                if(type== null){
                    type = "heic"
                }
                imageList.add(uri)
                imageTypeList.add(type)
                statusList.add("offline")
                uploadList.add("-")


                checkImageType(type,true)
                onSetImageCount()
            }
        // get image from api
        GetImagesAPI()

        onSetGridview()

    }

    override fun onResume() {
        super.onResume()
        parcelAdapter.notifyDataSetChanged()
    }

    private fun UploadImage(filePath : Uri,type:String,position: Int){

            val imagename = "upload_"+LocalDateTime.now().toString()+".$type"
            val childRef: StorageReference = storageReference.child(imagename.toString())
        val reduceUri : Uri
            if(type!="heic") {
                reduceUri = imageResize(filePath, type)
            }else{
                reduceUri = filePath
            }
            val uploadTask = childRef.putFile(reduceUri)
            uploadTask.addOnSuccessListener {
                submitData(imagename,type,position)
            }.addOnFailureListener { e ->
                Toast.makeText(this,"fail $e",Toast.LENGTH_LONG).show()

            }


    }

    private fun submitData(imageName : String,type: String,position:Int){
        val databaseReference = firebaseDatabase.reference.child("images/${firebaseAuth.currentUser!!.uid}").push()
        databaseReference.child("type").setValue(type)
        databaseReference.child("imageName").setValue(imageName)
        databaseReference.child("timestamp").setValue(LocalDateTime.now().toString())

        Toast.makeText(this,"Uploaded",Toast.LENGTH_LONG).show()

        val childRef: StorageReference = storageReference.child(imageName)
        childRef.downloadUrl.addOnSuccessListener {
            imageList.set(position,it)

        }

        statusList.set(position,"online")
        uploadList.set(position,LocalDateTime.now().toString())

        val num = ImageStoreManager(this).getNumCacheIndex()
        for (i in 0..num-1){
            if(i == num-1){
                ImageStoreManager(this).deleteImageCache(i, firebaseAuth.currentUser!!.uid)
            }
            val newUri = ImageStoreManager(this).getImageCache(i+1,firebaseAuth.currentUser!!.uid)
            ImageStoreManager(this).addImageCache(i,firebaseAuth.currentUser!!.uid, Uri.parse(newUri))
        }

        var offlineNum : Int = 0
        for (item in statusList){
            if (item == "offline"){
                offlineNum += 1
            }
        }

        ImageStoreManager(this).setINumCacheIndex(offlineNum)

        checkImageType(type,false)
        onSetImageCount()



        parcelAdapter.notifyDataSetChanged()

    }

    private fun GetImagesAPI(){
        var index = 0
        databaseReference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                responseImage.add(snapshot.getValue(FirebaseGetImagesModel::class.java)!!)
                val type = responseImage[index].type
                val uploadDate = responseImage[index].timestamp
                val childRef: StorageReference = storageReference.child(responseImage[index].imageName.toString())
                childRef.downloadUrl.addOnSuccessListener {

                    imageList.add(it)
                    if (type != null) {
                        imageTypeList.add(type)
                    }
                    statusList.add("online")
                    uploadList.add(uploadDate!!)
                    parcelAdapter.notifyDataSetChanged()
                }
                index +=1
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



    }

    private fun onSetMainMenu(open: Boolean) {
        if (open) {
            Handler().postDelayed({
                binding.drawerLayout.openDrawer(GravityCompat.END)
            }, 200)


        } else {
            Handler().postDelayed({
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            }, 200)

        }
    }

    private fun onSetGridview(){
        parcelAdapter = GridViewParcelListAdapter(this,imageList,imageTypeList,statusList)
        binding.gridShowImage.apply {
            adapter = parcelAdapter
        }.also {
            it.onItemClickListener = AdapterView.OnItemClickListener{ adapterView, view, position, sth ->
                // click img for show fullscreen
                Intent(applicationContext,PreviewActivity::class.java).apply {

                    putExtra("ImageIndex",position)
                    putExtra("statusArray",statusList)
                    putExtra("typeArray",imageTypeList)
                    putExtra("imageList", imageList)
                    putExtra("uploadList",uploadList)
                }.run {
                    startActivityForResult(this,PREVIEW_REQUEST)
                }
            }
        }
    }

    private fun callNetworkConnection() {
        checkNetworkConnection = CheckNetworkConnection(application)
        checkNetworkConnection.observe(this,{ isConnected ->
            onSetNetworkStatus(isConnected)
            connection_checkr = isConnected

            val cR : ContentResolver = this.contentResolver
            val mime : MimeTypeMap = MimeTypeMap.getSingleton()

            if (isConnected){
                if (statusList.contains("offline")){
                    for (i in 0..statusList.size-1){
                        if(statusList[i] == "offline"){
                            var type : String? = mime.getExtensionFromMimeType(cR.getType(imageList[i]))
                            if (type == null){
                                type = "heic"
                            }
                            UploadImage(imageList[i], type,i )
                        }
                    }
                }
            }
        })
    }

    private fun onSetNetworkStatus(isConnected:Boolean){
        if (isConnected){
            binding.networkStatus.text = "Connected"
            binding.networkStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green))
        }else{
            binding.networkStatus.text = "Not Connected"
            binding.networkStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
        }
    }

    private fun onSetPermission(selecter:Int) {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report != null) {
                    if(report.areAllPermissionsGranted()){
                        var selectIntent : Intent

                        if (selecter == CAMERA_REQUEST){
                            selectIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        }else{

                                selectIntent = Intent(Intent.ACTION_PICK)
//                                selectIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//                                selectIntent.addCategory(Intent.CATEGORY_OPENABLE)
                                selectIntent.type = "image/*"
                                val extraMimeType = arrayOf("image/png", "image/jpg", "image/jpeg","image/heif")
                                selectIntent.putExtra(Intent.EXTRA_MIME_TYPES,extraMimeType)

                        }

                        startActivityForResult(selectIntent,selecter)
                    }
                    if (report.isAnyPermissionPermanentlyDenied){
                        onSetOpenSettingDialog()
                    }

                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }).check()

    }

    private fun onSetOpenSettingDialog(){
        val alert = TwoButtonDialog()
        alert.showDialog(this,"Please allow permission","allow","cancel")
        alert.onOKButtonClickListener = {
            var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${this.packageName}")).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val cR : ContentResolver = this.contentResolver
        val mime : MimeTypeMap = MimeTypeMap.getSingleton()

        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQUEST){
                // if multiple images are selected
                if (data?.getClipData() != null) {

                    var count = data.clipData!!.itemCount

                    for (i in 0..count - 1) {
                        var imageUri: Uri = data.clipData!!.getItemAt(i).uri

                        var type = mime.getExtensionFromMimeType(cR.getType(imageUri))
                        if (type == null){
                            type = "heic"
                        }
                        onSetImageStore(type,imageUri)
                    }

                } else if (data?.getData() != null) {
                    // if single image is selected

                    var imageUri: Uri = data.data!!
                    var type = mime.getExtensionFromMimeType(cR.getType(imageUri))
                    if (type == null){
                        type = "heic"
                    }
                    onSetImageStore(type,imageUri)
                }
            }

            if (requestCode == CAMERA_REQUEST){
                // get img from Camera
                var byte: ByteArrayOutputStream = ByteArrayOutputStream()
                var bitmap = data?.extras?.get("data") as Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byte)

                var path = MediaStore.Images.Media.insertImage(
                    this.contentResolver,
                    bitmap,
                    "upload_img",
                    null
                )
                var newUri = Uri.parse(path)

                val type = mime.getExtensionFromMimeType(cR.getType(newUri))

                onSetImageStore(type,newUri)
            }

            if (requestCode == PREVIEW_REQUEST){
                val callbacks : Boolean = data!!.getSerializableExtra("isChange") as Boolean
                if(callbacks){
                   val intent = intent
                   finish()
                   startActivity(intent)
               }
            }


        }
    }

    private fun checkImageType(type: String?,isAdd:Boolean) : Boolean{
        when(type){
            "png" -> {
                if (isAdd) {
                    if (pngCount < 40) {
                        pngCount += 1
                        return true
                    }
                }else{
                    pngCount -= 1
                    return true
                }
            }
            "jpg" ->{
                if (isAdd) {
                    if (jpgCount < 40) {
                        jpgCount += 1
                        return true
                    }
                }else{
                    jpgCount -= 1
                    return true
                }
            }
            "heic" ->{
                if (isAdd) {
                    if (heicCount < 20) {
                        heicCount += 1
                        return true
                    }
                }else{
                    heicCount -= 1
                    return true
                }
            }
        }
        return false
    }

    private fun onSetImageStore(type: String?, imageUri: Uri) {
        if(checkImageType(type,true)){
            onSetImageCount()

            // get offline
            imageList.add(imageUri)
            imageTypeList.add(type!!)
            statusList.add("offline")
            uploadList.add("-")

            var storeIndex = ImageStoreManager(this).getNumCacheIndex()
            ImageStoreManager(this).addImageCache(storeIndex,firebaseAuth.currentUser!!.uid,imageUri)
            ImageStoreManager(this).setINumCacheIndex(storeIndex+1)

            parcelAdapter.notifyDataSetChanged()

        }else{
            limitDialogShow(type!!)
        }
    }

    private fun limitDialogShow(type:String) {
        val alert = OneButtonDialog()
        val msg = type+" type is limited"
        alert.showDialog(this,msg,"Close")

    }

    private fun onSetImageCount() {
        binding.menuJpgCount.text = jpgCount.toString()
        binding.menuPngCount.text = pngCount.toString()
        binding.menuHeicCount.text = heicCount.toString()
    }

    private fun imageResize(imageUri: Uri,type: String) : Uri{
//       1mb = 1,048,576 byte
        val max_size : Int = 1048576 // 262144  <- // 1mb/rgba

        val fullSizeBitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
        val reduceBitmap = ImageResizer().reduceBitmapSize(fullSizeBitmap,max_size)
        val desc = "${Environment.getExternalStorageDirectory()}${File.separator}/reduced_file.$type"

        val file:File = File(desc)
        file.createNewFile()
        val bos : ByteArrayOutputStream = ByteArrayOutputStream()
        if (type == "png"){
            reduceBitmap.compress(Bitmap.CompressFormat.PNG,80,bos)
        }else {
            reduceBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
        }
//        }else{
//            try {
//                HeifWriter.Builder(desc,fullSizeBitmap.width,fullSizeBitmap.height,HeifWriter.INPUT_MODE_BITMAP)
//                    .setQuality(80)
//                    .build().run {
//                        start()
//                        addBitmap(fullSizeBitmap)
//                        stop(0)
//                        close()
//                    }
//
//            }catch (e:Exception){
//                e.printStackTrace()
//
//            }
//        }

        val bitmapdata = bos.toByteArray()
        try {
            val fos : FileOutputStream = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()

            return Uri.fromFile(file)
        }catch (e : Exception){
            e.printStackTrace()
        }
        return Uri.fromFile(file)

    }

}