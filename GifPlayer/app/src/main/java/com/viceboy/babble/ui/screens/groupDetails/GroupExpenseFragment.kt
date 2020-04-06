package com.viceboy.babble.ui.screens.groupDetails

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentGroupExpenseBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.state.DataLoad
import com.viceboy.babble.ui.state.Resource

class GroupExpenseFragment : BaseHomeFragment<GroupDetailsViewModel, FragmentGroupExpenseBinding>(),
    Injectable {

    private var expenseDetailsAdapter: ExpenseDetailsAdapter? = null

    override fun layoutRes(): Int = R.layout.fragment_group_expense

    override fun onCreateView() {
        initExpenseDetailsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvGroupExpense.apply {
            rvGroupTabsRecyclerList.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            rvGroupTabsRecyclerList.adapter = expenseDetailsAdapter
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(
        viewModel: GroupDetailsViewModel,
        binding: FragmentGroupExpenseBinding
    ) {
        viewModel.groupExpenseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> binding.dataLoadState = DataLoad.IN_PROGRESS
                is Resource.Failure -> binding.dataLoadState = DataLoad.ERROR
                is Resource.Success -> {
                    binding.dataLoadState = DataLoad.SUCCESS
                    expenseDetailsAdapter?.submitList(it.data)
                }
            }
        })

        viewModel.tabIndexLiveData.observe(viewLifecycleOwner, Observer {
            if (it == 2) {
                if (binding.rvGroupExpense.root.visibility == View.VISIBLE)
                    expenseDetailsAdapter?.notifyDataSetChanged()
                else
                    binding.noExpensePlaceholder.lnrLytNoExpense.requestLayout()
            }
            binding.rvGroupExpense.rvGroupTabsRecyclerList.adapter?.notifyDataSetChanged()
        })
    }

    override val viewModelClass: Class<GroupDetailsViewModel> = GroupDetailsViewModel::class.java
    override val hasBottomNavigationView: Boolean = false

    private fun initExpenseDetailsAdapter() {
        expenseDetailsAdapter = ExpenseDetailsAdapter {
            findNavController().navigate(
                GroupDetailsFragmentDirections.actionGroupDetailsFragmentToExpenseDetailsFragment(
                    this
                )
            )
        }
    }
}
