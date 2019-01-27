package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerSubscriber

class LoggingDispatcher(
    private val logger: (error: Throwable) -> Unit
) : ScaremongerDispatcher {

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
    }

    override fun request(error: Throwable, callback: (retry: Boolean) -> Unit) {
        logger(error)
        subscriber?.request(error, callback) ?: run {
            callback(false)
        }
    }
}