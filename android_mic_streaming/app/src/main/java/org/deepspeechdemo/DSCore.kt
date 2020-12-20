package org.deepspeechdemo

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel
import java.io.Closeable

// We read from the recorder in chunks of 2048 shorts. With a model that expects its input
// at 16000Hz, this corresponds to 2048/16000 = 0.128s or 128ms.
private const val AUDIO_BUFFER_SIZE: Int = 2048

@ObsoleteCoroutinesApi
internal class DSCore(
    private val vm: DSViewModel,
    private val data: ModelData,
): Closeable {
    private var closed = false
    private val threadContext = newSingleThreadContext("Transcription Thread")
    private var job: Job? = null
    private val model =
        DeepSpeechModel(data.model.absolutePath).apply {
            enableExternalScorer(data.scorer.absolutePath)
        }

    @WorkerThread
    private suspend fun transcribe() {
        vm.runningMut.postValue(true)
        val audioData = ShortArray(AUDIO_BUFFER_SIZE)
        model.run {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate(),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AUDIO_BUFFER_SIZE
            )
            recorder.startRecording()

            val streamContext = createStream()
            try {
                while (true) {
                    recorder.read(audioData, 0, AUDIO_BUFFER_SIZE)
                    feedAudioContent(streamContext, audioData, audioData.size)
                    vm.transcriptionMut.postValue(intermediateDecode(streamContext))
                    yield()
                }
            } finally {
                vm.transcriptionMut.postValue(finishStream(streamContext))
                recorder.stop()
                recorder.release()
                vm.runningMut.postValue(false)
            }
        }
    }

    @MainThread
    internal fun startStopTranscription() {
        if (closed) {
            return
        }
        vm.viewModelScope.launch(threadContext) {
            job?.run {
                cancel()
                return@launch
            }
            launch {
                transcribe()
            }.run {
                this@DSCore.job = this
                join()
            }
            job = null
        }
    }

    @MainThread
    internal fun stopTranscription() {
        if (closed) {
            return
        }
        job?.let {
            runBlocking {
                it.cancelAndJoin()
            }
        }
    }

    @MainThread
    override fun close() {
        if (closed) {
            return
        }
        stopTranscription()
        threadContext.close()
        model.freeModel()
        closed = true
    }
}

@ObsoleteCoroutinesApi
internal fun DSViewModel.setupCoreOrPrompt(
    context: Context,
    onInit: DSCore.() -> Unit,
    onFail: () -> Unit = {},
): DSCore? =
    loadModelOrPrompt(context)?.use {
        try {
            DSCore(this, it).also { core ->
                onInit(core)
            }
        } catch (e: RuntimeException) {
            Log.e("setupCoreOrPrompt", "exception", e)
            reconfigureFiles(context)
            null
        }
    }.also {
        if (it == null) {
            onFail()
        }
    }
