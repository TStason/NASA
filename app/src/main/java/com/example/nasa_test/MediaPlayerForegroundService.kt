package com.example.nasa_test

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.MediaController
import java.lang.Exception


class MediaPlayerForegroundService: Service(), MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl {

    private val TAG = "NASA_MEDIA_PLAYER_SERVICE"

    var mediaPlayer = MediaPlayer()

    private val binder = VideoBinder()
    private var URL: String? =null

    fun setUrl(url: String?){
        Log.d(TAG, "setUrl -> $url")
        URL = url
        try{
            setMediaSource()
        } catch (ex: Exception){
            Log.e(TAG, "Error ${ex.localizedMessage}")
        }
    }

    private fun setMediaSource(){
        mediaPlayer.setDataSource(URL)
    }
    // after prep -> start()
    fun prepareAsync(){
        Log.d(TAG, "prepareAsync -> ${mediaPlayer}")
        URL?.let{
            try{

                mediaPlayer.prepareAsync()
            }
            catch (ex: Exception){
                Log.e(TAG, "Error ${ex.localizedMessage}")
            }
        } ?: Log.e(TAG, "prepareAsync source -> $URL")

    }

    fun releaseMediaPlayer(){
        mediaPlayer.release()
        mediaPlayer = MediaPlayer()
    }

    private fun stopMedia(){
        Log.d(TAG, "stopMedia")
        if (mediaPlayer.isPlaying)
            mediaPlayer.pause()
        mediaPlayer.stop()
    }

    private fun resetMediaPlayer(){
        Log.d(TAG, "resetMediaPlayer")
        stopMedia()
        mediaPlayer.reset()
    }
    // MediaPlayer.OnPreparedListener
    override fun onPrepared(mp: MediaPlayer?) {
        Log.d(TAG, "onPrepared")
        //mediaPlayer.start()
    }
    // Service
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener(this)
        super.onCreate()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return binder
    }
    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
    }
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return true
    }
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopMedia()
        mediaPlayer.release()
        super.onDestroy()
    }

    // MediaController.MediaPlayerControl
    override fun isPlaying(): Boolean = mediaPlayer.isPlaying
    override fun canSeekForward(): Boolean = true
    override fun getDuration(): Int = mediaPlayer.duration
    override fun pause() = mediaPlayer.pause()
    override fun getBufferPercentage(): Int = 0
    override fun seekTo(pos: Int) = mediaPlayer.seekTo(pos)
    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition
    override fun canSeekBackward(): Boolean = true
    override fun start() = mediaPlayer.start()
    override fun getAudioSessionId(): Int = mediaPlayer.audioSessionId
    override fun canPause(): Boolean = true


    inner class VideoBinder(): Binder(){
        fun getService() = this@MediaPlayerForegroundService
    }
    interface ServiceOwner{
        fun BindService(serviceConnection: ServiceConnection)
        fun UnbindService(serviceConnection: ServiceConnection)
        fun StartService()
        fun StopService()
    }
}