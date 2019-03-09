package ru.terrakok.scaremonger

object Scaremonger : ScaremongerEmitter, ScaremongerSubscriber {

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
        subscriber?.let { s ->
            return s.request(error, callback)
        } ?: run {
            callback(false)
            return FakeDisposable
        }
    }
}






