package org.deepspeechdemo

import android.annotation.SuppressLint
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.get
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenResumed
import kotlinx.coroutines.*
import org.deepspeechdemo.databinding.KeyboardBinding

@ObsoleteCoroutinesApi
internal class DeepIMEServiceView(private val service: DeepIMEService):
    ViewModelStoreOwner, LifecycleOwner, LifecycleEventObserver
{
    private val registry = LifecycleRegistry(this).also {
        it.addObserver(this)
    }
    private var core: DSCore? = null
    private lateinit var vm: DSViewModel
    private var backspaceJob: Job? = null
    private lateinit var hideRoot: () -> Unit

    @SuppressLint("ClickableViewAccessibility") // TODO fix
    internal fun create(): View {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        val handler = Handler(service.mainLooper)

        KeyboardBinding.inflate(
            LayoutInflater.from(ContextThemeWrapper(service, R.style.AppTheme)),
        ).run {
            vm = ViewModelProvider(this@DeepIMEServiceView).get<DSViewModel>().apply {
                fun stopAndHandle(block: () -> Unit) {
                    core!!.stopTranscription()
                    handler.postDelayed({
                        block()
                    }, 0)
                }
                val longClickListener = View.OnLongClickListener {
                    stopAndHandle {
                        service.getSystemService<InputMethodManager>()!!.showInputMethodPicker()
                    }
                    true
                }
                settingsButton.setOnClickListener {
                    reconfigureFiles(service)
                }
                recordButton.setOnClickListener {
                    core!!.startStopTranscription()
                }
                backspaceButton.setOnClickListener {
                    stopAndHandle {
                        service.currentInputConnection.deleteSurroundingText(1, 0)
                    }
                }
                backspaceButton.setOnTouchListener { _, event ->
                    when(event.action) {
                        MotionEvent.ACTION_DOWN -> viewModelScope.run {
                            if (backspaceJob == null) {
                                backspaceJob = launch {
                                    delay(300)
                                    while (true) {
                                        stopAndHandle {
                                            service.currentInputConnection
                                                .deleteSurroundingText(1, 0)
                                        }
                                        delay(50)
                                    }
                                }
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            backspaceJob?.run {
                                runBlocking {
                                    cancelAndJoin()
                                }
                                backspaceJob = null
                            }
                        }
                    }
                    false
                }
                switchImeButton.run {
                    setOnClickListener {
                        service.switchToNextInputMethod(false)
                    }
                    setOnLongClickListener(longClickListener)
                }
                spacebarButton.run {
                    setOnClickListener {
                        stopAndHandle {
                            service.currentInputConnection.commitText(" ", 1)
                        }
                    }
                    setOnLongClickListener(longClickListener)
                }
                enterButton.setOnClickListener {
                    stopAndHandle {
                        service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                    }
                }
                transcription.observe(this@DeepIMEServiceView, {
                    // TODO send keystrokes to apps that don't support setComposingText
                    service.currentInputConnection.setComposingText(it, 1)
                })
                running.observe(this@DeepIMEServiceView, {
                    if (!it) {
                        service.currentInputConnection.finishComposingText()
                    }
                })
                hideRoot = {
                    root.visibility = View.GONE
                    service.requestHideSelf(0)
                }
                (service.application as DSApp).addModelReloadListener(service) {
                    viewModelScope.launch {
                        whenResumed {
                            service.restart()
                        }
                    }
                }
                this@DeepIMEServiceView.vm = this
            }
            switchImeButton.isEnabled = service.shouldOfferSwitchingToNextInputMethod()
            lifecycleOwner = this@DeepIMEServiceView
            return root
        }
    }

    internal fun start() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun onStart() {
        if (!checkAudioPermission(service)) {
            return
        }
        if (core == null) {
            core = vm.setupCoreOrPrompt(service, {}) {
                hideRoot()
            }
        }
    }

    internal fun stop() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    internal fun destroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> core?.startStopTranscription()
            Lifecycle.Event.ON_PAUSE -> core?.stopTranscription()
            Lifecycle.Event.ON_DESTROY -> viewModelStore.clear()
            else -> {}
        }
    }

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    private val viewModelStore: ViewModelStore = ViewModelStore()
    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }
}
