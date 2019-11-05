package com.wesync.metronome

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import com.wesync.R
import com.wesync.util.MetronomeCodes
import com.wesync.util.Tempo
import java.lang.IllegalStateException

class TickHandlerThread( context:Context ): HandlerThread("TickHandlerThread",
    Process.THREAD_PRIORITY_DEFAULT) {

    /*
           TODO: try to reduce audio latency by using Oboe's AudioStream instead of MediaPlayer
           TODO: DO IT IN EXPERIMENTAL BRANCH.
     */

    private lateinit var handler: Handler
    private var mp: MediaPlayer = MediaPlayer.create(context, R.raw.tick)
    private var bpm:Long = 120
    private var _isPlaying:Boolean? = false

    override fun run() {
        if (Looper.myLooper() == null) {
            super.run()
        }
        else
            Looper.loop()

        try {
            mp.prepareAsync()

        } catch(e: IllegalStateException) {
           // Log.d("prepareAsync","eh ketangkep")
        }
        //Log.d("ThreadStart","Thread has been started!")
    }

    override fun onLooperPrepared() {
        handler = Handler {
            when (it.what) {
            MetronomeCodes.START_METRONOME -> {
                _isPlaying = true
                mp.start()
                SystemClock.sleep((60000 / this.bpm) - Tempo.OFFSET_IN_MILLIS)
                //Log.d("tick","tick")
                if (_isPlaying == true) {
                    handler.sendEmptyMessage(MetronomeCodes.START_METRONOME)
                }
            }
            MetronomeCodes.STOP_METRONOME -> {
                _isPlaying = false
                handler.removeMessages(MetronomeCodes.START_METRONOME)
                handler.removeMessages(MetronomeCodes.ON_BPM_CHANGED)
                handler.removeMessages(MetronomeCodes.STOP_METRONOME)
            }
            MetronomeCodes.ON_BPM_CHANGED -> {
                this.bpm = it.obj as Long
            }
        }
            return@Handler true
        }
    }

    override fun quitSafely(): Boolean {
        mp.release()
        return super.quitSafely()
    }

    fun getHandler(): Handler {
        return handler
    }

    fun isPlaying() = _isPlaying
}