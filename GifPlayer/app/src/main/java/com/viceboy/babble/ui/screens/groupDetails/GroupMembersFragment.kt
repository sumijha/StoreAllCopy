package com.viceboy.babble.ui.screens.groupDetails

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentGroupMembersBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.state.DataLoad
import com.viceboy.babble.ui.state.Resource

class GroupMembersFragment : BaseHomeFragment<GroupDetailsViewModel, FragmentGroupMembersBinding>(),
    Injectable {
    private var memberDetailsAdapter: MemberDetailsAdapter? = null

    override fun layoutRes(): Int = R.layout.fragment_group_members

    override fun onCreateView() {
        initMemberDetailsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvGroupMembers.apply {
            rvGroupTabsRecyclerList.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            rvGroupTabsRecyclerList.adapter = memberDetailsAdapter
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(
        viewModel: GroupDetailsViewModel,
        binding: FragmentGroupMembersBinding
    ) {
        viewModel.groupMembersLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> binding.dataLoadState = DataLoad.IN_PROGRESS
                is Resource.Failure -> binding.dataLoadState = DataLoad.ERROR
                is Resource.Success -> {
                    binding.dataLoadState = DataLoad.SUCCESS
                    memberDetailsAdapter?.submitList(it.data)
                }
            }
        })
        viewModel.tabIndexLiveData.observe(viewLifecycleOwner, Observer {
            if (it == 0) {
                if (binding.rvGroupMembers.root.visibility == View.VISIBLE)
                    memberDetailsAdapter?.notifyDataSetChanged()
                else
                    binding.noMemberPlaceholder.lnrLytNoMember.requestLayout()
            }
            memberDetailsAdapter?.notifyDataSetChanged()
        })
    }

    override val viewModelClass: Class<GroupDetailsViewModel> = GroupDetailsViewModel::class.java
    override val hasBottomNavigationView: Boolean = false

    private fun initMemberDetailsAdapter() {
        memberDetailsAdapter = MemberDetailsAdapter()
    }
}
