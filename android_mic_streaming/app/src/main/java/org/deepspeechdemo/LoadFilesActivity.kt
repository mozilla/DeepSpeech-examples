package org.deepspeechdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import org.deepspeechdemo.databinding.ActivityLoadFilesBinding
import org.deepspeechdemo.databinding.TwoLineItemBinding

class LoadFilesActivity: AppCompatActivity(R.layout.activity_load_files) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoadFilesBinding>(
            this, R.layout.activity_load_files).apply {
            vm = ViewModelProvider(this@LoadFilesActivity).get<LoadFilesViewModel>().apply {
                fun TwoLineItemBinding.bind(item: LoadFilesViewModelFile) {
                    val launcher = registerForActivityResult(object : OpenDocument() {
                        override fun createIntent(context: Context, input: Array<String>): Intent {
                            return super.createIntent(context, input).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            }
                        }
                    }) {
                        contentResolver.run {
                            item.uri.value = it
                        }
                    }
                    root.setOnClickListener {
                        launcher.launch(arrayOf("*/*"))
                    }
                    item.path.observe(this@LoadFilesActivity) {
                        description = it ?: getString(R.string.file_not_set)
                    }
                }
                modelFileListItem.bind(model)
                scorerFileListItem.bind(scorer)
                nextButton.setOnClickListener {
                    when {
                        nextIsDone.value == true -> {
                            contentResolver.run {
                                val uris = mutableListOf<Uri>()
                                getDefaultSharedPreferences(this@LoadFilesActivity).edit().run {
                                    fun save(key: String, item: LoadFilesViewModelFile) {
                                        item.uri.value!!.let {
                                            uris.add(it)
                                            takePersistableUriPermission(
                                                it,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                            )
                                            putString(key, "$it")
                                        }
                                    }
                                    save(MODEL_URI_PREF_KEY, model)
                                    save(SCORER_URI_PREF_KEY, scorer)
                                    commit()
                                }
                                persistedUriPermissions.forEach {
                                    val uri = it.uri
                                    if (!uris.contains(uri)) {
                                        releasePersistableUriPermission(
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                        )
                                    }
                                }
                                (application as DSApp).reloadModel()
                            }
                            finish()
                        }
                        model.path.value == null -> {
                            modelFileListItem.root.triggerRipple()
                        }
                        scorer.path.value == null -> {
                            scorerFileListItem.root.triggerRipple()
                        }
                        else -> {
                            error("Impossible")
                        }
                    }
                }
                lifecycleOwner = this@LoadFilesActivity
            }
        }
    }
}

internal fun reconfigureFiles(context: Context) = LoadFilesActivity::class.java.start(context)
