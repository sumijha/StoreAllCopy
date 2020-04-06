package com.viceboy.data_repo.converters.group

import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.dataModel.Transactions
import com.viceboy.data_repo.model.uiModel.GroupTransactions
import com.viceboy.data_repo.repository.UserRepository
import com.viceboy.data_repo.util.DataUtils
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import javax.inject.Inject

interface GroupTransactionBeanConverter {
    fun mapTransactionToGroupTransaction(): FlowableTransformer<Transactions, GroupTransactions>
}

class GroupTransactionBeanConverterImpl @Inject constructor() :
    BaseDataConverterImpl<Transactions, GroupTransactions>(), GroupTransactionBeanConverter {

    @Inject
    lateinit var userRepository: UserRepository

    private val emptyString = ""

    override fun processConversionFromInToFlowableOut(inObject: Transactions): Flowable<GroupTransactions> {
        val groupTransactions = processConversionFromInToOut(inObject)
        return userRepository.loadUser(groupTransactions.paidBy)
            .map {
                groupTransactions.paidBy = it.name
                groupTransactions.paidByImage = it.avatar_url ?: emptyString
                groupTransactions
            }.flatMap { groupTrans ->
                userRepository.loadUser(groupTrans.paidTo)
                    .map {
                        groupTrans.paidTo = it.name
                        groupTrans
                    }
            }
    }

    override fun processConversionFromInToOut(inObject: Transactions): GroupTransactions {
        return GroupTransactions(
            inObject.id,
            inObject.groupId,
            DataUtils.getDateFromMilliSec(inObject.paymentDate),
            inObject.currencySymbol,
            inObject.amount,
            inObject.paidBy,
            emptyString,
            inObject.paidTo
        )
    }

    override fun mapTransactionToGroupTransaction() =
        convertInToOutFlowableTransformer()
}