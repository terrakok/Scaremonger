package ru.terrakok.scaremonger

interface ScaremongerEmitter {
    fun subscribe(subscriber: ScaremongerSubscriber)
    fun unsubscribe()
}