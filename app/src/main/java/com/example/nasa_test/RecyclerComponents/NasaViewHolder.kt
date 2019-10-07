package com.example.nasa_test.RecyclerComponents

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nasa_test.DataClasses.NasaNode
import com.example.nasa_test.R
import kotlin.math.min

abstract class NasaViewHolder(view: View): RecyclerView.ViewHolder(view), Interaction{
    val TAG = "NASA_NasaViewHolder"
    val title = view.findViewById<TextView>(R.id.title)
    val description = view.findViewById<TextView>(R.id.description)
    val keywords = view.findViewById<TextView>(R.id.keywords)
    var mediaUrl: String? = null

    var nasaNode: NasaNode? = null

    abstract fun setup(item: NasaNode)

    abstract fun setMedia(links: List<String>)

    override fun setAction(action: ((NasaNode?) -> Unit)?){
        action?.let{
            itemView.setOnClickListener{ it(nasaNode) }
        } ?: itemView.setOnClickListener(null)
    }
}
interface Interaction{
    fun setAction(action: ((NasaNode?) -> Unit)?)
}

class ImageNasaViewHolder(view: View): NasaViewHolder(view){

    override fun setup(item: NasaNode) {
        nasaNode = item
        val nasaData = item.nasaData
        title.text = "${nasaData.title}"
        nasaData.description?.let{
            description.text = "${it.subSequence(0 until min(it.length, 100))}..."
        }
        keywords.text = "${nasaData.keywords}"
        Log.d(TAG, "MediaType ${nasaData.mediaType}")
        setMedia(item.mediaLinks)
    }

    override fun setMedia(links: List<String>) {
        val img = itemView.findViewById<ImageView>(R.id.imageView)
        mediaUrl = links.find { it.contains("thumb", true) }
        Log.d(TAG, "imgSrc -> $mediaUrl, $links")
        Glide.with(itemView)
            .load(mediaUrl)
            .placeholder(R.drawable.img_default)
            .centerCrop()
            .into(img)
    }

}

class AudioNasaViewHolder(view: View): NasaViewHolder(view){

    override fun setup(item: NasaNode) {
        nasaNode = item
        val nasaData = item.nasaData
        title.text = "${nasaData.title}"
        nasaData.description?.let{
            description.text = "${it.subSequence(0 until min(it.length, 100))}..."
        }
        keywords.text = "${nasaData.keywords}"
        Log.d(TAG, "MediaType ${nasaData.mediaType}")
    }

    override fun setMedia(links: List<String>) {
        mediaUrl = links.find { it.contains(".mp3") }
        if (mediaUrl == null)
            mediaUrl = links.find { it.contains(".m4a") }
        if (mediaUrl == null)
            mediaUrl = links.find { it.contains(".wav") }    }
}

class VideoNasaViewHolder(view: View): NasaViewHolder(view){

    override fun setup(item: NasaNode) {
        nasaNode = item
        val nasaData = item.nasaData
        title.text = "${nasaData.title}"
        nasaData.description?.let{
            description.text = "${it.subSequence(0 until min(it.length, 100))}..."
        }
        keywords.text = "${nasaData.keywords}"
        Log.d(TAG, "MediaType ${nasaData.mediaType}")
        setMedia(item.mediaLinks)
    }

    override fun setMedia(links: List<String>) {
//        mediaUrl = links.find { it.endsWith("mobile.mp4", true) }
        val img = itemView.findViewById<ImageView>(R.id.imageView)
        mediaUrl = links.find { it.contains("thumb", true) }
        Log.d(TAG, "imgSrc -> $mediaUrl, $links")
        Glide.with(itemView)
            .load(mediaUrl)
            .placeholder(R.drawable.img_default)
            .centerCrop()
            .into(img)
    }
}