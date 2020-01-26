package org.mozilla.deepspeechdemo

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechStreamingState


class MainActivity : AppCompatActivity() {
    private var model: DeepSpeechModel? = null
    private var streamContext: DeepSpeechStreamingState? = null

    // Change the following parameters regarding 
    // what works best for your use case or your language.
    private val BEAM_WIDTH = 500
    private val LM_ALPHA = 0.75f
    private val LM_BETA = 1.85f

    private val RECORDER_SAMPLERATE = 16000
    private val RECORDER_CHANNELS: Int = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording: Boolean = false

    private var NUM_BUFFER_ELEMENTS = 1024
    private var BYTES_PER_ELEMENT = 2 // 2 bytes (short) because of 16 bit format

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        // permission is automatically granted on sdk < 23 upon installation
        if (Build.VERSION.SDK_INT >= 23)
        {
            for (permission in permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(permission), 3)
                }
            }
        }
    }

    private fun transcribe() {
        val audioData = ShortArray(NUM_BUFFER_ELEMENTS)

        while (isRecording) {
            recorder?.read(
                audioData,
                0,
                NUM_BUFFER_ELEMENTS
            )
            model?.feedAudioContent(streamContext, audioData, audioData.size)
            val decoded = model?.intermediateDecode(streamContext)
            transcription.text = decoded
        }
    }

    private fun createModel() {
        val modelsPath = modelsPathInput.text.toString()
        model = DeepSpeechModel(modelsPath + "/output_graph.tflite", BEAM_WIDTH)
        model?.enableDecoderWihLM(modelsPath + "/lm.binary", modelsPath + "/trie", LM_ALPHA, LM_BETA)
    }

    private fun startListening() {
        btnStartInference.text = "Stop Recording"

        status.text = "Creating model...\n"

        if (model == null) {
            createModel()
            status.append("Created model.\n")
        } else {
            status.append("Model already created.\n")
        }

        streamContext = model?.createStream()

        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            RECORDER_SAMPLERATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING,
            NUM_BUFFER_ELEMENTS * BYTES_PER_ELEMENT)

        recorder?.startRecording()
        isRecording = true
        recordingThread = Thread(Runnable { transcribe() }, "AudioRecorder Thread")
        recordingThread?.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        modelsPathInput.setText("/sdcard/deepspeech")
        status.text = "Ready, waiting ..."
    }

    private fun stopListening() {
        isRecording = false
        recordingThread = null
        btnStartInference.text = "Start Recording"

        val decoded = model?.finishStream(streamContext)
        transcription.text = decoded

        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    fun onRecordClick(v: View?) {
        if (isRecording) {
            stopListening()
        } else {
            startListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (model != null) {
            model?.freeModel()
        }
    }
}
