package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.FakeDisposable
import ru.terrakok.scaremonger.ScaremongerDisposable
import ru.terrakok.scaremonger.ScaremongerSubscriber

class ForkDispatcher(
    private val mainRule: (error: Throwable) -> Boolean,
    private val forkedSubscriber: ScaremongerSubscriber? = null
) : ScaremongerDispatcher {

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
    }

    override fun request(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        if (mainRule(error)) {
            subscriber?.let { s ->
                return s.request(error, callback)
            } ?: run {
                callback(false)
                return FakeDisposable
            }
        } else {
            forkedSubscriber?.let { s ->
                return s.request(error, callback)
            } ?: run {
                callback(false)
                return FakeDisposable
            }
        }
    }
}