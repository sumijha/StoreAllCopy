package com.viceboy.babble.ui.screens.expenseDetails


import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentExpenseDetailsBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.data_repo.model.uiModel.GroupExpense

class ExpenseDetailsFragment :
    BaseHomeFragment<ExpenseDetailsViewModel, FragmentExpenseDetailsBinding>(), Injectable {

    private var groupExpense: GroupExpense? = null

    private val navArgs by navArgs<ExpenseDetailsFragmentArgs>()

    override fun layoutRes(): Int = R.layout.fragment_expense_details

    override fun onCreateView() = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGroupExpense()
        setUpBinding()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(
        viewModel: ExpenseDetailsViewModel,
        binding: FragmentExpenseDetailsBinding
    ) {
        viewModel.expenseInfoLiveData.observe(viewLifecycleOwner, Observer {
            //TODO: Complete method body
        })
    }

    override val viewModelClass: Class<ExpenseDetailsViewModel> =
        ExpenseDetailsViewModel::class.java

    override val hasBottomNavigationView: Boolean = false

    private fun initGroupExpense() {
        arguments?.let {
            groupExpense = navArgs.expense
            groupExpense?.let { viewModel.setGroupExpense(it) }
        }
    }

    private fun setUpBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }
}
