package ru.terrakok.scaremonger.android

import android.app.Activity
import android.app.AlertDialog
import ru.terrakok.scaremonger.ScaremongerDisposable
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

    override fun onNext(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        var d: AlertDialog? = null
        val disposable = ScaremongerDisposable {
            dialogs.remove(d)
            d?.dismiss()
        }
        d = AlertDialog.Builder(activity).apply {
            setTitle(titleText)
            setMessage(msgCreator(error))
            setPositiveButton(retryButtonText) { _, _ ->
                disposable.isDisposed = true
                callback(true)
            }
            setNegativeButton(cancelButtonText) { _, _ ->
                disposable.isDisposed = true
                callback(false)
            }
            setCancelable(false)
        }.create()
        dialogs.add(d)
        d.show()

        return disposable
    }

    fun resume(emitter: ScaremongerEmitter) {
        this.emitter = emitter
        emitter.subscribe(this)
    }

    fun pause() {
        emitter?.unsubscribe()
        dialogs.forEach { it.dismiss() }
        dialogs.clear()
    }
}