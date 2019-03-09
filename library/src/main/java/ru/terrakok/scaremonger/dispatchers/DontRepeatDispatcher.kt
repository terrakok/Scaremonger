package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.FakeDisposable
import ru.terrakok.scaremonger.ScaremongerDisposable
import ru.terrakok.scaremonger.ScaremongerSubscriber

class DontRepeatDispatcher : ScaremongerDispatcher {

    private val callbackMap = mutableMapOf<String, ArrayList<(retry: Boolean) -> Unit>>()
    private val disposableMap = mutableMapOf<String, ScaremongerDisposable>()

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
        callbackMap.forEach { _, list -> list.forEach { it(false) } }
        callbackMap.clear()
    }

    override fun request(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        subscriber?.let { s ->
            val type = error.javaClass.simpleName
            val list = callbackMap.getOrPut(type) { ArrayList() }
            if (list.isEmpty()) {
                val d = s.request(error) { retry -> onResponse(type, retry) }
                disposableMap[type] = d
            }
            list.add(callback)
            return object : ScaremongerDisposable {
                override fun dispose() {
                    callbackMap[type]?.let { l ->
                        l.remove(callback)
                        if (l.isEmpty()) {
                            disposableMap[type]?.dispose()
                            disposableMap.remove(type)
                        }
                    }
                }
            }
        } ?: run {
            callback(false)
            return FakeDisposable
        }
    }

    private fun onResponse(type: String, retry: Boolean) {
        callbackMap[type]?.forEach { it(retry) }
        callbackMap.remove(type)
        disposableMap.remove(type)
    }
}