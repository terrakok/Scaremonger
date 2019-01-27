package ru.terrakok.scaremonger.dispatchers

import ru.terrakok.scaremonger.ScaremongerEmitter
import ru.terrakok.scaremonger.ScaremongerSubscriber

interface ScaremongerDispatcher : ScaremongerEmitter, ScaremongerSubscriber {

    fun connect(other: ScaremongerDispatcher): ScaremongerDispatcher {
        subscribe(other)
        return object : ScaremongerDispatcher,
            ScaremongerEmitter by other,
            ScaremongerSubscriber by this {}
    }

}