package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class AmbientSoundManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isRainPlaying = MutableStateFlow(false)
    val isRainPlaying: StateFlow<Boolean> = _isRainPlaying.asStateFlow()

    private val _isLofiPlaying = MutableStateFlow(false)
    val isLofiPlaying: StateFlow<Boolean> = _isLofiPlaying.asStateFlow()

    private var rainTrack: AudioTrack? = null
    private var rainJob: Job? = null

    private var lofiTrack: AudioTrack? = null
    private var lofiJob: Job? = null

    fun toggleRain() {
        if (_isRainPlaying.value) {
            stopRain()
        } else {
            stopLofi()
            startRain()
        }
    }

    fun toggleLofi() {
        if (_isLofiPlaying.value) {
            stopLofi()
        } else {
            stopRain()
            startLofi()
        }
    }

    private fun startRain() {
        stopRain()
        _isRainPlaying.value = true
        rainJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            rainTrack = track
            track.play()

            // Filtered pink/brown rain noise buffer
            val buffer = ShortArray(bufferSize)
            var lastOut = 0.0
            while (isActive && _isRainPlaying.value) {
                for (i in buffer.indices) {
                    val white = (Random.nextDouble() * 2.0 - 1.0)
                    lastOut = (lastOut + (0.05 * white)) / 1.05
                    val sample = (lastOut * 4000.0).toInt().coerceIn(-32768, 32767)
                    buffer[i] = sample.toShort()
                }
                track.write(buffer, 0, buffer.size)
            }
        }
    }

    private fun stopRain() {
        _isRainPlaying.value = false
        rainJob?.cancel()
        rainJob = null
        try {
            rainTrack?.stop()
            rainTrack?.release()
        } catch (_: Exception) {}
        rainTrack = null
    }

    private fun startLofi() {
        stopLofi()
        _isLofiPlaying.value = true
        lofiJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            lofiTrack = track
            track.play()

            // Calming Lo-Fi study chords (A minor, F major, C major, G major)
            val chordFreqs = listOf(
                listOf(220.0, 261.63, 329.63), // Am
                listOf(174.61, 220.0, 261.63), // F
                listOf(261.63, 329.63, 392.0), // C
                listOf(196.0, 246.94, 293.66)  // G
            )
            val buffer = ShortArray(bufferSize)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var chordIndex = 0
            var sampleCounter = 0
            val samplesPerChord = sampleRate * 3 // 3 seconds per chord

            while (isActive && _isLofiPlaying.value) {
                val freqs = chordFreqs[chordIndex]
                for (i in buffer.indices) {
                    val s1 = sin(phase1)
                    val s2 = sin(phase2)
                    val s3 = sin(phase3)
                    val envelope = 0.8
                    val combined = ((s1 + s2 + s3) / 3.0 * envelope * 5000.0).toInt().coerceIn(-32768, 32767)
                    buffer[i] = combined.toShort()

                    phase1 += 2.0 * PI * freqs[0] / sampleRate
                    phase2 += 2.0 * PI * freqs[1] / sampleRate
                    phase3 += 2.0 * PI * freqs[2] / sampleRate

                    sampleCounter++
                    if (sampleCounter >= samplesPerChord) {
                        sampleCounter = 0
                        chordIndex = (chordIndex + 1) % chordFreqs.size
                    }
                }
                track.write(buffer, 0, buffer.size)
            }
        }
    }

    private fun stopLofi() {
        _isLofiPlaying.value = false
        lofiJob?.cancel()
        lofiJob = null
        try {
            lofiTrack?.stop()
            lofiTrack?.release()
        } catch (_: Exception) {}
        lofiTrack = null
    }

    fun playBeep() {
        scope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            } catch (_: Exception) {}
        }
    }

    fun releaseAll() {
        stopRain()
        stopLofi()
    }
}
