package org.deepspeechdemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class DSViewModel: ViewModel() {
    val transcriptionMut = MutableLiveData("")
    val transcription: LiveData<String> = transcriptionMut

    val runningMut = MutableLiveData(false)
    val running: LiveData<Boolean> = runningMut
}
