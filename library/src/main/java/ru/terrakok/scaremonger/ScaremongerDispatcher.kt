package ru.terrakok.scaremonger

interface ScaremongerDispatcher : ScaremongerEmitter, ScaremongerSubscriber {

    fun connect(other: ScaremongerDispatcher): ScaremongerDispatcher {
        subscribe(other)
        return object : ScaremongerDispatcher,
            ScaremongerEmitter by other,
            ScaremongerSubscriber by this {}
    }

}