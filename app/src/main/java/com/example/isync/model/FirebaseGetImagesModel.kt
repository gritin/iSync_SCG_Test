package com.example.isync.model

import java.util.HashMap

class FirebaseGetImagesModel {
    var imageName: String? = null
    var type:String? = null
    var timestamp : String? = null
    constructor()
    constructor(imageName:String?,type:String?,timestamp:String?){
        this.imageName = imageName
        this.type = type
        this.timestamp = timestamp
    }

    fun toMap():Map<String,Any>{
        val result = HashMap<String,Any>()
        result.put("imageName",imageName!!)
        result.put("timestamp",timestamp!!)
        result.put("type",type!!)

        return result
    }
}