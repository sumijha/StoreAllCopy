package com.viceboy.babble.ui.screens.groupDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viceboy.babble.di.MainActivityScope
import com.viceboy.babble.ui.base.BaseViewModel
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnBack
import com.viceboy.babble.ui.util.toFlowable
import com.viceboy.data_repo.converters.group.GroupExpenseBeanConverter
import com.viceboy.data_repo.converters.group.GroupMemberBeanConverter
import com.viceboy.data_repo.converters.group.GroupTransactionBeanConverter
import com.viceboy.data_repo.model.uiModel.*
import com.viceboy.data_repo.repository.ExpenseRepository
import com.viceboy.data_repo.repository.TransactionRepository
import com.viceboy.data_repo.repository.UserRepository
import javax.inject.Inject
import kotlin.math.abs

@MainActivityScope
class GroupDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository
) : BaseViewModel<Boolean>() {

    @Inject
    lateinit var groupExpenseBeanConverter: GroupExpenseBeanConverter

    @Inject
    lateinit var groupMemberBeanConverter: GroupMemberBeanConverter

    @Inject
    lateinit var groupTransactionBeanConverter: GroupTransactionBeanConverter

    private val emptyString = ""

    /**
     * Setting up Mutable LiveData to set up group id
     */
    private val mutableGroupLiveData = MutableLiveData<DashboardGroup>()
    val groupLiveData: LiveData<DashboardGroup>
        get() = mutableGroupLiveData

    /**
     * Setting up Mutable LiveData to get expense data from repository wrapped in ResourceState LiveData
     */
    private val mutableGroupMemberLiveData = MutableLiveData<Resource<List<GroupMembers>>>()
    val groupMembersLiveData: LiveData<Resource<List<GroupMembers>>>
        get() = mutableGroupMemberLiveData

    /**
     * Setting up Mutable LiveData to get expense data from repository wrapped in ResourceState LiveData
     */
    private val mutableGroupExpenseLiveData = MutableLiveData<Resource<List<GroupExpense>>>()
    val groupExpenseLiveData: LiveData<Resource<List<GroupExpense>>>
        get() = mutableGroupExpenseLiveData


    /**
     * Setting up Mutable LiveData to set up tab index of view pager
     */
    private val mutableTabIndexLiveData = MutableLiveData<Int>()
    val tabIndexLiveData: LiveData<Int>
        get() = mutableTabIndexLiveData

    /**
     * Setting up Mutable LiveData to get transaction list for selected group wrapped in ResourceState LiveData
     */
    private val mutableGroupTransactionsLiveData =
        MutableLiveData<Resource<List<GroupTransactions>>>()
    val groupTransactionsLiveData: LiveData<Resource<List<GroupTransactions>>>
        get() = mutableGroupTransactionsLiveData

    /**
     * Setting up mutable LiveData to send list of settlement pair
     */
    private val mutableListOfSettlementPairLiveData = MutableLiveData<Resource<List<SettlementPair>>>()
    val listOfSettlementPairLiveData: LiveData<Resource<List<SettlementPair>>>
        get() = mutableListOfSettlementPairLiveData

    /**
     * Set selected Group model data in view model which trigger dependent data load
     */
    fun setSelectedGroup(groups: DashboardGroup) {
        mutableGroupLiveData.value = groups
    }

    /**
     * Set Selected tab of view pager which triggers update in viewpager fragments
     */
    fun setPageIndex(position: Int) {
        mutableTabIndexLiveData.value = position
    }

    /**
     * Initialize Data load from repository for views
     */
    fun initDataLoad(groups: DashboardGroup) {
        loadGroupMembers(
            groups.mapOfOutstandingAmountByUser ?: mutableMapOf(),
            groups.groupCurrency,
            groups.currencySymbol,
            groups.groupAdmin
        )
        loadGroupExpenses(groups.groupId)
        loadGroupTransactions(groups.groupId)
        loadSettlementPair(groups.mapOfOutstandingAmountByUser ?: mutableMapOf(),groups.currencySymbol)
    }

    /**Load Group expense list by group id */
    private fun loadGroupExpenses(groupId: String) {
        mutableGroupExpenseLiveData.postValue(Resource.Loading())
        val mutableSetOfExpense = mutableSetOf<GroupExpense>()
        expenseRepository.loadExpenseByGroupId(groupId)
            .flatMapIterable { it }
            .compose(groupExpenseBeanConverter.mapExpenseToGroupByExpense())
            .scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, {
                mutableSetOfExpense.add(it)
                mutableGroupExpenseLiveData.postValue(Resource.Success(mutableSetOfExpense.toList()))
            }, {
                mutableGroupExpenseLiveData.postValue(Resource.Failure(it.message))
            }, {
                mutableGroupExpenseLiveData.postValue(Resource.Failure(emptyString))
            })
    }

    /**Load Group Transaction list by group Id wrapped in Resource State liveData*/
    private fun loadGroupTransactions(groupId: String) {
        val mutableSetOfTransactions = mutableSetOf<GroupTransactions>()
        mutableGroupTransactionsLiveData.postValue(Resource.Loading())
        transactionRepository.loadTransactionByGroupId(groupId)
            .flatMapIterable { it }
            .compose(groupTransactionBeanConverter.mapTransactionToGroupTransaction())
            .scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, {
                mutableSetOfTransactions.add(it)
                mutableGroupTransactionsLiveData.postValue(Resource.Success(mutableSetOfTransactions.toList()))
            }, {
                mutableGroupTransactionsLiveData.postValue(Resource.Failure(it.message))
            }, {
                mutableGroupTransactionsLiveData.postValue(Resource.Failure(emptyString))
            })
    }

    /**Load Group Members for selected Group Id wrapped in Resource State LiveData*/
    private fun loadGroupMembers(
        mapOfUserAndAmount: MutableMap<String, Float>,
        currency: String,
        currencySymbol: String,
        groupAdminId : String
    ) {
        val setOfGroupMembers = mutableSetOf<GroupMembers>()
        if (mapOfUserAndAmount.isNotEmpty()) {
            mutableGroupMemberLiveData.postValue(Resource.Loading())
            userRepository.loadUserList(mapOfUserAndAmount.keys.toTypedArray())
                .flatMapIterable { it }
                .map {
                    setOfGroupMembers.add(
                        groupMemberBeanConverter.fromUserToGroupMember(
                            it,
                            currency,
                            currencySymbol,
                            mapOfUserAndAmount[it.id],
                            groupAdminId
                        )
                    )
                    setOfGroupMembers
                }.addToCompositeDisposable(compositeDisposable,
                    {
                        mutableGroupMemberLiveData.postValue(Resource.Success(it.toList()))
                    }, {
                        mutableGroupMemberLiveData.postValue(Resource.Failure(it.message))
                    }, {
                        mutableGroupMemberLiveData.postValue(Resource.Failure(emptyString))
                    })
        }
    }

    /**Initialize process of creating settlement pair*/
    private fun loadSettlementPair(mapOfOutstandingAmountByUser: MutableMap<String, Float>,currency: String) {
        val listOfPair =
            createOutstandingSettlementPair(mapOfOutstandingAmountByUser,currency).toFlowable<List<SettlementPair>>()
        val mutableListOfSettlementPair = mutableSetOf<SettlementPair>()

        mutableListOfSettlementPairLiveData.postValue(Resource.Loading())
        listOfPair.flatMapIterable { it }
            .flatMap { settlementPair ->
                userRepository.loadUser(settlementPair.fromUser)
                    .map { user ->
                        settlementPair.fromUser = if (user.id == userRepository.getCurrentUserId()) "You" else user.name
                        settlementPair
                    }
            }.flatMap { settlementPair ->
                userRepository.loadUser(settlementPair.toUser)
                    .map {user ->
                        settlementPair.toUser = if (user.id == userRepository.getCurrentUserId()) "You" else user.name
                        mutableListOfSettlementPair.add(settlementPair)
                        mutableListOfSettlementPair.sortedBy { it.amount }
                    }
            }.addToCompositeDisposable(compositeDisposable, {
                mutableListOfSettlementPairLiveData.postValue(Resource.Success(it))
            }, {
                mutableListOfSettlementPairLiveData.postValue(Resource.Failure(it.message))
            }, {
                mutableListOfSettlementPairLiveData.postValue(Resource.Failure(emptyString))
            })
    }

    /** Create a list of Settlement pair for selected group */
    private fun createOutstandingSettlementPair(mapOfOutstandingAmountByUser: MutableMap<String, Float>,currency: String): MutableList<SettlementPair> {
        val listOfPair = mutableListOf<SettlementPair>()
        val mapOfBalances = mapOfOutstandingAmountByUser.filter { it.value != 0f }.toMutableMap()

        if (mapOfBalances.isNotEmpty()) {
            while (mapOfBalances.size != 1) {
                val maxValuePair = mapOfBalances.maxBy { it.value }!!
                val minValuePair = mapOfBalances.minBy { it.value }!!
                val diff = maxValuePair.value.minus(minValuePair.value)
                val pair = SettlementPair(
                    System.currentTimeMillis(),
                    minValuePair.key,
                    maxValuePair.key,
                    abs(minValuePair.value),
                    currency
                )
                if (diff == 0f) {
                    mapOfBalances.remove(maxValuePair.key)
                    mapOfBalances.remove(minValuePair.key)
                } else {
                    mapOfBalances.remove(minValuePair.key)
                    mapOfBalances[maxValuePair.key] = diff
                }
                listOfPair.add(pair)
            }
        }
        return listOfPair
    }

    /**
     * Setting up mutable LiveData for Settlement pair observed by GroupSettlementConfirmation bottomsheet
     */
    private val mutableSettlementPairLiveData = MutableLiveData<SettlementPair>()
    val settlementPairLiveData : LiveData<SettlementPair>
    get() = mutableSettlementPairLiveData

    /**
     * Set data for currently selected pair for payment, required for payment confirmation dialog
     */
    fun setSelectedSettlementPair(settlementPair: SettlementPair) {
        mutableSettlementPairLiveData.value = settlementPair
    }
}
