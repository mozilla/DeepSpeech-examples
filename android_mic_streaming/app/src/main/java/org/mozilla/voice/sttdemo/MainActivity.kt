package org.mozilla.voice.sttdemo

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
import org.mozilla.voice.stt.MozillaVoiceSttModel.MozillaVoiceSttModel
import org.mozilla.voice.stt.MozillaVoiceSttModel.MozillaVoiceSttStreamingState
import java.io.File


class MainActivity : AppCompatActivity() {
    private var model: MozillaVoiceSttModel? = null
    private var streamContext: MozillaVoiceSttStreamingState? = null

    // Change the following parameters regarding 
    // what works best for your use case or your language.
    private val BEAM_WIDTH = 500
    private val LM_ALPHA = 0.931289039105002f
    private val LM_BETA = 1.1834137581510284f

    private val RECORDER_CHANNELS: Int = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording: Boolean = false

    private val NUM_BUFFER_ELEMENTS = 1024
    private val BYTES_PER_ELEMENT = 2 // 2 bytes (short) because of 16 bit format

    private val TFLITE_MODEL_FILENAME = "deepspeech-0.8.0-models.tflite"
    private val SCORER_FILENAME = "deepspeech-0.8.0-models.scorer"

    private fun checkAudioPermission() {
        // permission is automatically granted on sdk < 23 upon installation
        if (Build.VERSION.SDK_INT >= 23)
        {
            val permission = Manifest.permission.RECORD_AUDIO

            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 3)
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
            runOnUiThread { transcription.text = decoded }
        }
    }

    private fun createModel(): Boolean {
        val modelsPath = getExternalFilesDir(null).toString()
        val tfliteModelPath = "$modelsPath/$TFLITE_MODEL_FILENAME"
        val scorerPath = "$modelsPath/$SCORER_FILENAME"

        for (path in listOf(tfliteModelPath, scorerPath)) {
            if (!(File(path).exists())) {
                status.text = "Model creation failed: $path does not exist."
                return false
            }
        }

        model = mozillaVoiceSttModel(tfliteModelPath)
        model?.setBeamWidth(BEAM_WIDTH)
        model?.enableExternalScorer(scorerPath)
        model?.setScorerAlphaBeta(LM_ALPHA, LM_BETA)
        return true
    }

    private fun startListening() {
        status.text = "Creating model...\n"

        if (model == null) {
            if (!createModel()) {
                return
            }
            status.append("Created model.\n")
        } else {
            status.append("Model already created.\n")
        }

        model?.let { model ->
            btnStartInference.text = "Stop Recording"
            streamContext = model.createStream()

            if (recorder == null) {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    model.sampleRate(),
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    NUM_BUFFER_ELEMENTS * BYTES_PER_ELEMENT)
            }

            recorder?.startRecording()
            isRecording = true

            if (recordingThread == null) {
                recordingThread = Thread(Runnable { transcribe() }, "AudioRecorder Thread")
                recordingThread?.start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getExternalFilesDir(null)
        setContentView(R.layout.activity_main)
        checkAudioPermission()

        // create application data directory on the device
        getExternalFilesDir(null)

        status.text = "Ready, waiting ..."
    }

    private fun stopListening() {
        isRecording = false
        btnStartInference.text = "Start Recording"

        val decoded = model?.finishStream(streamContext)
        transcription.text = decoded

        recorder?.stop()
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
