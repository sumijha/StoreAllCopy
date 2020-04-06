package com.viceboy.babble.ui.base

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viceboy.babble.ui.state.ButtonState
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel<ND> : ViewModel() {

    private var animatorList: ArrayList<Animator>? = null

    /**
     * Method to set Navigation flag
     */
    protected fun setNavigationFlag(destination: ND, mutableLiveData: MutableLiveData<SingleLiveEvent<ND>>) {
        mutableLiveData.value = SingleLiveEvent(destination)
    }

    protected fun onButtonClick(mediatorOnClickEventLiveData: MediatorLiveData<ButtonState>) {
        mediatorOnClickEventLiveData.value = ButtonState.CLICKED
    }

    protected fun resetButtonClickEvent(mutableOnClickEventLiveData: MutableLiveData<Boolean>) {
        mutableOnClickEventLiveData.value = false
    }

    /**
     * Adding sources to Mediator LiveData
     */
    protected fun addSourcesToMediatorLiveData(
        mutableLiveData: MutableLiveData<String>,
        liveData: MediatorLiveData<Set<String>>,
        mutableList: MutableSet<String>,
        keyVal: String,
        boolExpr: (String) -> Boolean
    ) {
        liveData.apply {
            addSource(mutableLiveData) {
                if (boolExpr(it))
                    mutableList.add(keyVal)
                else
                    mutableList.remove(keyVal)
                value = mutableList
            }
        }
    }

    /**
     * Setting up Animation Listener for animation end
     */
    protected fun getAnimatorListenerObject(
        onAnimationEnd: (animation:Animator?) -> Unit,
        onAnimationStart: () -> Unit
    ): Animator.AnimatorListener {
        return object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                onAnimationEnd(animation)
                animatorList = (animation as AnimatorSet).childAnimations
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                onAnimationStart.invoke()
            }
        }
    }

    /**
     * Reset Animation State
     */
    fun resetAnimationState(animatorList:ArrayList<Animator>?) {
        animatorList?.let {
            for (va in it) {
                (va as ValueAnimator).reverse()
                va.end()
            }
        }
    }


    /**
     * Setting up composite Disposable to add Subscriptions and dispose them on Cleared
     */
    protected val compositeDisposable = CompositeDisposable()

    protected fun dispose() {
        if (!compositeDisposable.isDisposed)
            compositeDisposable.clear()
    }
}