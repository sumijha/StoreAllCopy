package com.viceboy.babble.ui.screens.expenseDetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.viceboy.babble.ui.base.BaseViewModel
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnBack
import com.viceboy.data_repo.expense.ExpenseInfoBeanConverter
import com.viceboy.data_repo.model.uiModel.ExpenseInfo
import com.viceboy.data_repo.model.uiModel.GroupExpense
import javax.inject.Inject

class ExpenseDetailsViewModel @Inject constructor() : BaseViewModel<Boolean>() {

    @Inject
    lateinit var expenseInfoBeanConverter: ExpenseInfoBeanConverter

    private val emptyString = ""

    /**
     * Setting up Mutable LiveData to set GroupExpense Data and convert to ExpenseInfo
     */
    private val mutableGroupExpenseLiveData = MutableLiveData<GroupExpense>()
    val expenseInfoLiveData = Transformations.switchMap(mutableGroupExpenseLiveData) {
        val mutableExpenseInfoLiveData = MutableLiveData<Resource<ExpenseInfo>>()
        mutableExpenseInfoLiveData.postValue(Resource.Loading())
        expenseInfoBeanConverter.mapGroupExpenseToExpenseInfo(it)
            .scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, { expense ->
                mutableExpenseInfoLiveData.postValue(Resource.Success(expense))
            }, { throwable ->
                mutableExpenseInfoLiveData.postValue(Resource.Failure(throwable.message))
            }, {
                mutableExpenseInfoLiveData.postValue(Resource.Failure(emptyString))
            })
        return@switchMap mutableExpenseInfoLiveData
    }

    fun setGroupExpense(groupExpense: GroupExpense) {
        mutableGroupExpenseLiveData.value = groupExpense
    }

}