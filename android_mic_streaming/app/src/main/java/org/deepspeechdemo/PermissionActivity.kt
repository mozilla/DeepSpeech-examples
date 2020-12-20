package org.deepspeechdemo

import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Handler
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionActivity : AppCompatActivity(R.layout.activity_permission) {
    private val requestLauncher = registerForActivityResult(RequestPermission()) {
        if (it) {
            finish()
        } else {
            request()
        }
    }

    private fun request() {
        Handler(mainLooper).postDelayed({
            requestLauncher.launch(RECORD_AUDIO)
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        request()
    }
}

internal fun checkAudioPermission(context: Context): Boolean {
    return (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) == PERMISSION_GRANTED).also {
        if (!it) {
            PermissionActivity::class.java.start(context)
        }
    }
}
