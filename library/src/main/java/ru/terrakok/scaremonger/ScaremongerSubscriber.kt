package ru.terrakok.scaremonger

interface ScaremongerSubscriber {
    fun onNext(
        error: Throwable,
        callback: (retry: Boolean) -> Unit
    ): ScaremongerDisposable
}