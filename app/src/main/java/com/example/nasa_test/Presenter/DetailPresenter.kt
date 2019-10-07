package com.example.nasa_test.Presenter

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import android.widget.MediaController
import com.bumptech.glide.Glide
import com.example.nasa_test.DataClasses.NasaNode
import com.example.nasa_test.IDetailActivity
import com.example.nasa_test.MediaController.NasaAudioMediaController
import com.example.nasa_test.MediaPlayerForegroundService
import kotlin.properties.Delegates

class VideoDetailPresenter(val view: IDetailActivity, val mediaController: MediaController): IDetailPresenter {
    private val TAG = "NASA_VideoDetailPresenter"

    private var currentNode: NasaNode? = null
    private var URL: String? = null

    private var mediaService by Delegates.observable<MediaPlayerForegroundService?>(null){ prop, old, new ->
        Log.d(TAG, "mediaPlayerPropertyDelegat: surfaceCreated? -> $isSurfaceCreated")
        if (isSurfaceCreated){
            setServiceSetting((view.getMediaView() as SurfaceView).holder)
        }
    }
    private var serviceConnector: ServiceConnection? = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "serviceConnector: onServiceDisconnected")
            //reconnect
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "serviceConnector: onServiceConnected")
            val tmp = service as MediaPlayerForegroundService.VideoBinder
            mediaService = tmp.getService()
        }

    }
    private var isSurfaceCreated = false
    private val surfaceHolderCallback = object : SurfaceHolder.Callback{
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            Log.d(TAG, "surface.holder: surfaceChanged")
        }
        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            Log.d(TAG, "surface.holder: surfaceDestroyed")
            isSurfaceCreated = false
        }
        override fun surfaceCreated(holder: SurfaceHolder?) {
            Log.d(TAG, "surface.holder: surfaceCreated (service?->$mediaService)")
            setServiceSetting(holder)
            isSurfaceCreated = true
        }
    }
    init{
        view.BindService(serviceConnector!!)
    }

    fun setServiceSetting(surfaceHolder: SurfaceHolder?){
        Log.e(TAG, "Init Service")
        mediaService?.let {
            it.releaseMediaPlayer()
            it.setUrl(URL)
            it.mediaPlayer.setDisplay(surfaceHolder)
            mediaController.setMediaPlayer(mediaService)
            it.prepareAsync()
        } ?: Log.e(TAG, "mediaService -> null")
    }

    override fun onActivityCreate(extra: Bundle?){
        extra?.let {
            currentNode = it.getSerializable("nasa_node") as NasaNode
        } ?: Log.e(TAG, "Empty extra!")
        currentNode?.let{ node ->
            view.setTitle(node.nasaData.title)
            view.setDescription(node.nasaData.description)
            view.setKeywords(node.nasaData.keywords.toString())
            URL = node.mediaLinks.find { it.endsWith("mobile.mp4", true) }
        } ?: Log.e(TAG, "Empty node!")
        (view.getMediaView() as SurfaceView)?.let{
            it.holder.addCallback(surfaceHolderCallback)
            mediaController.setAnchorView(it)
            it.setOnClickListener {
                mediaController.show()
            }
        }
    }

    override fun onActivityResume() {
        Log.d(TAG, "onActivityResume")

    }
    override fun onActivityStop() {
        Log.d(TAG, "onActivityStop")
        (view.getMediaView() as SurfaceView)?.let{
            it.holder.removeCallback(surfaceHolderCallback)
            it.setOnClickListener(null)
            mediaService?.releaseMediaPlayer()
            view.UnbindService(serviceConnector!!)
            mediaService = null
            serviceConnector = null
        }
    }
    override fun onActivityDestroy() {
        Log.d(TAG, "onActivityDestroy")
    }
}

class ImageDetailPresenter(val view: IDetailActivity): IDetailPresenter{
    private val TAG = "NASA_ImageDetailPresenter"

    private var currentNode: NasaNode? = null

    override fun onActivityCreate(extra: Bundle?){
        extra?.let {
            currentNode = it.getSerializable("nasa_node") as NasaNode

        } ?: Log.e(TAG, "Empty extra!")
        currentNode?.let{ node ->
            view.setTitle(node.nasaData.title)
            view.setDescription(node.nasaData.description)
            view.setKeywords(node.nasaData.keywords.toString())
            Glide.with(view as Activity)
                .load(node.mediaLinks.find { it.contains("orig") })
                .into(view.getMediaView() as ImageView)
            //(view.getMediaView() as? ImageView)
        } ?: Log.e(TAG, "Empty node!")

    }

    override fun onActivityResume() {
        Log.d(TAG, "onActivityResume")
    }
    override fun onActivityStop() {
        Log.d(TAG, "onActivityStop")
    }
    override fun onActivityDestroy() {
        Log.d(TAG, "onActivityDestroy")
    }
}

class AudioDetailPresenter(val view: IDetailActivity): IDetailPresenter{
    val TAG: String = "NASA_AudioDetailPresenter"
    var currentNode: NasaNode? = null

    private var URL: String? = null
    private var mediaService: MediaPlayerForegroundService? = null
    private var serviceConnector: ServiceConnection? = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "serviceConnector: onServiceDisconnected")
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "serviceConnector: onServiceConnected")
            val tmp = service as MediaPlayerForegroundService.VideoBinder
            mediaService = tmp.getService()
            setServiceSetting()
        }
    }
    init {
        view.BindService(serviceConnector!!)
    }

    fun setServiceSetting(){
        Log.e(TAG, "Init Service")
        mediaService?.let {
            it.releaseMediaPlayer()
            it.setUrl(URL)
            (view.getMediaView() as NasaAudioMediaController).apply {
                this.setMediaPlayer(mediaService!!.mediaPlayer)
            }
            it.prepareAsync()
        } ?: Log.e(TAG, "mediaService -> null")
    }

    override fun onActivityCreate(extra: Bundle?) {
        Log.d(TAG, "onActivityCreate")
        extra?.let {
            currentNode = it.getSerializable("nasa_node") as NasaNode
        } ?: Log.e(TAG, "Empty extra!")
        currentNode?.let{ node ->
            view.setTitle(node.nasaData.title)
            view.setDescription(node.nasaData.description)
            view.setKeywords(node.nasaData.keywords.toString())
            URL = node.mediaLinks.find { it.contains(".mp3") }
            if (URL == null)
                URL = node.mediaLinks.find { it.contains(".m4a") }
            if (URL == null)
                URL = node.mediaLinks.find { it.contains(".wav") }
        } ?: Log.e(TAG, "Empty node!")
    }

    override fun onActivityResume() {
        Log.d(TAG, "onActivityResume")

    }
    override fun onActivityStop() {
        Log.d(TAG, "onActivityStop")
        mediaService?.let{
            (view.getMediaView() as NasaAudioMediaController).detachMediaPlayer()
        }
        mediaService?.releaseMediaPlayer()
        view.UnbindService(serviceConnector!!)
        mediaService = null
        serviceConnector = null
    }
    override fun onActivityDestroy() {
        Log.d(TAG, "onActivityDestroy")
    }
}

interface IDetailPresenter{
    fun onActivityCreate(extra: Bundle?)
    fun onActivityResume()
    fun onActivityStop()
    fun onActivityDestroy()
}