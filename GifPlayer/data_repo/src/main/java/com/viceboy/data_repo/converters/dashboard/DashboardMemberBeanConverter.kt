package com.viceboy.data_repo.converters.dashboard

import com.viceboy.data_repo.converters.BaseDataConverterImpl
import com.viceboy.data_repo.model.dataModel.User
import com.viceboy.data_repo.model.uiModel.DashboardMembers
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.repository.UserRepository
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import javax.inject.Inject

interface DashboardMemberBeanConverter {
    fun mapUserToDashboardMembers(): FlowableTransformer<User, DashboardMembers>
    fun mapGroupIdToGroupMemberList(): FlowableTransformer<String, List<String>>
    fun mapMemberIdToDashboardMember(): FlowableTransformer<List<String>, DashboardMembers>
}

class DashboardMemberBeanConverterImpl @Inject constructor() :
    BaseDataConverterImpl<User, DashboardMembers>(),
    DashboardMemberBeanConverter {

    @Inject
    lateinit var groupsRepository: GroupsRepository

    @Inject
    lateinit var userRepository: UserRepository

    override fun processConversionFromInToOut(inObject: User): DashboardMembers {
        return DashboardMembers(
            inObject.id,
            inObject.name,
            null,
            null
        )
    }

    override fun mapUserToDashboardMembers(): FlowableTransformer<User, DashboardMembers> =
        convertInToOutFlowableTransformer()

    override fun mapGroupIdToGroupMemberList(): FlowableTransformer<String, List<String>> {
        return FlowableTransformer {
            it.flatMap {
                groupsRepository.loadGroupMembers(it)
            }
        }
    }

    override fun mapMemberIdToDashboardMember(): FlowableTransformer<List<String>, DashboardMembers> {
        return FlowableTransformer {
            it.flatMap { listOfMemberId ->
                val newList = listOfMemberId.toMutableList()
                newList.remove(userRepository.getCurrentUserId())
                userRepository.loadUserList(newList.toTypedArray())
                    .flatMapIterable { it }
                    .compose(mapUserToDashboardMembers())
            }
        }
    }

    override fun processConversionFromInToFlowableOut(inObject: User): Flowable<DashboardMembers> {
        val dashboardMemberModel =
            DashboardMembers(
                inObject.id,
                inObject.name,
                inObject.avatar_url,
                null
            )
        val userGroupsArray = inObject.groups?.toTypedArray()
        return groupsRepository.loadGroupList(userGroupsArray!!)
            .map {
                val listOfGroupNames = mutableListOf<String>()
                it.forEach { listOfGroupNames.add(it.groupName) }
                dashboardMemberModel.inGroups = listOfGroupNames
                dashboardMemberModel
            }
    }
}