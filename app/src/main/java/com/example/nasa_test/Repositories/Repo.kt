package com.example.nasa_test.Repositories

import com.example.nasa_test.JsonClasses.SearchResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

abstract class Repo {
    // https://images-api.nasa.gov/
    // api_key=nk9JVphmGTh1Wvg2qKMhk3GTWgvmJTaLDiBVWZro
    // https://api.nasa.gov/planetary/apod?api_key=nk9JVphmGTh1Wvg2qKMhk3GTWgvmJTaLDiBVWZro
    // , @Query("api_key") apiKey: String
    interface NASA_API{

        @GET("search")
        fun search(
            @Query("q") query: String? = null,
            @Query("center") center: String?= null,
            @Query("page") page: Int? = null,
            @Query("nasa_id") nasaId: String?=null
        ): Call<SearchResponse>

        @GET("asset/{id}")
        fun asset(@Path("id") nasaId: String): Call<SearchResponse>
    }


    companion object{
        fun getRepo() = Retrofit.Builder()
            .baseUrl("https://images-api.nasa.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NASA_API::class.java)
    }
}