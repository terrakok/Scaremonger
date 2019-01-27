package ru.terrakok.scaremonger

import java.util.*

object Scaremonger : ScaremongerEmitter {

    private val listeners: MutableMap<String, (retry: Boolean) -> Unit> = mutableMapOf()

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
    }

    fun newRequest(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): String {
        val id = UUID.randomUUID().toString()
        listeners[id] = callback
        dispatchRequest(id, error)
        return id
    }

    fun cancelRequest(id: String) {
        listeners.remove(id)
    }

    private fun dispatchRequest(id: String, error: Throwable) {
        subscriber?.let { s ->
            s.request(error) { sendResponse(id, it) }
        } ?: run {
            sendResponse(id, false)
        }
    }

    private fun sendResponse(id: String, retry: Boolean) {
        listeners[id]?.let { listener ->
            listener(retry)
            listeners.remove(id)
        }
    }
}






