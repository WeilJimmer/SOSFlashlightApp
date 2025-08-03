package org.wbftw.weil.sos_flashlight.objs

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.wbftw.weil.sos_flashlight.BuildConfig
import org.wbftw.weil.sos_flashlight.R

class TonePlayer(private val context: Context, private var soundType: Int = DEFAULT_SOUND_TYPE_750) {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var streamId: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val TAG = "TonePlayer"
        const val DEFAULT_SOUND_TYPE_750 = 750
        const val DEFAULT_SOUND_TYPE_1000 = 1000
    }

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()

        // 載入音效
        soundId = if (soundType == DEFAULT_SOUND_TYPE_1000) {
            soundPool?.load(context, R.raw.tone1000, 1) ?: 0
        } else {
            // 默認使用750Hz音調
            soundPool?.load(context, R.raw.tone750, 1) ?: 0
        }
    }

    fun setSoundType(type: Int) {
        if (type != soundType) {
            soundType = type
            release()
            initSoundPool()
        }
    }

    fun playTone(durationMs: Long = 250) {
        Log.d(TAG, "playTone: soundType=$soundType, durationMs=$durationMs")
        soundPool?.let { pool ->
            val defaultVolume = if (BuildConfig.DEBUG){
                0.1f
            }else{
                1.0f
            }
            streamId = pool.play(soundId, defaultVolume, defaultVolume, 1, 1, 1.0f)

            // 設定計時器在指定時間後停止播放
            handler.postDelayed({
                pool.stop(streamId)
            }, durationMs)
        }
    }

    fun stopTone() {
        soundPool?.stop(streamId)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}