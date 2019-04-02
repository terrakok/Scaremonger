package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerDisposable
import ru.terrakok.scaremonger.ScaremongerSubscriber

class DontRepeatDispatcher : ScaremongerDispatcher {

    private data class Request(
        val callback: (retry: Boolean) -> Unit,
        val disposable: ScaremongerDisposable
    )

    private val callbackMap = mutableMapOf<String, ArrayList<Request>>()
    private val disposableMap = mutableMapOf<String, ScaremongerDisposable>()

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
        callbackMap.forEach { _, list -> list.forEach { it.callback(false) } }
        callbackMap.clear()
        disposableMap.clear()
    }

    override fun request(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        subscriber?.let { s ->
            val type = error.javaClass.simpleName
            val list = callbackMap.getOrPut(type) { ArrayList() }
            val firstRequest = list.isEmpty()

            var r: Request? = null
            val disposable = ScaremongerDisposable {
                callbackMap[type]?.let { l ->
                    l.remove(r)
                    if (l.isEmpty()) {
                        disposableMap[type]?.dispose()
                        disposableMap.remove(type)
                    }
                }
            }
            r = Request(callback, disposable)
            list.add(r)

            if (firstRequest) {
                disposableMap[type] = s.request(error) { retry -> onResponse(type, retry) }
            }
            return disposable
        } ?: run {
            callback(false)
            return ScaremongerDisposable()
        }
    }

    private fun onResponse(type: String, retry: Boolean) {
        callbackMap[type]?.forEach { request ->
            request.disposable.isDisposed = true
            request.callback(retry)
        }
        callbackMap.remove(type)
        disposableMap.remove(type)
    }
}