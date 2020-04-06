package com.viceboy.babble.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.viceboy.babble.ui.base.ViewModelFactory
import com.viceboy.babble.ui.screens.addExpense.AddExpenseViewModel
import com.viceboy.babble.ui.screens.addGroup.AddGroupViewModel
import com.viceboy.babble.ui.screens.captureExpense.CaptureExpenseViewModel
import com.viceboy.babble.ui.screens.dashboard.DashboardViewModel
import com.viceboy.babble.ui.screens.expenseDetails.ExpenseDetailsViewModel
import com.viceboy.babble.ui.screens.groupDetails.GroupDetailsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    abstract fun bindsDashboardViewModelModule(viewModel: DashboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddGroupViewModel::class)
    abstract fun bindsAddGroupViewModelModule(viewModel: AddGroupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExpenseDetailsViewModel::class)
    abstract fun bindsExpenseDetailsViewModelModule(viewModel: ExpenseDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @MainActivityScope
    @ViewModelKey(AddExpenseViewModel::class)
    abstract fun bindsAddExpenseViewModelModule(viewModel: AddExpenseViewModel): ViewModel

    @Binds
    @IntoMap
    @MainActivityScope
    @ViewModelKey(GroupDetailsViewModel::class)
    abstract fun bindsGroupDetailsViewModelModule(viewModel: GroupDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CaptureExpenseViewModel::class)
    abstract fun bindsCaptureExpenseViewModelModule(viewModel: CaptureExpenseViewModel): ViewModel

    @Binds
    abstract fun bindsViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}