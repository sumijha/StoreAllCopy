package com.viceboy.babble.ui.util

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.schedulers.Schedulers

fun Completable.scheduleOnBackAndOutOnMain(): Completable {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Observable<T>.observeOnMainThread(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Observable<T>.scheduleOnBackAndOutOnMain(): Observable<T> {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Flowable<T>.scheduleOnBackAndOutOnMain(): Flowable<T> {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Flowable<T>.scheduleOnBackAndOutOnBack(): Flowable<T> {
    return this.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
}

@Suppress("UNCHECKED_CAST")
fun <T> Any.toFlowable(): Flowable<T> {
    return Flowable.create({ emitter ->
        if (this is List<*> && this.isEmpty()) emitter.onComplete()
        else emitter.onNext(this as T)
    }, BackpressureStrategy.LATEST)
}

fun Completable.addToCompositeDisposable(
    compositeDisposable: CompositeDisposable,
    onComplete: () -> Unit,
    onError: (Throwable) -> Unit
) {
    compositeDisposable.add(this.subscribeWith(object : DisposableCompletableObserver() {
        override fun onComplete() {
            onComplete.invoke()
        }

        override fun onError(e: Throwable) {
            onError.invoke(e)
        }

    }))
}

fun <T> Observable<T>.addToCompositeDisposable(
    compositeDisposable: CompositeDisposable,
    onNext: (T) -> Unit,
    onError: (Throwable) -> Unit
) {
    compositeDisposable.add(this.subscribe({
        onNext.invoke(it)
    }, {
        onError.invoke(it)
    }))
}

fun <T> Flowable<T>.addToCompositeDisposable(
    compositeDisposable: CompositeDisposable,
    onNext: (T) -> Unit,
    onError: (Throwable) -> Unit,
    onComplete: () -> Unit = {}
) {
    compositeDisposable.add(this.subscribe({
        onNext.invoke(it)
    }, {
        onError.invoke(it)
    }, {
        onComplete.invoke()
    }))
}

