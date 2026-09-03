package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class GameAudioPlayer {

    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled: Boolean = true

    private val sampleRate = 22050

    private fun playTone(frequencies: List<Float>, durationsMs: List<Int>, gains: List<Float> = listOf(1f)) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                var totalSamples = 0
                for (dur in durationsMs) {
                    totalSamples += (sampleRate * dur) / 1000
                }
                if (totalSamples <= 0) return@launch

                val buffer = ShortArray(totalSamples)
                var currentSample = 0

                for (i in frequencies.indices) {
                    val freq = frequencies[i]
                    val durMs = durationsMs.getOrElse(i) { durationsMs.last() }
                    val gain = gains.getOrElse(i) { 1.0f }
                    val numSamples = (sampleRate * durMs) / 1000

                    for (j in 0 until numSamples) {
                        val t = j.toDouble() / sampleRate
                        val envelope = exp(-3.5 * (j.toDouble() / numSamples)) // smooth decay
                        val sampleVal = sin(2.0 * PI * freq * t) * envelope * gain
                        val clamped = (sampleVal * 24000).toInt().coerceIn(-32767, 32767).toShort()
                        if (currentSample < buffer.size) {
                            buffer[currentSample++] = clamped
                        }
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Release after playback
                scope.launch {
                    val waitTime = (totalSamples * 1000L) / sampleRate + 100
                    kotlinx.coroutines.delay(waitTime)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    fun playTileTap() {
        // Quick high wooden pop
        playTone(listOf(750f, 480f), listOf(25, 35), listOf(0.7f, 0.4f))
    }

    fun playTileSlide() {
        // Soft slide sound
        playTone(listOf(520f, 660f), listOf(30, 40), listOf(0.5f, 0.6f))
    }

    fun playMatch(comboCount: Int = 1) {
        // Sparkly ascending chord, pitch scales with combo
        val baseMultiplier = 1f + (comboCount - 1).coerceAtMost(6) * 0.08f
        val c = 523.25f * baseMultiplier
        val e = 659.25f * baseMultiplier
        val g = 783.99f * baseMultiplier
        val c2 = 1046.50f * baseMultiplier

        playTone(listOf(c, e, g, c2), listOf(60, 60, 70, 110), listOf(0.7f, 0.8f, 0.9f, 1.0f))
    }

    fun playBoosterWhoosh() {
        // Magical shimmer arpeggio
        playTone(
            listOf(600f, 750f, 900f, 1100f, 1350f, 1600f),
            listOf(30, 30, 30, 35, 40, 70),
            listOf(0.6f, 0.7f, 0.8f, 0.85f, 0.9f, 1.0f)
        )
    }

    fun playWinFanfare() {
        // Celebratory triumphant fanfare
        playTone(
            listOf(523f, 659f, 784f, 659f, 784f, 1046f),
            listOf(90, 90, 90, 80, 100, 240),
            listOf(0.8f, 0.85f, 0.9f, 0.8f, 0.95f, 1.0f)
        )
    }

    fun playGameOver() {
        // Descending gentle minor tone
        playTone(
            listOf(587f, 523f, 440f, 349f),
            listOf(90, 90, 100, 180),
            listOf(0.7f, 0.65f, 0.6f, 0.5f)
        )
    }

    fun playCoinEarn() {
        // Crisp dual chime
        playTone(listOf(987.77f, 1318.51f), listOf(50, 90), listOf(0.8f, 0.9f))
    }
}
