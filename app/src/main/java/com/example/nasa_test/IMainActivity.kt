package com.example.nasa_test

import com.example.nasa_test.DataClasses.NasaNode

interface IMainActivity: MediaPlayerForegroundService.ServiceOwner {
    fun updateRecycler(list: List<NasaNode>, needRefresh: Boolean)
    fun setTotalHits(totalHits: String)
    fun updateCurrentCount(added: Int)
    fun displayItemDetail(node: NasaNode)

    fun updateProgressBar()
}