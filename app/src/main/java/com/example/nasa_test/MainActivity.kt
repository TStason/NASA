package com.example.nasa_test

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nasa_test.DataClasses.NasaNode
import com.example.nasa_test.Presenter.IPresenter
import com.example.nasa_test.Presenter.Presenter
import com.example.nasa_test.RecyclerComponents.NasaAdapter

class MainActivity : AppCompatActivity(), IMainActivity, MediaPlayerForegroundService.ServiceOwner {
    private val TAG = "TEST_NASA_MAIN_ACTIVITY"
    private var presenter: IPresenter? = null
    private val nasaPosts = mutableListOf<NasaNode>()
    private var adapter: NasaAdapter? = null
    private var layoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = Presenter(this)

        val btn = findViewById<ImageButton>(R.id.button)
        val tedit = findViewById<EditText>(R.id.tedit)
        //add presenter method on buttonClick()
        btn.setOnClickListener {
            tedit.clearFocus()
            presenter?.searchButtonPressed(tedit.text.toString())
        }
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        adapter = NasaAdapter(nasaPosts, presenter!!.getOnClickAction())
        layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        recycler.addOnScrollListener(presenter!!.getOnScrollListener())

    }

    override fun setTotalHits(totalHits: String) {
        val totalView = findViewById<TextView>(R.id.total_hits_text)
        totalView.text = totalHits
    }

    override fun updateCurrentCount(added: Int) {
        val currentCountView = findViewById<TextView>(R.id.current_in_recycle_text)
        var newCount = currentCountView.text.toString().toIntOrNull() ?: 0
        newCount += added
        currentCountView.text = newCount.toString()
    }
    override fun updateRecycler(list: List<NasaNode>, needRefresh: Boolean) {
        adapter?.let{
            if (needRefresh){
                it.items.clear()
                findViewById<TextView>(R.id.current_in_recycle_text).text = "0"
            }
            it.items.addAll(list)
            it.notifyDataSetChanged()
        }
    }
    override fun displayItemDetail(node: NasaNode) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("nasa_node", node)
        startActivity(intent)
    }
    override fun updateProgressBar() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (progressBar.visibility == ProgressBar.VISIBLE)
            progressBar.visibility = ProgressBar.GONE
        else
            progressBar.visibility = ProgressBar.VISIBLE
    }
    // lifecycle
    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
    }
    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }
    override fun onResume() {
        Log.d(TAG, "onResume")
        presenter?.onActivityResume()
        super.onResume()
    }
    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
    override fun onStop(){
        Log.d(TAG, "onStop")
        presenter?.onActivityStop()
        super.onStop()
    }
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        presenter?.onActivityDestroy()
        presenter = null
        adapter = null
        layoutManager = null
        super.onDestroy()
    }

    // ServiceOwner
    override fun BindService(serviceConnection: ServiceConnection) {
        val intent = Intent(this, MediaPlayerForegroundService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    override fun UnbindService(serviceConnection: ServiceConnection) {
        unbindService(serviceConnection)
    }

    override fun StartService() {
        val intent = Intent(this, MediaPlayerForegroundService::class.java)
        val started = startService(intent)
        Log.d(TAG, "StartService -> ${started.className}")
    }
    override fun StopService() {
        val intent = Intent(this, MediaPlayerForegroundService::class.java)
        val stopped = stopService(intent)
        Log.d(TAG, "StopService -> $stopped")
    }

}
