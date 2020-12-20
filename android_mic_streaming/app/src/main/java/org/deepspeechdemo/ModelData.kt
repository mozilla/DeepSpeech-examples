package org.deepspeechdemo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import java.io.Closeable
import java.io.File

internal const val MODEL_URI_PREF_KEY = "model"
internal const val SCORER_URI_PREF_KEY = "scorer"

internal class ModelData(
    internal val model: File,
    private val modelFd: ParcelFileDescriptor,
    internal val scorer: File,
    private val scorerFd: ParcelFileDescriptor,
): Closeable {
    override fun close() {
        modelFd.close()
        scorerFd.close()
    }
}

internal fun loadModel(context: Context): ModelData? = getDefaultSharedPreferences(context).run {
    fun getFd(key: String, contentResolver: ContentResolver): ParcelFileDescriptor? {
        return Uri.parse(
            getString(key, null) ?: return null,
        ).getFd(contentResolver)
    }
    context.contentResolver.let {
        val model = getFd(MODEL_URI_PREF_KEY, it) ?: return null
        val scorer = getFd(SCORER_URI_PREF_KEY, it) ?: return null
        ModelData(
            model.getFile() ?: return null,
            model,
            scorer.getFile() ?: return null,
            scorer,
        )
    }
}

internal fun loadModelOrPrompt(context: Context) = loadModel(context).also {
    if (it == null) {
        reconfigureFiles(context)
    }
}
