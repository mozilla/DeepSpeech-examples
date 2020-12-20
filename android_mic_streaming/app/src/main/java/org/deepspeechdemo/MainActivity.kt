package org.deepspeechdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenResumed
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.deepspeechdemo.databinding.ActivityMainBinding

@ObsoleteCoroutinesApi
class MainActivity : AppCompatActivity() {
    private var core: DSCore? = null
    private lateinit var vm: DSViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).run {
            vm = ViewModelProvider(this@MainActivity).get<DSViewModel>().apply {
                recordButton.setOnClickListener {
                    core!!.startStopTranscription()
                }
                transcriptionMut.value = getString(R.string.spoken_text_will_appear_here)
                (application as DSApp).addModelReloadListener(this@MainActivity) {
                    core?.run {
                        viewModelScope.launch {
                            whenResumed {
                                stopTranscription()
                                close()
                                core = null
                                loadModel(this@MainActivity)?.use {
                                    core = DSCore(this@apply, it)
                                }
                            }
                        }
                    }
                }
                this@MainActivity.vm = this
            }
            lifecycleOwner = this@MainActivity
        }
    }

    override fun onResume() {
        super.onResume()
        if (!checkAudioPermission(this)) {
            return
        }
        if (core == null) {
            core = vm.setupCoreOrPrompt(this, {
                startStopTranscription()
            })
        }
    }

    override fun onPause() {
        super.onPause()
        core?.stopTranscription()
    }

    override fun onDestroy() {
        super.onDestroy()
        core?.close()
        core = null
    }
}
