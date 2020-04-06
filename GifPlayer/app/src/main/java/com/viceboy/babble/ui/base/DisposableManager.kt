package com.viceboy.babble.ui.base

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class DisposableManager @Inject constructor() {

    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(vararg disposable: Disposable) {
        compositeDisposable.addAll(*disposable)
        compositeDisposable.size()
    }

    fun dispose() = compositeDisposable.clear()
}