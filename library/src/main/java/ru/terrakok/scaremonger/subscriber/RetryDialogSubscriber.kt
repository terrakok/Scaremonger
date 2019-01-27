package ru.terrakok.scaremonger.subscriber

import android.app.Activity
import android.app.AlertDialog
import ru.terrakok.scaremonger.ScaremongerEmitter
import ru.terrakok.scaremonger.ScaremongerSubscriber

class RetryDialogSubscriber(
    private val activity: Activity,
    private val titleText: String,
    private val retryButtonText: String,
    private val cancelButtonText: String,
    private val msgCreator: (error: Throwable) -> String = { e -> e.toString() }
) : ScaremongerSubscriber {

    private var emitter: ScaremongerEmitter? = null
    private val dialogs = mutableListOf<AlertDialog>()

    override fun request(error: Throwable, callback: (retry: Boolean) -> Unit) {
        val d = AlertDialog.Builder(activity).apply {
            setTitle(titleText)
            setMessage(msgCreator(error))
            setPositiveButton(retryButtonText) { _, _ -> callback(true) }
            setNegativeButton(cancelButtonText) { _, _ -> callback(false) }
            setCancelable(false)
        }.create()
        dialogs.add(d)
        d.show()
    }

    fun resume(emitter: ScaremongerEmitter) {
        this.emitter = emitter
        emitter.subscriber = this
    }

    fun pause() {
        emitter?.subscriber = null
        dialogs.forEach { it.dismiss() }
        dialogs.clear()
    }
}