package ru.terrakok.scaremonger

object Scaremonger : ScaremongerDispatcher {

    private var subscriber: ScaremongerSubscriber? = null

    override fun subscribe(subscriber: ScaremongerSubscriber) {
        this.subscriber = subscriber
    }

    override fun unsubscribe() {
        this.subscriber = null
    }

    override fun onNext(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable {
        subscriber?.let { s ->
            return s.onNext(error, callback)
        } ?: run {
            callback(false)
            return ScaremongerDisposable()
        }
    }
}






