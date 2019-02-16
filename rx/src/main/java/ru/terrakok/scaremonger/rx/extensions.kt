package ru.terrakok.scaremonger.rx

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.terrakok.scaremonger.Scaremonger

fun <T> Single<T>.scaremonger(): Single<T> =
    retryWhen { it.handleErrors() }

fun <T> Maybe<T>.scaremonger(): Maybe<T> =
    retryWhen { it.handleErrors() }

fun Completable.scaremonger(): Completable =
    retryWhen { it.handleErrors() }

fun <T> Flowable<T>.scaremonger(): Flowable<T> =
    retryWhen { it.handleErrors() }

fun <T> Observable<T>.scaremonger(): Observable<T> =
    retryWhen { errors ->
        errors
            .toFlowable(BackpressureStrategy.LATEST)
            .handleErrors()
            .toObservable()
    }

private fun Flowable<Throwable>.handleErrors() =
    this
        .observeOn(AndroidSchedulers.mainThread())
        .flatMapSingle<Unit> { err ->
            Single.create<Unit> { s ->
                val id = Scaremonger.newRequest(err) { retry ->
                    if (retry) s.onSuccess(Unit) else s.onError(err)
                }
                s.setDisposable(object : Disposable {
                    private var disposed = false
                    override fun isDisposed() = disposed

                    override fun dispose() {
                        disposed = true
                        Scaremonger.cancelRequest(id)
                    }
                })
            }
        }