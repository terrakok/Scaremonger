package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerDisposable
import ru.terrakok.scaremonger.ScaremongerSubscriber

class BufferedDispatcher : ScaremongerDispatcher {

    private class Request(val error: Throwable, val callback: (retry: Boolean) -> Unit)

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

    override fun request(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        val r = Request(error, callback)
        buffer.add(r)

        if (buffer.size == 1) { //this is first request
            tryNewRequest()
        }

        return object : ScaremongerDisposable {
            override fun dispose() {
                buffer.remove(r)
                if (currentRequest == r) {
                    currentDisposable?.dispose()

                    currentRequest = null
                    currentDisposable = null
                    tryNewRequest()
                }
            }
        }
    }

    private fun tryNewRequest() {
        val s = subscriber
        if (s != null && buffer.isNotEmpty()) {
            val r = buffer.last()
            currentRequest = r
            currentDisposable = s.request(r.error) { retry -> onResponse(retry) }
        }
    }

    private fun onResponse(retry: Boolean) {
        currentRequest?.let { r ->
            buffer.remove(r)
            r.callback(retry)
        }

        currentRequest = null
        currentDisposable = null
        tryNewRequest()
    }
}