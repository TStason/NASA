package com.example.nasa_test

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nasa_test.DataClasses.NasaNode
import com.example.nasa_test.MediaController.NasaAudioMediaController
import com.example.nasa_test.Presenter.AudioDetailPresenter
import com.example.nasa_test.Presenter.VideoDetailPresenter
import com.example.nasa_test.Presenter.ImageDetailPresenter
import com.example.nasa_test.Presenter.IDetailPresenter

class DetailActivity: AppCompatActivity(),  IDetailActivity {

    private val TAG = "NASA_DATAIL_ACTIVITY"

    private var presenter: IDetailPresenter? = null

    //
    private var titleView: TextView? = null
    private var descriptionView: TextView? = null
    private var keywordsView: TextView? = null
    //
    private var imageView: ImageView? = null
    private var surfaceView: SurfaceView? = null
    private var nasaAudioController: NasaAudioMediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate -> $this")
        super.onCreate(savedInstanceState)
        if (intent.hasExtra("nasa_node")){
            val tmp: NasaNode = intent.getSerializableExtra("nasa_node") as NasaNode
            when(tmp.nasaData.mediaType){
                "image" -> {
                    setContentView(R.layout.activity_detail_image)
                    presenter = ImageDetailPresenter(this)
                    imageView = findViewById(R.id.image)
                }
                "video" -> {
                    setContentView(R.layout.activity_detail_video)
                    presenter = VideoDetailPresenter(this, MediaController(this))
                    surfaceView = findViewById(R.id.surface)
                }
                "audio" -> {
                    setContentView(R.layout.activity_detail_audio)
                    presenter = AudioDetailPresenter(this)
                    nasaAudioController = findViewById(R.id.mediaController)
                }
            }
        }
        titleView = findViewById(R.id.title)
        keywordsView = findViewById(R.id.keywords)
        descriptionView = findViewById(R.id.description)

        presenter?.onActivityCreate(intent.extras)
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        presenter?.onActivityResume()
        super.onResume()
    }
    override fun onStop(){
        Log.d(TAG, "onStop")
        presenter?.onActivityStop()
        presenter = null
        super.onStop()
    }
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        presenter?.onActivityDestroy()
        super.onDestroy()
    }

    // IDetailActivity
    override fun getMediaView(): View? {
        presenter?.let{
            when(it){
                is VideoDetailPresenter -> return surfaceView
                is AudioDetailPresenter -> return nasaAudioController
                is ImageDetailPresenter -> return imageView
                else -> return null
            }
        }
        return null
    }
    override fun setTitle(text: String?) {
        titleView?.text = text
    }
    override fun setDescription(text: String?) {
        descriptionView?.text = text
    }
    override fun setKeywords(text: String?) {
        keywordsView?.text = text
    }

    // Owner
    override fun BindService(serviceConnection: ServiceConnection) {
        val intent = Intent(this, MediaPlayerForegroundService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    override fun UnbindService(serviceConnection: ServiceConnection) {
        unbindService(serviceConnection)
    }

    override fun StartService() {
    }
    override fun StopService() {
    }
}

interface IDetailActivity: MediaPlayerForegroundService.ServiceOwner{
    fun getMediaView(): View?
    fun setTitle(text: String?)
    fun setDescription(text: String?)
    fun setKeywords(text: String?)

}