package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerSubscriber
import java.util.*

class BufferedDispatcher : ScaremongerDispatcher {

    private class Request(val error: Throwable, val callback: (retry: Boolean) -> Unit)

    private val buffer: Queue<Request> = LinkedList()

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
        tryNewRequest()
    }

    override fun unsubscribe() {
        this.subscriber = null
    }

    override fun request(error: Throwable, callback: (retry: Boolean) -> Unit) {
        buffer.add(Request(error, callback))
        if (buffer.size == 1) { //this is first request
            tryNewRequest()
        }
    }

    private fun tryNewRequest() {
        val s = subscriber
        if (s != null && buffer.isNotEmpty()) {
            val r = buffer.last()
            s.request(r.error) { retry -> onResponse(retry) }
        }
    }

    private fun onResponse(retry: Boolean) {
        val r = buffer.remove()
        r.callback(retry)
        tryNewRequest()
    }
}