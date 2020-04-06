package com.viceboy.data_repo.converters.group

import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.dataModel.Expense
import com.viceboy.data_repo.model.uiModel.GroupExpense
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.repository.UserRepository
import com.viceboy.data_repo.util.DataUtils
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import javax.inject.Inject

interface GroupExpenseBeanConverter {
    fun mapExpenseToGroupByExpense(): FlowableTransformer<Expense, GroupExpense>
}

class GroupExpenseBeanConverterImpl @Inject constructor() :
    BaseDataConverterImpl<Expense, GroupExpense>(), GroupExpenseBeanConverter {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var groupsRepository: GroupsRepository

    private val emptyString = ""

    override fun processConversionFromInToFlowableOut(inObject: Expense): Flowable<GroupExpense> {
        val groupExpense = processConversionFromInToOut(inObject)
        return userRepository.loadUser(groupExpense.expenseOwner)
            .map {
                groupExpense.expenseOwner = it.name
                groupExpense
            }.flatMap { groupExpenseModel ->
                val mapOfContributors = mutableMapOf<String, Float>()
                userRepository.loadUserList(
                    groupExpenseModel.expensePaidTo.keys.toList().toTypedArray()
                )
                    .flatMapIterable { it }
                    .map { user ->
                        mapOfContributors[user.name] =
                            groupExpenseModel.expensePaidTo[user.id] ?: 0f
                        groupExpense.expensePaidTo = mapOfContributors
                        groupExpense
                    }.flatMap {
                        groupsRepository.loadGroupList(arrayOf(it.group))
                            .map {
                                groupExpense.currencySymbol = it.first().currencySymbol
                                groupExpense
                            }
                    }
            }
    }

    override fun processConversionFromInToOut(inObject: Expense): GroupExpense {
        return GroupExpense(
            inObject.id,
            inObject.itemName,
            inObject.expenseImageUrl ?: emptyString,
            inObject.amountPaid,
            inObject.groupId,
            inObject.currency,
            emptyString,
            DataUtils.getDateFromMilliSec(inObject.expenseDate),
            inObject.expenseOwner,
            inObject.expenseSharedBy
        )
    }

    override fun mapExpenseToGroupByExpense() = convertInToOutFlowableTransformer()

}