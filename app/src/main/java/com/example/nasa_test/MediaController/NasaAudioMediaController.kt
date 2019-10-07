package com.example.nasa_test.MediaController

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import com.example.nasa_test.R
import kotlin.math.max
import kotlin.math.min

class NasaAudioMediaController: ViewGroup, MediaController.MediaPlayerControl {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    private val TAG = "TEST_TEST_NasaAudioMediaController"//"TEST_NasaAudioMediaController"
    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private val btnLeft = ImageButton(context).apply{
        this.maxHeight = DEFAULT_HEIGHT
        this.adjustViewBounds = true
        this.scaleType = ImageView.ScaleType.FIT_XY
        this.setBackgroundResource(R.drawable.ic_fast_rewind_black_24dp)
        this.isEnabled = false
        this.setOnClickListener {
            if (this@NasaAudioMediaController.isPlaying){
                val newPos = max(0, this@NasaAudioMediaController.getCurrentPosition() - 5000)
                this@NasaAudioMediaController.seekTo(newPos)
                this@NasaAudioMediaController.updateProgress(newPos)
            }
        }
    }
    private val btnStartPause = ImageButton(context).apply{
        this.maxHeight = DEFAULT_HEIGHT
        this.adjustViewBounds = true
        this.scaleType = ImageView.ScaleType.FIT_XY
        this.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        this.isEnabled = false
        this.setOnClickListener {
            if (this@NasaAudioMediaController.isPlaying){
                this@NasaAudioMediaController.pause()
            }
            else{
                this@NasaAudioMediaController.start()
            }
        }
    }
    private val btnRight = ImageButton(context).apply{
        this.maxHeight = DEFAULT_HEIGHT
        this.adjustViewBounds = true
        this.scaleType = ImageView.ScaleType.FIT_XY
        this.setBackgroundResource(R.drawable.ic_fast_forward_black_24dp)
        this.isEnabled = false
        this.setOnClickListener {
            if (this@NasaAudioMediaController.isPlaying){
                val newPos = min(this@NasaAudioMediaController.getDuration(),
                    this@NasaAudioMediaController.getCurrentPosition() + 5000)
                this@NasaAudioMediaController.seekTo(newPos)
                this@NasaAudioMediaController.updateProgress(newPos)
            }
        }
    }
    private val slider = SeekBar(context).apply{
        this.isEnabled = false
        this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!isDragging)
                    return
                mediaPlayer.seekTo(progress)
                //update TextView
                setTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDragging = true
                //removeCallbacks(showProgress)
                //pause
                this@NasaAudioMediaController.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDragging = false
                //update
                //pause
                //play
                this@NasaAudioMediaController.start()
            }

        })
    }
    private val timeShmime = TextView(context).apply{
        this.text = "--.-- : --.--"
        this.setTextColor(Color.parseColor("#000000"))
    }

    private var isDragging = false
    private val handlerProgress = Handler()
    private val showProgress: Runnable = object :Runnable {
        override fun run() {
            updateProgress(mediaPlayer.currentPosition)
            if (!isDragging && mediaPlayer.isPlaying()) {
                handlerProgress.postDelayed(this, (1000).toLong())
            }
        }
    }

    private val partCount = 17
    private var onePart = 0
    private val childExpandCoefficient = arrayOf(2, 2, 2, 8, 3)
    private val DEFAULT_WIDTH = 400
    private val DEFAULT_HEIGHT = 80
    private val MARGIN = 10
    init{
        this.addView(btnLeft)
        this.addView(btnStartPause)
        this.addView(btnRight)
        this.addView(slider)
        this.addView(timeShmime)
    }

    fun updateProgress(v: Int){
        slider.setProgress(v, true)
        setTime(v)
    }

    private val onPreparedListener = object : MediaPlayer.OnPreparedListener{
        override fun onPrepared(mp: MediaPlayer?) {
            Log.d(TAG, "medaPlayerPrepared")
            initializeSlider()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mediaControlWidth = recognizeSize(widthMeasureSpec, DEFAULT_WIDTH)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        onePart = (mediaControlWidth.toFloat() / partCount).toInt()

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }
    private fun recognizeSize(measureSpec: Int, measuredSize: Int): Int {
        val sizeFromMeasureSpec = MeasureSpec.getSize(measureSpec)// / resources.displayMetrics.density).toInt()
        when (MeasureSpec.getMode(measureSpec)){
            MeasureSpec.EXACTLY -> {
                return sizeFromMeasureSpec
            }
            MeasureSpec.AT_MOST ->{
                return sizeFromMeasureSpec
            }
            MeasureSpec.UNSPECIFIED -> {
                return sizeFromMeasureSpec
            }
        }
        return 0
    }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var prevRight = 0
        Log.e(TAG, "l -> $l, t -> $t, r -> $r, b -> $b")
        for (i in 0 until childCount){
            val child = getChildAt(i)
            child.layout(
                prevRight,
                paddingTop,
                prevRight + onePart * childExpandCoefficient[i],
                paddingBottom + child.measuredHeight
            )
            prevRight += onePart * childExpandCoefficient[i] + MARGIN
        }
    }
    private fun changeAllButtonEnabled(){
        btnLeft.isEnabled = !btnLeft.isEnabled
        btnStartPause.isEnabled = !btnStartPause.isEnabled
        btnRight.isEnabled = !btnRight.isEnabled
        slider.isEnabled = !slider.isEnabled
    }
    fun initializeSlider(){
        changeAllButtonEnabled()
        slider.max = this.getDuration()
        slider.setProgress(0, false)
    }

    fun setTime(time: Int){
        val currentMin = time / 1000 / 60
        val currentSec = time / 1000 % 60
        val durationMin = mediaPlayer.duration / 1000 / 60
        val durationSec = mediaPlayer.duration / 1000 % 60
        timeShmime.text = " $currentMin,$currentSec : $durationMin,$durationSec"
    }

    fun setMediaPlayer(newMediaPlayer: MediaPlayer){
        mediaPlayer.release()
        mediaPlayer = newMediaPlayer
        mediaPlayer.setOnPreparedListener(onPreparedListener)
        Log.d(TAG, "SUCCESS ATTACH MEDIA_PLAYER $mediaPlayer")
    }
    fun detachMediaPlayer(){
        Log.d(TAG, "detachMediaPlayer $mediaPlayer")
        handlerProgress.removeCallbacks(showProgress)
        changeAllButtonEnabled()
        mediaPlayer = MediaPlayer()
    }
    // MediaController.MediaPlayerControl
    override fun isPlaying(): Boolean = mediaPlayer.isPlaying
    override fun canSeekForward(): Boolean = true
    override fun getDuration(): Int = mediaPlayer.duration
    override fun pause() {
        Log.d(TAG, "pause()")
        btnStartPause.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        mediaPlayer.pause()
        handlerProgress.removeCallbacks(showProgress)
    }
    override fun getBufferPercentage(): Int = 0
    override fun seekTo(pos: Int) = mediaPlayer.seekTo(pos)
    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition
    override fun canSeekBackward(): Boolean = true
    override fun start() {
        Log.d(TAG, "start()")
        btnStartPause.setBackgroundResource(R.drawable.ic_pause_black_24dp)
        mediaPlayer.start()
        handlerProgress.post(showProgress)
    }
    override fun getAudioSessionId(): Int = mediaPlayer.audioSessionId
    override fun canPause(): Boolean = true
}