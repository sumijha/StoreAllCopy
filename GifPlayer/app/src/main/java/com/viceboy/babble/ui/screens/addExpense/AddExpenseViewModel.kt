package com.viceboy.babble.ui.screens.addExpense

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.viceboy.babble.R
import com.viceboy.babble.di.MainActivityScope
import com.viceboy.babble.ui.base.BaseViewModel
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.Constants.KEY_EQUAL
import com.viceboy.babble.ui.util.Constants.KEY_UNEQUAL
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnMain
import com.viceboy.data_repo.model.dataModel.Groups
import com.viceboy.data_repo.model.dataModel.User
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.repository.UserRepository
import javax.inject.Inject

@MainActivityScope
class AddExpenseViewModel @Inject constructor(
    context: Context,
    private val userRepository: UserRepository,
    private val groupsRepository: GroupsRepository
) : BaseViewModel<Boolean>() {

    private val strSelectGroup = context.resources.getString(R.string.select_group)
    private val strSelectExpenseOwner = context.resources.getString(R.string.select_expense_owner)

    //Mediator LiveData to setup check on all required fields filled/selected for Add Expense
    private val mutableListOfBlankFields = mutableSetOf<String>()
    private val mediatorListOfMissingFieldsLiveData = MediatorLiveData<Set<String>>()
    val listOfMissingFieldsLiveData: LiveData<Set<String>>
        get() = mediatorListOfMissingFieldsLiveData

    // Mediator LiveData to observe Split Amount By RadioButtons
    private val mediatorOnSplitAmountBySelectLiveData = MediatorLiveData<String>()
    val splitAmountByLiveData: LiveData<String>
        get() = mediatorOnSplitAmountBySelectLiveData

    //Mutable LiveData to setup list of unchecked fields
    private val mutableListOfUncheckedParticipantsLiveData = MutableLiveData<ArrayList<Int>>()
    val listOfUncheckedParticipantsLiveData: LiveData<ArrayList<Int>>
        get() = mutableListOfUncheckedParticipantsLiveData

    //Mutable LiveData to setup NoOfParticipants
    private val mutableParticipantsCountLiveData = MutableLiveData<Int>()
    val participantsCountLiveData: LiveData<Int>
        get() = mutableParticipantsCountLiveData

    //Mutable LiveData to setup Participants Share
    private val mutableParticipantsShareLiveData = MutableLiveData<ArrayList<Float>>()
    val participantsShareLiveData: LiveData<ArrayList<Float>>
        get() = mutableParticipantsShareLiveData

    //Mutable LiveData to trigger onSaveExpenseLiveData
    private val mutableOnSaveExpenseLiveData = MutableLiveData(false)
    val onSaveExpenseLiveData: LiveData<Boolean>
        get() = mutableOnSaveExpenseLiveData

    // LiveData to setup two way binding in Amount Paid field
    val mutableAmountPaidLiveData = MutableLiveData<String>("")

    // LiveData to setup two way binding in Split Amount Equally Radio
    val mutableSplitAmountEquallyRadLiveData = MutableLiveData(true)

    // LiveData to setup two way binding in Item Description input field
    val mutableItemDescLiveData = MutableLiveData("")

    // LiveData to setup two way binding in Split Amount Unequally Radio
    val mutableSplitAmountUnequallyRadLiveData = MutableLiveData<Boolean>()

    //Mutable LiveData to set Selected Group
    private val mutableSelectedGroupLiveData = MutableLiveData<Groups>(null)
    val selectedGroupLiveData: LiveData<Groups>
        get() = mutableSelectedGroupLiveData
    val selectedGroupNameLiveData: LiveData<String> =
        Transformations.map(mutableSelectedGroupLiveData) {
            if (it != null)
                return@map it.groupName
            return@map strSelectGroup
        }

    //Mutable LiveData to set Selected Expense Owner
    private val mutableSelectedExpenseOwnerLiveData = MutableLiveData(strSelectExpenseOwner)
    val selectedExpenseOwnerLiveData: LiveData<String>
        get() = mutableSelectedExpenseOwnerLiveData

    //Mutable LiveData to setup image Uri received from image picker/Camera
    private val mutableImageUriLiveData = MutableLiveData<Uri>()
    val imageUriLiveData: LiveData<Uri>
        get() = mutableImageUriLiveData

    // Mutable LiveData to setup date received from SELECT DATE Dialog
    private val mutableDatePickerValLiveData = MutableLiveData("")
    val datePickerLiveData: LiveData<String>
        get() = mutableDatePickerValLiveData

    //Mutable LiveData to request focus on Base Amount
    private val mutableRequestFocusLiveData = MutableLiveData<Boolean>()
    val requestFocusLiveData: LiveData<Boolean>
        get() = mutableRequestFocusLiveData

    /* Get List of Users for a particular group from Repository and returns a LiveData wrapped in Resource State */
    private val mutableGroupMembersLiveData = MutableLiveData<Resource<List<User>>>()
    val groupMembersLiveData = Transformations.switchMap(mutableSelectedGroupLiveData) { groupId ->
        groupId?.let { inputGroupId ->
            mutableGroupMembersLiveData.value = Resource.Loading()
            groupsRepository.loadGroupMembers(inputGroupId.id)
                .scheduleOnBackAndOutOnMain()
                .addToCompositeDisposable(compositeDisposable,
                    { listOfUserId ->
                        userRepository.loadUserList(listOfUserId.toTypedArray())
                            .scheduleOnBackAndOutOnMain()
                            .addToCompositeDisposable(compositeDisposable,
                                {
                                    mutableGroupMembersLiveData.value = Resource.Success(it)
                                }, {
                                    mutableGroupMembersLiveData.value = Resource.Failure(it.message)
                                })
                    }, {
                        mutableGroupMembersLiveData.value = Resource.Failure(it.message)
                    }
                )
            return@switchMap mutableGroupMembersLiveData
        }
    }


    fun setMutableListOfUncheckedFields(list: ArrayList<Int>) {
        mutableListOfUncheckedParticipantsLiveData.value = list
    }

    fun setMutableDate(value: String) {
        mutableDatePickerValLiveData.value = value
    }

    fun setMutableRequestFocus(flag: Boolean) {
        mutableRequestFocusLiveData.value = flag
    }

    fun setMutableImageUri(uri: Uri) {
        mutableImageUriLiveData.value = uri
    }

    fun onGroupNameSelected(group: Groups) {
        mutableSelectedGroupLiveData.value = group
    }

    fun onExpenseOwnerSelected(userName: String) {
        mutableSelectedExpenseOwnerLiveData.value = userName
    }

    fun setParticipantsCount(count: Int) {
        mutableParticipantsCountLiveData.value = count
    }

    fun setParticipantsShareList(list: ArrayList<Float>) {
        mutableParticipantsShareLiveData.value = list
    }

    fun onSaveExpenseClick() {
        mutableOnSaveExpenseLiveData.value = true
    }

    fun resetExpenseOwnerLiveData() {
        mutableSelectedExpenseOwnerLiveData.value = strSelectExpenseOwner
    }

    fun resetParticipantsCountLiveData() {
        mutableParticipantsCountLiveData.value = null
    }

    fun resetParticipantShareLiveData() {
        mutableParticipantsShareLiveData.value = ArrayList()
    }

    /* Get List of Groups from Repository and returns a LiveData wrapped in Resource State */
    fun groupsLiveData(): LiveData<Resource<List<Groups>>> {
        val groupLiveData = MutableLiveData<Resource<List<Groups>>>()
        groupLiveData.value = Resource.Loading()
        userRepository.loadGroups(userRepository.getCurrentUserId())
            .scheduleOnBackAndOutOnMain()
            .addToCompositeDisposable(compositeDisposable, { listOfGroupId ->
                groupsRepository.loadGroupList(listOfGroupId.toTypedArray())
                    .scheduleOnBackAndOutOnMain()
                    .addToCompositeDisposable(compositeDisposable,
                        {
                            groupLiveData.value = Resource.Success(it)
                        }, {
                            groupLiveData.value = Resource.Failure(it.message)
                        })
            }, {
                groupLiveData.value = Resource.Failure(it.message)
            })
        return groupLiveData
    }

    private fun resetDatePickerLiveData() {
        mutableDatePickerValLiveData.value = null
    }

    private fun resetSelectedGroupLiveData() {
        mutableSelectedGroupLiveData.value = null
    }

    private fun resetImageUriData() {
        mutableImageUriLiveData.value = null
    }

    private fun resetSaveExpenseEvent() {
        mutableOnSaveExpenseLiveData.value = false
    }

    private fun addSourcesToOnSplitAmountMediatorLiveData() {
        mediatorOnSplitAmountBySelectLiveData.apply {
            addSource(mutableSplitAmountEquallyRadLiveData) {
                if (it) value = KEY_EQUAL
            }
        }

        mediatorOnSplitAmountBySelectLiveData.apply {
            addSource(mutableSplitAmountUnequallyRadLiveData) {
                if (it) value = KEY_UNEQUAL
            }
        }
    }

    private fun addSourcesToMissingFieldsMediatorLiveData() {
        mediatorListOfMissingFieldsLiveData.apply {
            addSource(mutableAmountPaidLiveData) {
                if (it.isNullOrEmpty())
                    mutableListOfBlankFields.add(ERR_ITEM_AMOUNT)
                else
                    mutableListOfBlankFields.remove(ERR_ITEM_AMOUNT)
                value = mutableListOfBlankFields
            }

            addSource(mutableItemDescLiveData) {
                if (it.isNullOrEmpty())
                    mutableListOfBlankFields.add(ERR_ITEM_DESC)
                else
                    mutableListOfBlankFields.remove(ERR_ITEM_DESC)
                value = mutableListOfBlankFields
            }

            addSource(mutableDatePickerValLiveData) {
                if (it.isNullOrEmpty())
                    mutableListOfBlankFields.add(ERR_ITEM_DATE)
                else
                    mutableListOfBlankFields.remove(ERR_ITEM_DATE)
                value = mutableListOfBlankFields
            }

            addSource(selectedGroupNameLiveData) {
                if (it.isNullOrEmpty() || it == strSelectGroup)
                    mutableListOfBlankFields.add(ERR_GROUP_SELECTED)
                else
                    mutableListOfBlankFields.remove(ERR_GROUP_SELECTED)
                value = mutableListOfBlankFields
            }

            addSource(mutableSelectedExpenseOwnerLiveData) {
                if (it.isNullOrEmpty() || it == strSelectExpenseOwner)
                    mutableListOfBlankFields.add(ERR_OWNER_SELECTED)
                else
                    mutableListOfBlankFields.remove(ERR_OWNER_SELECTED)
                value = mutableListOfBlankFields
            }

            addSource(mutableParticipantsCountLiveData) {
                if (it == 0)
                    mutableListOfBlankFields.add(ERR_PARTICIPANT_COUNT)
                else
                    mutableListOfBlankFields.remove(ERR_PARTICIPANT_COUNT)
                value = mutableListOfBlankFields
            }

            addSource(mutableParticipantsShareLiveData) {
                if (it.sum() == 0f)
                    mutableListOfBlankFields.add(ERR_TOTAL_AMOUNT)
                else
                    mutableListOfBlankFields.remove(ERR_TOTAL_AMOUNT)
                value = mutableListOfBlankFields
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dispose()
        resetImageUriData()
        resetExpenseOwnerLiveData()
        resetSelectedGroupLiveData()
        resetSaveExpenseEvent()
        resetDatePickerLiveData()
        resetParticipantsCountLiveData()
        resetParticipantShareLiveData()
    }

    init {
        addSourcesToOnSplitAmountMediatorLiveData()
        addSourcesToMissingFieldsMediatorLiveData()
    }

    companion object {;
        const val ERR_ITEM_DESC = "item_desc_err"
        const val ERR_ITEM_AMOUNT = "item_amount_err"
        const val ERR_ITEM_DATE = "item_date_err"
        const val ERR_GROUP_SELECTED = "item_group_name_err"
        const val ERR_OWNER_SELECTED = "item_owner_name_err"
        const val ERR_TOTAL_AMOUNT = "item_amount_sum_err"
        const val ERR_PARTICIPANT_COUNT = "item_participant_count_err"
    }
}