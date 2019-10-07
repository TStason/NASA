package com.example.nasa_test.Presenter

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nasa_test.DataClasses.NasaNode
import com.example.nasa_test.IMainActivity
import com.example.nasa_test.JsonClasses.SearchResponse
import com.example.nasa_test.Repositories.Repo
import kotlinx.coroutines.*
import retrofit2.await
import java.lang.Exception
import java.util.regex.Pattern

class Presenter(var view: IMainActivity): IPresenter {
    private val TAG = "TEST_NASA_MAIN_Presenter"

    private val job = SupervisorJob()
    private val presenterScope = CoroutineScope(Dispatchers.IO + job)
    private var isUpdating = false
    private var needRefresh = false
    private val countDataInPortion = 8

    private var currentRequest: String? = null
    private var nextRequestPage: Int? = null

    private var currentTotalHits = 0

    private val apiKey = "nk9JVphmGTh1Wvg2qKMhk3GTWgvmJTaLDiBVWZro"
    private val repo = Repo.getRepo()

    init{
        view.StartService()
    }

    override fun searchButtonPressed(request: String) {
        sendRequest(request, true)
    }

    private fun sendRequest(request: String?, needRefreshRecycle: Boolean, page: Int?=null, nasaId: String?=null){
        if (isUpdating){
            Log.e(TAG, "ALREADY UPDATING")
            return
        }
        isUpdating = true
        currentRequest = request
        var needRefresh = needRefreshRecycle
        // send on UI
        // start updating circle :)
        view.updateProgressBar()

        presenterScope.launch{
            val result = mutableListOf<NasaNode>()
            var readyToNotifyCount = 0
            var assetResult: SearchResponse? = null
            val searchResult = getSearchResponseAsync(request, page, nasaId).await()
            nextRequestPage = getNextRequestPage(searchResult)
            currentTotalHits = getTotalHits(searchResult)
//            Log.d(TAG, "current result -> ${searchResult?.collection?.items?.size}")
            searchResult?.collection?.items?.forEach {
                it.data?.first()?.let{ nasa_data ->
                    if (readyToNotifyCount < countDataInPortion) {
                        assetResult = getAssetResponseAsync(nasa_data.nasaId).await()
                        result.add(NasaNode(nasa_data, getMediaUrlsFromResponse(assetResult)))
                        readyToNotifyCount++

                    } else {
                        notifyResult(result, needRefresh).join()
                        needRefresh = false
                        result.clear()
                        assetResult = getAssetResponseAsync(nasa_data.nasaId).await()
                        result.add(NasaNode(nasa_data, getMediaUrlsFromResponse(assetResult)))
                        readyToNotifyCount = 1
                    }
                }
            }
            notifyResult(result, needRefresh).join()
            result.clear()
            // send on UI
            // stop updating circle :)
            withContext(Dispatchers.Main){
                isUpdating = false
                view.updateProgressBar()
            }
        }
    }

    private fun notifyResult(list: List<NasaNode>, needRefresh: Boolean) = presenterScope.launch(Dispatchers.Main){
        if(!list.isNullOrEmpty()){
            if(!needRefresh){
                view.setTotalHits(currentTotalHits.toString())
            }
            view.updateRecycler(list.map { it.copy() }, needRefresh)
            view.updateCurrentCount(list.size)
        }
        else {
            Log.e(TAG, "Nothing to update")
        }
    }
    private fun getSearchResponseAsync(query: String?, page: Int?=null, nasaId: String?=null): Deferred<SearchResponse?> = presenterScope.async {
        var res: SearchResponse? = null
        try{
            val tmp = repo.search(query = query, page = page, nasaId = nasaId)
            //Log.e(TAG, "${tmp.request()}")
            res = tmp.await()
        } catch (ex: Exception){
            Log.e(TAG, ex.message)
        }
        res
    }
    private fun getAssetResponseAsync(nasaId: String): Deferred<SearchResponse?> = presenterScope.async {
        var res: SearchResponse? = null
        try{
            val tmp = repo.asset(nasaId)
//            Log.e(TAG, "${tmp.request()}")
            res = tmp.await()
        } catch (ex: Exception){
            Log.e(TAG, ex.message)
        }
        res
    }
    private fun getMediaUrlsFromResponse(response: SearchResponse?): List<String>{
        val res = mutableListOf<String>()
        response?.collection?.items?.forEach {
            res.add(it.href)
        }
        return res
    }
    // Stub Magic
    private fun getNextRequestPage(response: SearchResponse?): Int?{
        response?.let{ resp ->
            Log.e(TAG, "Links ${resp.collection.links}")
            resp.collection.links?.let{ link ->
                val tmp = link.find { it.rel.contains("next") }?.href
                val pattern = Pattern.compile("page=\\d+")
                val matcher = pattern.matcher(tmp)
                val pageProp = if (matcher.find()) matcher.group(0) else null
                return pageProp?.substringAfter('=')?.toIntOrNull()
            }
        }
        return null
    }
    private fun getTotalHits(response: SearchResponse?): Int{
        response?.let {res ->
            res.collection.metadata?.let { meta ->
                return meta.totalHits ?: 0
            }
        }
        return 0
    }

    fun uploadMoreData(){
        Log.d(TAG, "Upload more data")
        nextRequestPage?.let{
            Log.d(TAG, "next request -> $nextRequestPage")
            sendRequest(currentRequest, false, page=it)
        } ?: Log.e(TAG, "Nothing to upload")
    }

    override fun getOnScrollListener() = object : RecyclerView.OnScrollListener(){
        var layoutManager: LinearLayoutManager? = null
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0){
                layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                layoutManager?.let{
                    if(it.itemCount - it.findLastCompletelyVisibleItemPosition() < 5)
                        uploadMoreData()
                }
            }
        }
    }
    override fun getOnClickAction(): (NasaNode?) -> Unit = { node ->
        node?.let{
            Log.e(TAG, "Clicked item ${it.nasaData.nasaId}")
            view.displayItemDetail(node)
        }
    }

    override fun onActivityResume() {
        Log.d(TAG, "onActivityResume")
    }
    override fun onActivityStop() {
        Log.d(TAG, "onActivityStop")
    }
    override fun onActivityDestroy() {
        Log.d(TAG, "onActivityDestroy")
        view.StopService()
        job.cancel("onActivityDestroy")
    }
}