package app.gossamer.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * The faint ting of something sticking to silk: two soft sine partials dying away.
 * High for a catch, lower and longer for a thread pulled tight.
 */
object Chime {
    private const val RATE = 22050

    private fun pcm(high: Boolean): ShortArray {
        val f = if (high) 1568.0 else 987.8
        val len = if (high) .55 else .8
        val n = (RATE * len).toInt()
        return ShortArray(n) { i ->
            val t = i.toDouble() / RATE
            val attack = (t / .004).coerceAtMost(1.0)
            val v = attack * (sin(2 * PI * f * t) * exp(-t * 7) * .55 + sin(2 * PI * f * 2.76 * t) * exp(-t * 16) * .18 + sin(2 * PI * f * .5 * t) * exp(-t * 5) * .12)
            (v * 9000).toInt().toShort()
        }
    }

    private val high by lazy { pcm(true) }
    private val low by lazy { pcm(false) }

    fun play(high: Boolean) {
        runCatching {
            val data = if (high) this.high else low
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(data.size * 2)
                .build()
            track.write(data, 0, data.size)
            track.setNotificationMarkerPosition(data.size - 1)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) = Unit
            })
            track.play()
        }
    }
}
