package com.viceboy.babble.ui.screens.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.internal.LinkedTreeMap
import com.viceboy.babble.R
import com.viceboy.babble.ui.base.BaseViewModel
import com.viceboy.babble.ui.base.SingleLiveEvent
import com.viceboy.babble.ui.state.ButtonState
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnBack
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnMain
import com.viceboy.data_repo.converters.dashboard.DashboardExpenseBeanConverter
import com.viceboy.data_repo.converters.dashboard.DashboardGroupBeanConverter
import com.viceboy.data_repo.converters.dashboard.DashboardMemberBeanConverter
import com.viceboy.data_repo.model.dataModel.Expense
import com.viceboy.data_repo.model.dataModel.User
import com.viceboy.data_repo.model.uiModel.DashboardExpense
import com.viceboy.data_repo.model.uiModel.DashboardGroup
import com.viceboy.data_repo.model.uiModel.DashboardMembers
import com.viceboy.data_repo.repository.ExpenseRepository
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.repository.UserRepository
import com.viceboy.data_repo.util.DataUtils
import timber.log.Timber
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    context: Context,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val groupsRepository: GroupsRepository,
    private val dashboardGroupBeanConverter: DashboardGroupBeanConverter
) : BaseViewModel<Int>() {

    @Inject
    lateinit var dashboardMemberBeanConverter: DashboardMemberBeanConverter

    @Inject
    lateinit var dashboardExpenseBeanConverter: DashboardExpenseBeanConverter

    private var currentIndexOfSelector = 0
    private var buttonClicked = false
    private var mapOfCurrency = LinkedTreeMap<String, List<Expense>>()

    private val emptyMessage = ""
    private val txtLastExpenseAddedOther =
        context.resources.getString(R.string.last_expense_added_other)
    private val txtLastExpenseAddedSelf =
        context.resources.getString(R.string.last_expense_added_self)


    /**
     * Setting up mutable LiveData to handle latest expense by current user wrapped in LiveData
     */
    private val mutableLatestExpenseLiveData = MutableLiveData<Resource<DashboardExpense>>()
    val latestExpenseLiveData: LiveData<Resource<DashboardExpense>>
        get() = mutableLatestExpenseLiveData

    /**
     * Setting up mutable LiveData to handle group tab data list wrapped in Resource state
     */
    private val mutableGroupTabResourceLiveData =
        MutableLiveData<Resource<MutableSet<DashboardGroup>>>()
    val groupTabResourceLiveData: LiveData<Resource<MutableSet<DashboardGroup>>>
        get() = mutableGroupTabResourceLiveData

    /**
     * Setting up mutable LiveData to handle user details on dashboard
     */
    private val mutableUserDetailsLiveData = MutableLiveData<User>()
    val userDetailsLiveData: LiveData<User>
        get() = mutableUserDetailsLiveData

    /**
     * Setting up mutable LiveData to handle Members tab data list wrapped in Resource state
     */
    private val mutableMembersTabResourceLiveData =
        MutableLiveData<Resource<MutableSet<DashboardMembers>>>()
    val memberTabResourceLiveData: LiveData<Resource<MutableSet<DashboardMembers>>>
        get() = mutableMembersTabResourceLiveData

    /**
     * Setting up mutable LiveData to trigger tab(Groups/Members) click events
     */
    private val mutableOnGroupTabClickedLiveData = MutableLiveData(true)
    val onGroupTabClickedLiveData: LiveData<Boolean>
        get() = mutableOnGroupTabClickedLiveData

    /**
     * Setting up mutable LiveData to manage navigation between fragments
     */
    private val navigateMutableLiveData = MutableLiveData<SingleLiveEvent<Int>>()
    val navigateLiveData: LiveData<SingleLiveEvent<Int>>
        get() = navigateMutableLiveData

    /**
     * Setting up mutable LiveData to manage Button State for fabShowOptions button
     */
    private val mutableMainFabButtonStateLiveData = MutableLiveData<ButtonState>()
    val mainFabButtonStateLiveData: LiveData<ButtonState>
        get() = mutableMainFabButtonStateLiveData


    /**
     * Setting up mutable LiveData to initiate fabShowOptions functionality
     */
    private val mutableAnimFabOptionsLiveData = MutableLiveData<SingleLiveEvent<Boolean>>()
    val animFabLiveData: LiveData<SingleLiveEvent<Boolean>>
        get() = mutableAnimFabOptionsLiveData

    /**
     * Setting up mutable LiveData to handle currency selector which triggers Dashboard BarChart
     */
    private val mutableCurrencySelectorLiveData = MutableLiveData<Resource<String>>()
    val currencySelectorLiveData: LiveData<Resource<String>>
        get() = mutableCurrencySelectorLiveData


    /**
     * Setting up LiveData to retrieve List of Expense for current user and year as per the selected currency using mutableCurrencySelectorLiveData
     */
    val userExpenseListForCurrentYrLiveData: LiveData<Resource<List<Expense>>> =
        Transformations.switchMap(mutableCurrencySelectorLiveData) {
            val mutableLiveData = MutableLiveData<Resource<List<Expense>>>()
            when (it) {
                is Resource.Loading -> mutableLiveData.postValue(Resource.Loading())

                is Resource.Success -> mutableLiveData.postValue(Resource.Success(mapOfCurrency[it.data]))

                is Resource.Failure -> mutableLiveData.postValue(Resource.Failure(it.message))
            }
            return@switchMap mutableLiveData
        }


    /**
     * Called when fabShowOptions button is clicked and update button state
     */
    fun onFabShowOptionsClicked() {
        if (!buttonClicked) {
            mutableMainFabButtonStateLiveData.value = ButtonState.CLICKED
            mutableAnimFabOptionsLiveData.value = SingleLiveEvent(true)
            buttonClicked = true
        } else {
            mutableMainFabButtonStateLiveData.value = ButtonState.ACTIVE
            mutableAnimFabOptionsLiveData.value = SingleLiveEvent(true)
            buttonClicked = false
        }
    }

    /**
     * Reset fabShowOptions button state and reset button state and animation state
     */
    fun resetMainFabStateWithAnimation() {
        buttonClicked = false
        mutableMainFabButtonStateLiveData.value = ButtonState.ACTIVE
        mutableAnimFabOptionsLiveData.value = SingleLiveEvent(true)
    }

    /**
     * Navigate to mentioned destination Id
     */
    fun navigateTo(destinationId: Int) {
        setNavigationFlag(destinationId, navigateMutableLiveData)
    }

    /**
     * Trigger Group Tab selected event
     */
    fun onGroupTabSelected() {
        mutableOnGroupTabClickedLiveData.value = true
    }

    /**
     * Trigger Members Tab selected event
     */
    fun onMemberTabSelected() {
        mutableOnGroupTabClickedLiveData.value = false
    }

    /**
     * Load other required data by dashboard when there are associated groups by current user
     */
    fun onSuccessfulGroupLoad() {
        loadUserProfile()
        loadDashboardMembersTabData()
        loadLatestExpenseForCurrentUser()
        loadExpenseListGroupByCurrency()
    }

    /**
     * Circles the currency map keys in an incremental approach called on next arrow Click
     */
    fun onCurrencySelectorNextClick() {
        Timber.e("Next clicked ")
        if (mapOfCurrency.isNotEmpty() && mapOfCurrency.size > 1) {
            currentIndexOfSelector = if (currentIndexOfSelector == mapOfCurrency.size - 1)
                0
            else
                ++currentIndexOfSelector
            mutableCurrencySelectorLiveData.value =
                Resource.Success(mapOfCurrency.keys.toList()[currentIndexOfSelector])
        }
    }

    /**
     * Circles the currency map keys in an decremental approach called on previous arrow Click
     */
    fun onCurrencySelectorPreviousClick() {
        Timber.e("Previous clicked ")
        if (mapOfCurrency.isNotEmpty() && mapOfCurrency.size > 1) {
            currentIndexOfSelector = if (currentIndexOfSelector == 0)
                mapOfCurrency.size - 1
            else
                --currentIndexOfSelector
            mutableCurrencySelectorLiveData.value =
                Resource.Success(mapOfCurrency.keys.toList()[currentIndexOfSelector])
        }
    }

    /* Load list of Groups from Repository and returns a LiveData wrapped in Resource State */
    private fun loadDashboardGroupTabData() {
        val listOfDashboardGroup = mutableSetOf<DashboardGroup>()
        mutableGroupTabResourceLiveData.postValue(Resource.Loading())
        userRepository.loadGroups(userRepository.getCurrentUserId())
            .flatMap { groupsRepository.loadGroupList(it.toTypedArray()) }
            .flatMapIterable { it }
            .compose(dashboardGroupBeanConverter.mapGroupToDashboardGroup())
            .flatMap {
                val dash = it
                expenseRepository.loadExpenseByGroupId(dash.groupId)
                    .flatMap { listOfExpense ->
                        expenseRepository.loadLatestExpense(listOfExpense.toTypedArray())
                    }.flatMap { expense ->
                        dash.lastTransaction = expense.amountPaid
                        userRepository.loadUser(expense.expenseOwner)
                    }.map { user ->
                        val lastTransactionBy = if (user.id == userRepository.getCurrentUserId())
                            txtLastExpenseAddedSelf
                        else
                            txtLastExpenseAddedOther + " " + user.name.substringBefore(
                                " "
                            )
                        dash.lastTransactionBy = lastTransactionBy
                        listOfDashboardGroup.add(dash)
                        listOfDashboardGroup.sortedBy { it.groupName }.toMutableSet()
                    }
            }
            .scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, {
                mutableGroupTabResourceLiveData.postValue(Resource.Success(it))
            }, {
                mutableGroupTabResourceLiveData.postValue(Resource.Failure(it.message))
            }, {
                mutableGroupTabResourceLiveData.postValue(Resource.Failure(emptyMessage))
            })
    }

    /** Load list of Members for current user active groups from Repository and returns a LiveData wrapped in Resource State */
    private fun loadDashboardMembersTabData() {
        val listOfMembersModel = mutableSetOf<DashboardMembers>()
        mutableMembersTabResourceLiveData.postValue(Resource.Loading())
        userRepository.loadGroups(userRepository.getCurrentUserId())
            .flatMapIterable { it }
            .compose(dashboardMemberBeanConverter.mapGroupIdToGroupMemberList())
            .compose(dashboardMemberBeanConverter.mapMemberIdToDashboardMember())
            .map {
                listOfMembersModel.add(it)
                listOfMembersModel.sortedBy { it.name }.toMutableSet()
            }
            .scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, {
                mutableMembersTabResourceLiveData.postValue(Resource.Success(it))
            }, {
                mutableMembersTabResourceLiveData.postValue(Resource.Failure(it.message))
            }, {
                mutableMembersTabResourceLiveData.postValue(Resource.Failure(emptyMessage))
            })
    }

    /** Load latest expense for current user and returns a LiveData wrapped in Resource State*/
    private fun loadLatestExpenseForCurrentUser() {
        mutableLatestExpenseLiveData.postValue(Resource.Loading())
        expenseRepository.loadExpenseByUserId(userRepository.getCurrentUserId())
            .compose(dashboardExpenseBeanConverter.mapExpenseListToLatestExpense())
            .compose(dashboardExpenseBeanConverter.mapExpenseToDashboardExpense())
            .scheduleOnBackAndOutOnMain()
            .addToCompositeDisposable(compositeDisposable,
                {
                    mutableLatestExpenseLiveData.postValue(Resource.Success(it))
                },
                {
                    mutableLatestExpenseLiveData.postValue(Resource.Failure(it.message))
                }, {
                    mutableLatestExpenseLiveData.postValue(Resource.Failure(emptyMessage))
                })
    }

    /** Load latest expense list for current user and year grouped by currency*/
    private fun loadExpenseListGroupByCurrency() {
        mutableCurrencySelectorLiveData.postValue(Resource.Loading())
        expenseRepository.loadExpenseByYear(
            userRepository.getCurrentUserId(),
            DataUtils.getCurrentYear()
        ).scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, {
                if (it.isNotEmpty()) {
                    mutableCurrencySelectorLiveData.postValue(Resource.Success(it.keys.toList()[currentIndexOfSelector]))
                    mapOfCurrency = it
                } else {
                    mutableCurrencySelectorLiveData.postValue(Resource.Failure(emptyMessage))
                }
            }, {
                mutableCurrencySelectorLiveData.postValue(Resource.Failure(it.message))
            }, {
                mutableCurrencySelectorLiveData.postValue(Resource.Failure(emptyMessage))
            })
    }

    /**Load current user details for Dashboard user profile image and username*/
    private fun loadUserProfile() {
        userRepository.loadUser(userRepository.getCurrentUserId())
            .scheduleOnBackAndOutOnBack()
            .addToCompositeDisposable(compositeDisposable, {
                mutableUserDetailsLiveData.postValue(it)
            }, {})
    }

    /**Save image view height livedata so that it can resume height on retain instance*/
    private val mutableProfileHeightLiveData = MutableLiveData<Int>()
    val profileHeightLiveData: LiveData<Int>
        get() = mutableProfileHeightLiveData

    fun saveImageHeight(profileViewHeight: Int) {
        mutableProfileHeightLiveData.value = profileViewHeight
    }

    init {
        loadDashboardGroupTabData()
    }
}

