package com.baize.common_lib.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.ocamara.common_libs.utils.LogUtil
import java.io.IOException


class PlayerUtil(var context: Context) {
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null

    // 播放状态监听器
    private var playbackListener: OnPlaybackListener? = null

    interface OnPlaybackListener {
        fun onPreparing()
        fun onPlaying()
        fun onCompleted()
        fun onError(error: String?)
    }

    fun setListener(playbackListener: OnPlaybackListener) {
        this.playbackListener = playbackListener
    }

    // 播放网络音频
    fun playAudioFromUrl(audioUrl: String?, speaker: Boolean) {
        try {
            stopAudio() // 停止之前的播放

            mediaPlayer = MediaPlayer()
            setupMediaPlayerListeners(speaker)

            if (speaker) {
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            } else {
                // 设置音频流类型为语音通话（听筒）
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)

            }

            // 设置数据源
            mediaPlayer!!.setDataSource(audioUrl)


            // 准备播放（异步）
            mediaPlayer!!.prepareAsync()

            playbackListener?.onPreparing()
        } catch (e: IOException) {
            e.printStackTrace()
            playbackListener?.onError("设置数据源失败: " + e.message)
        }
    }

    // 配置听筒播放
    private fun setupEarpieceAudio() {
        if (audioManager == null) return

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false

        // 禁用蓝牙SCO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.isBluetoothScoOn = false
        }
        LogUtil.d("PlayerUtil", "听筒播放...")
    }

    private fun setupSpeakerAudio() {
        if (audioManager == null) return

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
        LogUtil.d("PlayerUtil", "扬声器播放...")
    }

    // 设置MediaPlayer监听器
    private fun setupMediaPlayerListeners(speaker: Boolean) {
        if (mediaPlayer == null) return

        mediaPlayer!!.setOnPreparedListener { mp: MediaPlayer ->
            if (speaker) {
                setupSpeakerAudio()
            } else {
                setupEarpieceAudio() // 配置听筒
            }
            mp.start()
            playbackListener?.onPlaying()
        }

        mediaPlayer!!.setOnCompletionListener { mp: MediaPlayer? ->
            playbackListener?.onCompleted()
        }

        mediaPlayer!!.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
            var errorMsg = "播放错误: "
            errorMsg += when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> "未知错误"
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "服务终止"
                else -> "错误代码: $what"
            }

            playbackListener?.onError(errorMsg)
            true
        }
    }

    // 停止播放
    fun stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
            }
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    // 恢复音频管理器设置
    private fun resetAudioManager() {
        if (audioManager != null) {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
        }
    }

}