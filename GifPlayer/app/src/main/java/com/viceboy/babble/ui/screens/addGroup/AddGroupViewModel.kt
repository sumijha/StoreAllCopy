package com.viceboy.babble.ui.screens.addGroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.viceboy.babble.ui.base.BaseViewModel
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.state.SearchState
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.isEmailPattern
import com.viceboy.data_repo.model.dataModel.User
import com.viceboy.data_repo.repository.UserRepository
import java.util.*
import javax.inject.Inject

class AddGroupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel<Boolean>() {

    private val emptyText = ""
    private var emailText: String = ""

    // Setting up two way binding for GroupName text field
    val mutableGroupNameLiveData = MutableLiveData<String>(emptyText)

    // Setting up two way binding for Group Description text field
    val mutableGroupDescLiveData = MutableLiveData<String>(emptyText)

    // Setting up two way binding for Add Participants text field
    val mutableSearchParticipantsTextLiveData = MutableLiveData<String>()

    // Setting up mutableLiveData to trigger search event
    private val mutableSearchClickEventLiveData = MutableLiveData<Boolean>()

    // Setting up mutable LiveData to trigger onCreateGroup click
    private val mutableOnCreateGroupClickLiveData = MutableLiveData<Boolean>()
    val onCreateGroupClickLiveData: LiveData<Boolean>
        get() = mutableOnCreateGroupClickLiveData

    // Setting up MutableLiveData for Currency Symbol
    private val mutableCurrencySymbolLiveData = MutableLiveData<String>(emptyText)
    val currencySymbolLiveData: LiveData<String>
        get() = mutableCurrencySymbolLiveData

    // Setting up MutableLiveData for currency code
    private val mutableCurrencyCodeLiveData = MutableLiveData<String>()
    val currencyCodeLiveData: LiveData<String>
        get() = mutableCurrencyCodeLiveData

    // Setting up MutableLiveData for currency name
    private val mutableCurrencyNameLiveData = MutableLiveData<String>()
    val currencyNameLiveData: LiveData<String>
        get() = mutableCurrencyNameLiveData

    // Setting up mutable liveData to set added participants list
    private val mutableListOfAddedUserLiveData = MutableLiveData<MutableList<User>>(mutableListOf())
    val listOfAddedUserLiveData: LiveData<MutableList<User>>
        get() = mutableListOfAddedUserLiveData

    // Setting up mutable LiveData to observe Search State of participants Email
    private val mutableSearchStateLiveData = MutableLiveData(SearchState.IDLE)
    val searchStateLiveData: LiveData<SearchState>
        get() = mutableSearchStateLiveData

    //Mediator LiveData to setup check on all required fields filled/selected for Add Expense
    private val mutableListOfBlankFields = mutableSetOf<String>()
    private val mediatorListOfMissingFieldsLiveData = MediatorLiveData<Set<String>>()
    val listOfMissingFieldsLiveData: LiveData<Set<String>>
        get() = mediatorListOfMissingFieldsLiveData

    // Setting up mutable LiveData to setup error state of entered search text
    private val mutableChecklistOfTextErrorEvent = mutableSetOf<String>()
    private val checklistOfTextErrorEvent = listOf(KEY_CLICK_EVENT, KEY_TEXT_EVENT)
    private val mediatorTextErrorStateLiveData = MediatorLiveData<Set<String>>()
    val textErrorStateLiveData: LiveData<Boolean> =
        Transformations.map(mediatorTextErrorStateLiveData) {
            return@map it.containsAll(checklistOfTextErrorEvent)
        }


    private val mutableUserLiveData = MutableLiveData<Resource<User>>()
    private val mutableListOfCheckedEvents = mutableSetOf<String>()
    private val listOfCheckedSearchEvent = listOf(KEY_CLICK_EVENT, KEY_TEXT_EVENT)
    private val mediatorListOfCheckedEventLiveData = MediatorLiveData<Set<String>>()
    val listOfCheckedEventLiveData = Transformations.switchMap(mediatorListOfCheckedEventLiveData) {
        if (it.containsAll(listOfCheckedSearchEvent)) {
            mutableUserLiveData.value = Resource.Loading()
            userRepository.loadUserByEmail(emailText.toLowerCase(Locale.ENGLISH))
                .addToCompositeDisposable(compositeDisposable,
                    { user ->
                        mutableUserLiveData.value = Resource.Success(user)
                    }, { throwable ->
                        mutableUserLiveData.value = Resource.Failure(throwable.message)
                    }, {
                        mutableUserLiveData.value =
                            Resource.Failure("Unable to find user with email : $emailText")
                    })
        }
        return@switchMap mutableUserLiveData
    }

    fun onSearchClickEvent() {
        mutableSearchClickEventLiveData.value = true
    }

    fun onCreateGroupClick() {
        mutableOnCreateGroupClickLiveData.value = true
    }

    fun setCurrencySymbol(text: String) {
        mutableCurrencySymbolLiveData.value = text
    }

    fun setCurrencyCode(text: String) {
        mutableCurrencyCodeLiveData.value = text
    }

    fun setCurrencyName(text: String) {
        mutableCurrencyNameLiveData.value = text
    }

    fun setSearchState(searchState: SearchState) {
        mutableSearchStateLiveData.value = searchState
    }

    fun setAddedUserListLiveData(list: MutableList<User>) {
        mutableListOfAddedUserLiveData.value = list
    }

    fun resetSearchClickEvent() {
        mutableSearchClickEventLiveData.value = false
    }

    fun resetSearchStateEventLiveData() {
        mutableSearchStateLiveData.value = SearchState.IDLE
    }

    /** Add sources mediator List of search events LiveData */
    private fun addSourcesToMediatorListOfSearchEventLiveData() {
        mediatorListOfCheckedEventLiveData.apply {
            addSource(mutableSearchParticipantsTextLiveData) {
                emailText = it
                if (it.isNotEmpty() && it.isEmailPattern())
                    mutableListOfCheckedEvents.add(KEY_TEXT_EVENT)
                else
                    mutableListOfCheckedEvents.remove(KEY_TEXT_EVENT)
                value = mutableListOfCheckedEvents
            }

            addSource(mutableSearchClickEventLiveData) {
                if (it && emailText.isNotEmpty() && emailText.isEmailPattern())
                    mutableListOfCheckedEvents.add(KEY_CLICK_EVENT)
                else
                    mutableListOfCheckedEvents.remove(KEY_CLICK_EVENT)
                value = mutableListOfCheckedEvents
            }
        }
    }

    /** Add sources mediator List of Text Error events LiveData */
    private fun addSourcesToMediatorErrorStateLiveData() {
        mediatorTextErrorStateLiveData.apply {
            addSource(mutableSearchParticipantsTextLiveData) {
                if (it.isNotEmpty() && !it.isEmailPattern())
                    mutableChecklistOfTextErrorEvent.add(KEY_TEXT_EVENT)
                else
                    mutableChecklistOfTextErrorEvent.remove(KEY_TEXT_EVENT)
                value = mutableChecklistOfTextErrorEvent
            }

            addSource(mutableSearchClickEventLiveData) {
                if (it && emailText.isNotEmpty() && !emailText.isEmailPattern())
                    mutableChecklistOfTextErrorEvent.add(KEY_CLICK_EVENT)
                else
                    mutableChecklistOfTextErrorEvent.remove(KEY_CLICK_EVENT)
                value = mutableChecklistOfTextErrorEvent
            }
        }
    }

    /** Add sources mediator List of Missing fields LiveData */
    private fun addSourcesToMediatorMissingFieldLiveData() {
        mediatorListOfMissingFieldsLiveData.apply {
            addSource(mutableCurrencySymbolLiveData) {
                if (it.isEmpty())
                    mutableListOfBlankFields.add(KEY_ERR_GROUP_CURRENCY)
                else
                    mutableListOfBlankFields.remove(KEY_ERR_GROUP_CURRENCY)
                value = mutableListOfBlankFields
            }

            addSource(mutableGroupNameLiveData) {
                if (it.isEmpty())
                    mutableListOfBlankFields.add(KEY_ERR_GROUP_NAME)
                else
                    mutableListOfBlankFields.remove(KEY_ERR_GROUP_NAME)
                value = mutableListOfBlankFields
            }

            addSource(mutableGroupDescLiveData) {
                if (it.isEmpty())
                    mutableListOfBlankFields.add(KEY_ERR_GROUP_DESC)
                else
                    mutableListOfBlankFields.remove(KEY_ERR_GROUP_DESC)
                value = mutableListOfBlankFields
            }

            addSource(mutableListOfAddedUserLiveData) {
                if (it.isEmpty())
                    mutableListOfBlankFields.add(KEY_ERR_PARTICIPANTS_COUNT)
                else
                    mutableListOfBlankFields.remove(KEY_ERR_PARTICIPANTS_COUNT)
                value = mutableListOfBlankFields
            }
        }
    }

    init {
        addSourcesToMediatorListOfSearchEventLiveData()
        addSourcesToMediatorMissingFieldLiveData()
        addSourcesToMediatorErrorStateLiveData()
    }

    companion object {
        private const val KEY_CLICK_EVENT = "click_event"
        private const val KEY_TEXT_EVENT = "text_event"
        const val KEY_ERR_GROUP_NAME = "err_group_name"
        const val KEY_ERR_GROUP_DESC = "err_group_desc"
        const val KEY_ERR_GROUP_CURRENCY = "err_group_currency"
        const val KEY_ERR_PARTICIPANTS_COUNT = "err_participants_count"
    }
}