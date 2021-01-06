package org.deepspeechdemo

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking

private fun Class<out Activity>.doStart(context: Context) {
    context.startActivity(Intent(context, this).apply {
        if (context !is Activity) { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    })
}

internal fun Class<out Activity>.start(context: Context) { if (context is Activity) {
    doStart(context)
} else { Handler(context.mainLooper).postDelayed({ doStart(context) }, 0) } }

internal fun View.triggerRipple() {
    isPressed = true
    isPressed = false
}

internal fun Uri.getFd(contentResolver: ContentResolver) = try {
    contentResolver.openFileDescriptor(this, "r")
} catch (e: SecurityException) {
    Log.e("getFd", "exception", e)
    null
} catch (e: IOException) {
    Log.e("getFd", "exception", e)
    null
}

internal fun ParcelFileDescriptor.getFile() = File("/proc/self/fd/$fd").takeIf { it.exists() }

fun CoroutineScope.cancelAndJoinBlocking() =
    runBlocking { this@cancelAndJoinBlocking.coroutineContext.job.cancelAndJoin() }
