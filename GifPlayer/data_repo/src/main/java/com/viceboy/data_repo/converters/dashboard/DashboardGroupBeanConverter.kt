package com.viceboy.data_repo.converters.dashboard

import android.content.Context
import com.google.gson.internal.LinkedTreeMap
import com.viceboy.data_repo.R
import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.dataModel.Groups
import com.viceboy.data_repo.model.uiModel.DashboardGroup
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.repository.UserRepository
import com.viceboy.data_repo.util.DataConstants
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import javax.inject.Inject

interface DashboardGroupBeanConverter {
    fun fromGroupToDashboardGroup(groups: Groups): DashboardGroup
    fun mapGroupToDashboardGroup(): FlowableTransformer<Groups, DashboardGroup>
}

class DashboardGroupBeanConverterImpl @Inject constructor(private val context: Context) :
    BaseDataConverterImpl<Groups, DashboardGroup>(),
    DashboardGroupBeanConverter {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var groupRepository: GroupsRepository

    override fun processConversionFromInToOut(inObject: Groups): DashboardGroup {
        val textYouPay = context.resources.getString(R.string.text_you_pay)
        val textYouGet = context.resources.getString(R.string.text_you_get)
        val mapOfExpense = (inObject.groupAdmin as LinkedTreeMap<*, *>).entries.firstOrNull {
            it.key == userRepository.getCurrentUserId()
        }
            ?: (inObject.groupMembers as LinkedTreeMap<*, *>).entries.firstOrNull { it.key == userRepository.getCurrentUserId() }
        val amount =
            (mapOfExpense?.value as LinkedTreeMap<*, *>)[DataConstants.KEY_AMOUNT]?.toString()
                ?.toFloat() ?: 0f
        val owedText =
            if (amount < 0) textYouPay else textYouGet
        val admin = inObject.groupAdmin.keys.first().trim()

        return DashboardGroup(
            inObject.id,
            inObject.groupName,
            admin,
            inObject.groupDescription,
            inObject.currency,
            inObject.currencySymbol,
            owedText,
            null,
            amount,
            null,
            null
        )
    }

    override fun fromGroupToDashboardGroup(groups: Groups) = processConversionFromInToOut(groups)

    override fun processConversionFromInToFlowableOut(inObject: Groups): Flowable<DashboardGroup> {
        val dashGroup = processConversionFromInToOut(inObject)
        return groupRepository.loadUserExpenseMap(inObject.id)
            .map {
                dashGroup.mapOfOutstandingAmountByUser = it
                dashGroup
            }
    }

    override fun mapGroupToDashboardGroup(): FlowableTransformer<Groups, DashboardGroup> =
        convertInToOutFlowableTransformer()

}
