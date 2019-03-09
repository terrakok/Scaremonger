package ru.terrakok.scaremonger

interface ScaremongerDisposable {
    fun dispose()
}

object FakeDisposable : ScaremongerDisposable {
    override fun dispose() {}
}