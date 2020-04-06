package com.viceboy.babble.ui.screens.addGroup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mynameismidori.currencypicker.CurrencyPicker
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentAddGroupBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.state.SearchState
import com.viceboy.babble.ui.util.toEditable
import com.viceboy.babble.ui.util.vibrateText
import com.viceboy.data_repo.model.dataModel.User
import kotlinx.android.synthetic.main.fragment_add_group.view.*
import timber.log.Timber


class AddGroupFragment : BaseHomeFragment<AddGroupViewModel, FragmentAddGroupBinding>(), Injectable {

    private val emptyString = ""

    private var isVibrating = false
    private var listOfAddedUser = mutableListOf<User>()

    private var lastSearchedUser: User? = null
    private var userListAdapter: UserListAdapter? = null
    private var currentSearchState: SearchState? = null

    private lateinit var onAddParticipantsFocusText: String
    private lateinit var onAddParticipantsFocusLostText: String
    private lateinit var participantEmail: String

    override fun layoutRes(): Int = R.layout.fragment_add_group

    override fun onCreateView() = Unit

    override val viewModelClass: Class<AddGroupViewModel> = AddGroupViewModel::class.java
    override val hasBottomNavigationView: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAddParticipantsFocusChangeHintText()
        setUpAddParticipantsFocusChangeListener()
        setUpBinding()
        setUpUserListAdapter()
        setUpCurrencyPicker()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(viewModel: AddGroupViewModel, binding: FragmentAddGroupBinding) {
        viewModel.mutableSearchParticipantsTextLiveData.observe(viewLifecycleOwner, Observer {
            participantEmail = it ?: emptyString
        })

        viewModel.currencyCodeLiveData.observe(viewLifecycleOwner, Observer {
            binding.tvCurrency.text = it
        })

        viewModel.currencySymbolLiveData.observe(viewLifecycleOwner, Observer {
            binding.tvCurrencySymbol.text = it
        })

        viewModel.currencyNameLiveData.observe(viewLifecycleOwner, Observer {
            binding.tvSelectCurrency.text = it
        })

        viewModel.listOfCheckedEventLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> viewModel.setSearchState(SearchState.IN_PROGRESS)

                is Resource.Success -> handleSearchSuccessEvent(it.data)

                is Resource.Failure -> handleSearchFailureEvent(it.message)
            }
            viewModel.resetSearchClickEvent()
        })

        viewModel.searchStateLiveData.observe(viewLifecycleOwner, Observer {
            currentSearchState = it
            updateTextStyle(it)
        })

        viewModel.mutableSearchParticipantsTextLiveData.observe(viewLifecycleOwner, Observer {
            currentSearchState?.let { searchState ->
                if (searchState == SearchState.SUCCESS || searchState == SearchState.FAILED)
                    resetClickEvents()
            }
        })

        viewModel.listOfAddedUserLiveData.observe(viewLifecycleOwner, Observer {
            userListAdapter?.submitList(it.toMutableList())
        })

        viewModel.textErrorStateLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (!isVibrating) binding.etAddParticipants.vibrateText(
                    { isVibrating = true },
                    { isVibrating = false })
                updateTextStyle(SearchState.ERROR)
                viewModel.resetSearchClickEvent()
            }
        })

        viewModel.listOfMissingFieldsLiveData.observe(viewLifecycleOwner, Observer {
            Timber.e("List : $it")
        })
    }

    @Suppress("UNUSED_PARAMETER")
    fun onAddUserClick(view: View) {
        lastSearchedUser?.let { user ->
            if (!listOfAddedUser.contains(user)) {
                listOfAddedUser.add(user)
                viewModel.setAddedUserListLiveData(listOfAddedUser)
            } else {
                Toast.makeText(
                    requireContext(),
                    "User ${user.email} is already added to the list",
                    Toast.LENGTH_SHORT
                ).show()
            }
            resetTextField()
        }
    }

    //Setup click listener on Cancel Add Expense
    @Suppress("UNUSED_PARAMETER")
    fun onCancelExpense(view: View) {
        findNavController().popBackStack()
    }

    private fun updateTextStyle(searchState: SearchState) {
        when (searchState) {
            SearchState.SUCCESS -> binding.etAddParticipants.setTextAppearance(R.style.SearchSuccess)
            SearchState.ERROR -> binding.etAddParticipants.setTextAppearance(R.style.ErrorState)
            SearchState.FAILED -> binding.etAddParticipants.setTextAppearance(R.style.SearchIdle)
            else -> binding.etAddParticipants.setTextAppearance(R.style.SearchIdle)
        }
    }

    private fun initAddParticipantsFocusChangeHintText() {
        onAddParticipantsFocusText =
            requireContext().resources.getString(R.string.search_by_email)
        onAddParticipantsFocusLostText =
            requireContext().resources.getString(R.string.add_participants)
    }

    private fun setUpBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            presenter = this@AddGroupFragment
            addGroupViewModel = viewModel
        }
    }

    private fun setUpAddParticipantsFocusChangeListener() {
        binding.etAddParticipants.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                binding.etAddParticipants.hint = onAddParticipantsFocusText
            else
                binding.etAddParticipants.hint = onAddParticipantsFocusLostText
        }
    }

    private fun setUpCurrencyPicker() {
        binding.tvSelectCurrency.setOnClickListener {
            onCurrencyPickerClick()
        }
    }

    private fun setUpUserListAdapter() {
        userListAdapter = UserListAdapter {
            listOfAddedUser.remove(this)
            viewModel.setAddedUserListLiveData(listOfAddedUser)
        }
        binding.rvAddParticipantsList.adapter = userListAdapter
    }

    private fun handleSearchFailureEvent(message: String?) {
        Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
        viewModel.setSearchState(SearchState.FAILED)
    }

    private fun handleSearchSuccessEvent(data: User?) {
        lastSearchedUser = data
        binding.etAddParticipants.text = data?.name?.toEditable()
        viewModel.setSearchState(SearchState.SUCCESS)
    }

    private fun resetClickEvents() {
        viewModel.resetSearchClickEvent()
        viewModel.resetSearchStateEventLiveData()
    }

    private fun resetTextField() {
        binding.etAddParticipants.etAddParticipants.text = emptyString.toEditable()
        binding.etAddParticipants.clearFocus()
    }

    private fun onCurrencyPickerClick() {
        val title = "Select Currency"
        val picker = CurrencyPicker.newInstance(title)

        picker.setListener { name, code, symbol, _ ->
            val currencySymbol = if (code == "INR")
                resources.getString(R.string.ruppee)
            else
                symbol
            viewModel.setCurrencyCode(code)
            viewModel.setCurrencySymbol(currencySymbol)
            viewModel.setCurrencyName(name)
            picker.dismiss()
        }
        picker.show(requireFragmentManager(), "CURRENCY_PICKER")
    }
}
