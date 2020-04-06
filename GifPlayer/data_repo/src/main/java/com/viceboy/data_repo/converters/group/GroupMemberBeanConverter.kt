package com.viceboy.data_repo.converters.group

import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.dataModel.User
import com.viceboy.data_repo.model.uiModel.GroupMembers
import com.viceboy.data_repo.repository.UserRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject

interface GroupMemberBeanConverter {
    fun fromUserToGroupMember(
        user: User,
        currency: String? = null,
        symbol: String? = null,
        amount: Float? = 0f,
        adminId: String?
    ): GroupMembers
}

class GroupMemberBeanConverterImpl @Inject constructor() :
    BaseDataConverterImpl<User, GroupMembers>(),
    GroupMemberBeanConverter {

    @Inject
    lateinit var userRepository: UserRepository

    override fun processConversionFromInToFlowableOut(inObject: User): Flowable<GroupMembers> =
        Flowable.create(
            {}, BackpressureStrategy.LATEST
        )

    override fun processConversionFromInToOut(inObject: User): GroupMembers {
        val name = if (inObject.id == userRepository.getCurrentUserId()) "You" else inObject.name
        return GroupMembers(
            inObject.id,
            name,
            null,
            null,
            null,
            inObject.avatar_url,
            0f
        )
    }

    override fun fromUserToGroupMember(
        user: User,
        currency: String?,
        symbol: String?,
        amount: Float?,
        adminId: String?
    ): GroupMembers {
        val member = processConversionFromInToOut(user)
        member.amountPaid = amount ?: 0f
        member.currency = currency
        member.currencySymbol = symbol
        member.isAdmin = adminId == member.id
        return member
    }

}