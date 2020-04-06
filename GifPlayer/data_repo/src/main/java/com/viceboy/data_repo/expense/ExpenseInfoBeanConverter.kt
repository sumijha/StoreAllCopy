package com.viceboy.data_repo.expense

import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.uiModel.ExpenseInfo
import com.viceboy.data_repo.model.uiModel.GroupExpense
import com.viceboy.data_repo.repository.GroupsRepository
import io.reactivex.Flowable
import javax.inject.Inject

interface ExpenseInfoBeanConverter {
    fun mapGroupExpenseToExpenseInfo(groupExpense: GroupExpense): Flowable<ExpenseInfo>
}

class ExpenseInfoBeanConverterImpl @Inject constructor() :
    BaseDataConverterImpl<GroupExpense, ExpenseInfo>(), ExpenseInfoBeanConverter {

    @Inject
    lateinit var groupsRepository: GroupsRepository

    override fun processConversionFromInToFlowableOut(inObject: GroupExpense): Flowable<ExpenseInfo> {
        val expenseInfo = processConversionFromInToOut(inObject)
        return groupsRepository.loadGroup(inObject.group)
            .map {
                expenseInfo.groupName = it.groupName
                expenseInfo
            }
    }

    override fun processConversionFromInToOut(inObject: GroupExpense): ExpenseInfo {
        return ExpenseInfo(
            inObject.id,
            inObject.itemName,
            inObject.itemImage,
            inObject.itemAmount,
            inObject.currencySymbol,
            inObject.group,
            inObject.expenseDate,
            inObject.expenseOwner,
            toContributorsName(inObject.expensePaidTo.keys.toList()),
            toContributorsAmount(inObject.expensePaidTo.values.toList())
        )
    }

    override fun mapGroupExpenseToExpenseInfo(groupExpense: GroupExpense): Flowable<ExpenseInfo> =
        processConversionFromInToFlowableOut(groupExpense)

    private fun toContributorsAmount(toList: List<Float>): String {
        var contributorAmountList = ""
        toList.forEach { fl ->
            contributorAmountList += "$fl \n"
        }
        return contributorAmountList
    }

    private fun toContributorsName(toList: List<String>): String {
        var contributorNameList = ""
        toList.forEach { fl ->
            contributorNameList += "$fl :\n"
        }
        return contributorNameList
    }

}