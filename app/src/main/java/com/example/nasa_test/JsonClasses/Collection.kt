package com.example.nasa_test.JsonClasses

import com.google.gson.annotations.SerializedName

class Collection(
    val links: List<CollLink>? = null,
    val version: String? = null,
    val items: List<Item>? = null,
    val href: String? = null,
    val metadata: Meta? = null,
    val other: Map<String, Any?>)

data class CollLink( val rel: String,//? = null,
    val prompt: String,//? = null,
    val href: String//? = null
    )
data class Meta(@SerializedName("total_hits") val totalHits: Int? = null)