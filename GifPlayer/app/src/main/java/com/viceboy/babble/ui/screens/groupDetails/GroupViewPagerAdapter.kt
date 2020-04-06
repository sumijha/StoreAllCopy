package com.viceboy.babble.ui.screens.groupDetails

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class GroupViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> GroupExpenseFragment()
            2 -> GroupTransactionsFragment()
            0 -> GroupMembersFragment()
            else -> GroupExpenseFragment()
        }
    }
}