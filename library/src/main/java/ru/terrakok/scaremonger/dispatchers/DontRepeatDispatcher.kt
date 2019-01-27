package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerSubscriber

class DontRepeatDispatcher : ScaremongerDispatcher {

    private val map = mutableMapOf<String, MutableList<(retry: Boolean) -> Unit>>()

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
        map.forEach { _, list -> list.forEach { it(false) } }
        map.clear()
    }

    override fun request(error: Throwable, callback: (retry: Boolean) -> Unit) {
        subscriber?.let { s ->
            val type = error.javaClass.simpleName
            val list = map.getOrPut(type) { mutableListOf() }
            if (list.isEmpty()) {
                s.request(error) { retry -> onResponse(type, retry) }
            }
            list.add(callback)

        } ?: run {
            callback(false)
        }
    }

    private fun onResponse(type: String, retry: Boolean) {
        map[type]?.forEach { it(retry) }
        map.remove(type)
    }
}