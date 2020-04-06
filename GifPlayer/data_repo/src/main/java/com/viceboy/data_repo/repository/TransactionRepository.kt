package com.viceboy.data_repo.repository

import com.viceboy.data_repo.model.dataModel.Transactions
import io.reactivex.Completable
import io.reactivex.Flowable

interface TransactionRepository {
    fun loadTransactionByGroupId(groupId : String) : Flowable<List<Transactions>>
    fun loadTransactionByExpenseOwner(userId:String) : Flowable<List<Transactions>>
    fun saveTransaction(): Completable
    fun deleteTransaction(transactionId: String): Completable
}