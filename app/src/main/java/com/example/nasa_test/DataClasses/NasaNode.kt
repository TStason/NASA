package com.example.nasa_test.DataClasses

import com.example.nasa_test.JsonClasses.NasaData
import kotlinx.serialization.Serializable

@Serializable
data class NasaNode(val nasaData: NasaData, val mediaLinks: List<String>): java.io.Serializable