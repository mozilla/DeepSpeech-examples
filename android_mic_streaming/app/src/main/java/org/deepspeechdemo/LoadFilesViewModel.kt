package org.deepspeechdemo

import android.net.Uri
import androidx.lifecycle.*

internal class LoadFilesViewModelFile {
    val uri = MutableLiveData<Uri>(null)
    val path: LiveData<String> = Transformations.map(uri) {
        it?.toString()
    }
}

internal class LoadFilesViewModel: ViewModel() {
    val model = LoadFilesViewModelFile()
    val scorer = LoadFilesViewModelFile()
    val nextIsDone = MediatorLiveData<Boolean>().apply {
        addSource(model.uri) {
            value = it != null && scorer.uri.value != null
        }
        addSource(scorer.uri) {
            value = it != null && model.uri.value != null
        }
    }
}
