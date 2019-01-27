package ru.terrakok.scaremonger

interface ScaremongerSubscriber {
    fun request(error: Throwable, callback: (retry: Boolean) -> Unit)
}