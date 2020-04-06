package com.viceboy.data_repo.converters.dashboard

import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.dataModel.Expense
import com.viceboy.data_repo.model.uiModel.DashboardExpense
import com.viceboy.data_repo.repository.ExpenseRepository
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.util.DataUtils
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import javax.inject.Inject

interface DashboardExpenseBeanConverter {
    fun mapExpenseToDashboardExpense(): FlowableTransformer<Expense, DashboardExpense>
    fun mapExpenseListToLatestExpense(): FlowableTransformer<List<Expense>, Expense>
}

class DashboardExpenseBeanConverterImpl @Inject constructor() :
    BaseDataConverterImpl<Expense, DashboardExpense>(),
    DashboardExpenseBeanConverter {

    @Inject
    lateinit var groupsRepository: GroupsRepository

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    override fun processConversionFromInToOut(inObject: Expense): DashboardExpense {
        return DashboardExpense(
            inObject.id,
            inObject.itemName,
            inObject.expenseDate.toString(),
            inObject.expenseOwner,
            inObject.currency,
            null,
            inObject.amountPaid
        )
    }

    override fun mapExpenseToDashboardExpense() = convertInToOutFlowableTransformer()

    override fun mapExpenseListToLatestExpense(): FlowableTransformer<List<Expense>, Expense> =
        FlowableTransformer {
            it.flatMap { listOfExpense ->
                expenseRepository.loadLatestExpense(listOfExpense.toTypedArray())
            }
        }

    override fun processConversionFromInToFlowableOut(inObject: Expense): Flowable<DashboardExpense> {
        val dateInString = DataUtils.getDateFromMilliSec(inObject.expenseDate)
        return groupsRepository.loadGroupList(arrayOf(inObject.groupId))
            .map { listOfGroups ->
                DashboardExpense(
                    inObject.id,
                    inObject.itemName,
                    dateInString,
                    inObject.expenseOwner,
                    inObject.currency,
                    listOfGroups.first().groupName,
                    inObject.amountPaid
                )
            }
    }

}