package org.deepspeechdemo

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.coroutines.ObsoleteCoroutinesApi

class DeepIMEService: InputMethodService() {
    private var view = DeepIMEServiceView(this)

    override fun onCreateInputView(): View = view.create()

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        view.start()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        view.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        view.destroy()
    }

    internal fun restart() {
        view.destroy()
        view = DeepIMEServiceView(this).apply {
            setInputView(create())
            start()
        }
    }
}
