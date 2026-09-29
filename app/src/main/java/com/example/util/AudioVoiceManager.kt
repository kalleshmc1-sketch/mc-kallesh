package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

class AudioVoiceManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var androidTts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        androidTts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                androidTts?.language = Locale.US
                androidTts?.setPitch(0.95f)
                androidTts?.setSpeechRate(0.98f)
                isTtsReady = true
            }
        }
    }

    fun playWavFile(
        filePath: String,
        onCompletion: () -> Unit = {}
    ) {
        stopPlayback()
        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                val vol = if (_isMuted.value) 0f else 1f
                setVolume(vol, vol)
                setOnCompletionListener {
                    _isPlaying.value = false
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    onCompletion()
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = player
            _isPlaying.value = true
        } catch (e: Exception) {
            _isPlaying.value = false
            onCompletion()
        }
    }

    fun speakWithFallbackTts(text: String) {
        stopPlayback()
        if (_isMuted.value) return
        if (isTtsReady) {
            _isPlaying.value = true
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kallesh_tts_utt")
        }
    }

    fun togglePauseResume() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.start()
            _isPlaying.value = true
        }
    }

    fun toggleMute(): Boolean {
        val nextMute = !_isMuted.value
        _isMuted.value = nextMute
        val vol = if (nextMute) 0f else 1f
        try {
            mediaPlayer?.setVolume(vol, vol)
            if (nextMute) {
                androidTts?.stop()
            }
        } catch (_: Exception) {
        }
        return nextMute
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        try {
            androidTts?.stop()
        } catch (_: Exception) {
        }
        _isPlaying.value = false
    }

    fun startAudioRecording(): Result<File> = runCatching {
        stopAudioRecording()
        val outDir = File(context.cacheDir, "voice_recordings").apply { mkdirs() }
        val file = File(outDir, "voice_${System.currentTimeMillis()}.m4a")
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(64000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        mediaRecorder = recorder
        currentRecordingFile = file
        _isRecording.value = true
        file
    }

    fun stopAudioRecording(): File? {
        val recordedFile = currentRecordingFile
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null
        _isRecording.value = false
        return recordedFile
    }

    fun release() {
        stopPlayback()
        stopAudioRecording()
        try {
            androidTts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
