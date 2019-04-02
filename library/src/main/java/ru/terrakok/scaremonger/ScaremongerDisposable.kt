package ru.terrakok.scaremonger

class ScaremongerDisposable(private val block: (() -> Unit)? = null) {
    var isDisposed: Boolean = false

    fun dispose() {
        if (!isDisposed) block?.invoke()
    }
}