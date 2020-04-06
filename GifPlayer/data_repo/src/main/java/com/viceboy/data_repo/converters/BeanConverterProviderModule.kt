package com.viceboy.data_repo.converters

import com.viceboy.data_repo.converters.dashboard.*
import com.viceboy.data_repo.converters.group.*
import dagger.Binds
import dagger.Module


@Module
abstract class BeanConverterProviderModule {

    @Binds
    abstract fun bindsDashboardGroupBeanConverter(dashboardGroupBeanConverterImpl: DashboardGroupBeanConverterImpl): DashboardGroupBeanConverter

    @Binds
    abstract fun bindsDashboardMemberBeanConverter(dashboardMemberBeanConverterImpl: DashboardMemberBeanConverterImpl): DashboardMemberBeanConverter

    @Binds
    abstract fun bindsDashboardExpenseBeanConverter(dashboardExpenseBeanConverterImpl: DashboardExpenseBeanConverterImpl): DashboardExpenseBeanConverter

    @Binds
    abstract fun bindsGroupExpenseBeanConverter(groupExpenseBeanConverterImpl: GroupExpenseBeanConverterImpl): GroupExpenseBeanConverter

    @Binds
    abstract fun bindsGroupTransactionBeanConverter(groupTransactionBeanConverterImpl: GroupTransactionBeanConverterImpl): GroupTransactionBeanConverter

    @Binds
    abstract fun bindsGroupMemberBeanConverter(groupMemberBeanConverterImpl: GroupMemberBeanConverterImpl): GroupMemberBeanConverter

    @Binds
    abstract fun bindsGroupExpenseInfoBeanConverter(groupExpenseBeanConverterImpl: GroupExpenseBeanConverterImpl): GroupExpenseBeanConverter
}
