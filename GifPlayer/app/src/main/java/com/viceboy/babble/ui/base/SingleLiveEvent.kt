package com.viceboy.babble.ui.base

import androidx.lifecycle.Observer

class SingleLiveEvent<T> constructor(private val content: T) {

    private var eventConsumed: Boolean = false

    fun consumeEvent(): T? {
        if (!eventConsumed) {
            eventConsumed = true
            return content
        }
        return null
    }

    fun peekContent() = content
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) :
    Observer<SingleLiveEvent<T>> {
    override fun onChanged(t: SingleLiveEvent<T>?) {
        t?.consumeEvent()?.let {
            onEventUnhandledContent.invoke(it)
        }
    }

}