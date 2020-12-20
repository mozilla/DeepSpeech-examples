package org.deepspeechdemo

import android.app.Application
import android.content.Context
import java.util.*

class DSApp : Application() {
    private val modelReloadListeners = WeakHashMap<Any, () -> Unit>()

    internal fun addModelReloadListener(context: Context, func: () -> Unit) {
        modelReloadListeners[context] = func
    }

    internal fun reloadModel() {
        modelReloadListeners.run {
            values.forEach {
                it()
            }
            clear()
        }
    }
}
