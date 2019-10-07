package com.example.nasa_test.Presenter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.nasa_test.DataClasses.NasaNode

interface IPresenter {

    fun searchButtonPressed(request: String)

    fun getOnClickAction(): (NasaNode?) -> Unit
    fun getOnScrollListener(): RecyclerView.OnScrollListener

    fun onActivityResume()
    fun onActivityStop()
    fun onActivityDestroy()
}