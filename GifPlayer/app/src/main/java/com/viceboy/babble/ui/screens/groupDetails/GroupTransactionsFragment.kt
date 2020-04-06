package com.viceboy.babble.ui.screens.groupDetails

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentGroupTransactionsBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.state.DataLoad
import com.viceboy.babble.ui.state.Resource

class GroupTransactionsFragment :
    BaseHomeFragment<GroupDetailsViewModel, FragmentGroupTransactionsBinding>(), Injectable {

    private var paymentDetailsAdapter: PaymentDetailsAdapter? = null

    override fun layoutRes(): Int = R.layout.fragment_group_transactions

    override fun onCreateView() {
        initPaymentDetailsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvTransactions.apply {
            rvGroupTabsRecyclerList.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    VERTICAL
                )
            )
            rvGroupTabsRecyclerList.adapter = paymentDetailsAdapter
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(
        viewModel: GroupDetailsViewModel,
        binding: FragmentGroupTransactionsBinding
    ) {
        viewModel.groupTransactionsLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> binding.dataLoadState = DataLoad.IN_PROGRESS
                is Resource.Failure -> binding.dataLoadState = DataLoad.ERROR
                is Resource.Success -> {
                    binding.dataLoadState = DataLoad.SUCCESS
                    paymentDetailsAdapter?.submitList(it.data)
                }
            }
        })

        viewModel.tabIndexLiveData.observe(viewLifecycleOwner, Observer {
            if (it == 1) {
                if (binding.rvTransactions.root.visibility == View.VISIBLE)
                    paymentDetailsAdapter?.notifyDataSetChanged()
                else
                    binding.noTransactionPlaceholder.lnrLytNoPayment.requestLayout()
            }
        })
    }

    private fun initPaymentDetailsAdapter() {
        paymentDetailsAdapter = PaymentDetailsAdapter()
    }

    override val viewModelClass: Class<GroupDetailsViewModel> = GroupDetailsViewModel::class.java
    override val hasBottomNavigationView: Boolean = false
}
