package com.example.nasa_test.JsonClasses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Item(val href: String, val links: List<Link>? = null, val data: List<NasaData>? = null) {
}

data class Link(val href: String, val render: String, val rel: String)
@Serializable
data class NasaData(
    @SerializedName("nasa_id")
    @Expose
    val nasaId: String,
    val title: String?=null,
    val keywords: List<String>?=null,
    val center: String?=null,
    @SerializedName("date_created")
    @Expose
    val dateCreated: String?=null,
    @SerializedName("media_type")
    @Expose
    val mediaType: String,
    val photographer: String?=null,
    val description: String?=null
    //val other: Map<String, Any?>?=null
): java.io.Serializable