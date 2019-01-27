package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerSubscriber

class FilterDispatcher(
    private val filter: (error: Throwable) -> Boolean
) : ScaremongerDispatcher {

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
    }

    override fun request(error: Throwable, callback: (retry: Boolean) -> Unit) {
        if (filter(error)) {
            subscriber?.request(error, callback) ?: run {
                callback(false)
            }
        } else {
            callback(false)
        }
    }
}