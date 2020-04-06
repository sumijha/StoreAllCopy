package com.viceboy.babble.data_provider.mockRepository

import android.content.Context
import com.google.gson.internal.LinkedTreeMap
import com.viceboy.babble.data_provider.mockDataLoader.MockResourceLoader
import com.viceboy.data_repo.model.dataModel.Transactions
import com.viceboy.data_repo.repository.TransactionRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class MockTransactionRepository @Inject constructor(val context: Context) :
    TransactionRepository {

    private val mutableCachedMapOfGroups = HashMap<String, Transactions>()
    private var mapOfTransactionId: LinkedTreeMap<String, Any>? = null

    override fun saveTransaction(): Completable = Completable.create {}

    override fun deleteTransaction(transactionId: String): Completable = Completable.create {}

    override fun loadTransactionByGroupId(groupId: String): Flowable<List<Transactions>> {
        return Flowable.create({ emitter ->
            mapOfTransactionId?.let { map ->
                val modelData = MockResourceLoader.queryDataListWithParams<Transactions>(
                    map,
                    "groupId",
                    groupId
                )

                if (modelData.isNotEmpty())
                    emitter.onNext(modelData)
                else
                    emitter.onComplete()
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadTransactionByExpenseOwner(userId: String): Flowable<List<Transactions>> {
        return Flowable.create({ emitter ->
            mapOfTransactionId?.let { map ->
                val modelData = MockResourceLoader.queryDataListWithParams<Transactions>(
                    map,
                    "paidBy",
                    userId
                )

                if (modelData.isNotEmpty())
                    emitter.onNext(modelData)
                else
                    emitter.onComplete()
            }
        }, BackpressureStrategy.LATEST)
    }

    private fun mapOfTransactions(): LinkedTreeMap<String, Any> {
        val mapOfGroups = MockResourceLoader.getDummyResponseFrom(
            context,
            method,
            endPoint.split("/").toTypedArray()
        )
        return mapOfGroups?.get(NODE_GROUPS) as LinkedTreeMap<String, Any>
    }

    init {
        mapOfTransactionId = mapOfTransactions()
    }

    companion object {
        private const val NODE_GROUPS = "transactions"

        private const val method = "users_transactions"
        private const val endPoint = "data/users_transactions"
    }

}