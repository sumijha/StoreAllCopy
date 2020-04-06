package com.viceboy.babble.di

import com.viceboy.babble.ui.screens.addExpense.AddExpenseFragment
import com.viceboy.babble.ui.screens.addExpense.dialog.SelectDateFragment
import com.viceboy.babble.ui.screens.addGroup.AddGroupFragment
import com.viceboy.babble.ui.screens.captureExpense.CaptureExpenseFragment
import com.viceboy.babble.ui.screens.dashboard.DashboardFragment
import com.viceboy.babble.ui.screens.expenseDetails.ExpenseDetailsFragment
import com.viceboy.babble.ui.screens.groupDetails.GroupDetailsFragment
import com.viceboy.babble.ui.screens.groupDetails.GroupExpenseFragment
import com.viceboy.babble.ui.screens.groupDetails.GroupMembersFragment
import com.viceboy.babble.ui.screens.groupDetails.GroupTransactionsFragment
import com.viceboy.babble.ui.screens.groupDetails.dialog.GroupSettlementBottomSheetFragment
import com.viceboy.babble.ui.screens.groupDetails.dialog.GroupSettlementConfirmBottomSheetFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBindingModule {
    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): DashboardFragment

    @ContributesAndroidInjector
    abstract fun contributeAddDashboardFragment(): AddGroupFragment

    @ContributesAndroidInjector
    abstract fun contributeAddExpenseFragment(): AddExpenseFragment

    @ContributesAndroidInjector
    abstract fun contributeGroupDetailsFragment(): GroupDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeExpenseDetailsFragment(): ExpenseDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeCaptureExpenseFragment(): CaptureExpenseFragment

    @ContributesAndroidInjector
    abstract fun contributeMembersFragment(): GroupMembersFragment

    @ContributesAndroidInjector
    abstract fun contributeExpenseFragment(): GroupExpenseFragment

    @ContributesAndroidInjector
    abstract fun contributeTransactionFragment(): GroupTransactionsFragment

    @ContributesAndroidInjector
    abstract fun contributeSelectDateFragment(): SelectDateFragment

    @ContributesAndroidInjector
    abstract fun contributeGroupSettlementBottomSheetFragment(): GroupSettlementBottomSheetFragment

    @ContributesAndroidInjector
    abstract fun contributeGroupSettlementConfirmationFragment(): GroupSettlementConfirmBottomSheetFragment
}