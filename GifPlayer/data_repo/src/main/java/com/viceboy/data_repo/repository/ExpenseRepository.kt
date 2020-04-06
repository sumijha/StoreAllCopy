package com.viceboy.data_repo.repository

import com.google.gson.internal.LinkedTreeMap
import com.viceboy.data_repo.model.dataModel.Expense
import io.reactivex.Completable
import io.reactivex.Flowable

interface ExpenseRepository {
    fun saveExpense(expense: Expense) : Completable
    fun remove(expense: Expense) : Completable
    fun loadExpenseParticipants(expenseId:String) : Flowable<HashMap<String,Float>>
    fun loadExpenseByGroupId(groupId : String) : Flowable<List<Expense>>
    fun loadExpenseByYear(userId: String,year:Int) : Flowable<LinkedTreeMap<String, List<Expense>>>
    fun loadExpenseByUserId(userId:String) : Flowable<List<Expense>>
    fun loadLatestExpense(list : Array<Expense>) : Flowable<Expense>
    fun loadExpense(expenseId:String) : Flowable<Expense>
}