package com.example.nasa_test.RecyclerComponents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nasa_test.DataClasses.NasaNode
import com.example.nasa_test.R
import java.lang.IllegalArgumentException

class NasaAdapter(val items: MutableList<NasaNode>, val action: (NasaNode?) -> Unit): RecyclerView.Adapter<NasaViewHolder>(){
    private val TAG = "TEST_NASA_ADAPTER"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NasaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        when(viewType){
            1 -> {
                view = inflater.inflate(R.layout.image_nasa_view_holder, parent, false)
                return ImageNasaViewHolder(view)
            }
            2 -> {
                view = inflater.inflate(R.layout.video_nasa_view_holder, parent, false)
                return VideoNasaViewHolder(view)
            }
            3 -> {
                view = inflater.inflate(R.layout.audio_nasa_view_holder, parent, false)
                return AudioNasaViewHolder(view)
            }
            else -> {
                return throw IllegalArgumentException("Unknown type")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (items[position].nasaData.mediaType){
            "image" -> return 1
            "video" -> return 2
            "audio" -> return 3
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: NasaViewHolder, position: Int) {
        holder.setup(items[position])
        holder.setAction(action)
    }

}