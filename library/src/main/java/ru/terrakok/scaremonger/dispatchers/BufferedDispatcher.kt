package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerDispatcher
import ru.terrakok.scaremonger.ScaremongerDisposable
import ru.terrakok.scaremonger.ScaremongerSubscriber

class BufferedDispatcher : ScaremongerDispatcher {

    private class Request(
        val error: Throwable,
        val callback: (retry: Boolean) -> Unit,
        val disposable: ScaremongerDisposable
    )

    private val buffer = ArrayList<Request>()

    private var subscriber: ScaremongerSubscriber? = null
    private var currentRequest: Request? = null
    private var currentDisposable: ScaremongerDisposable? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
        tryNewRequest()
    }

    override fun unsubscribe() {
        this.subscriber = null
        this.currentRequest = null
        this.currentDisposable = null
    }

    override fun onNext(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        var r: Request? = null
        val disposable = ScaremongerDisposable { disposeRequest(r!!) }
        r = Request(error, callback, disposable)

        buffer.add(r)
        if (buffer.size == 1) { //this is first onNext
            tryNewRequest()
        }

        return disposable
    }

    private fun disposeRequest(r: Request) {
        buffer.remove(r)
        if (currentRequest == r) {
            currentDisposable?.dispose()

            currentRequest = null
            currentDisposable = null
            tryNewRequest()
        }
    }

    private fun tryNewRequest() {
        val s = subscriber
        if (s != null && buffer.isNotEmpty()) {
            val r = buffer.last()
            currentRequest = r
            currentDisposable = s.onNext(r.error) { retry -> onResponse(retry) }
        }
    }

    private fun onResponse(retry: Boolean) {
        currentRequest?.let { r ->
            buffer.remove(r)
            r.disposable.isDisposed = true
            r.callback(retry)
        }

        currentRequest = null
        currentDisposable = null
        tryNewRequest()
    }
}