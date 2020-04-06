package com.viceboy.babble.data_provider.mockRepository

import com.viceboy.data_repo.repository.ExpenseRepository
import com.viceboy.data_repo.repository.GroupsRepository
import com.viceboy.data_repo.repository.TransactionRepository
import com.viceboy.data_repo.repository.UserRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryProviderModule {

    @Binds
    abstract fun bindsMockUserRepo(userRepository: MockUserRepository): UserRepository

    @Binds
    abstract fun bindsMockGroupRepo(groupRepository: MockGroupRepository): GroupsRepository

    @Binds
    abstract fun bindsMockExpenseRepo(expenseRepository: MockExpenseRepository): ExpenseRepository

    @Binds
    abstract fun bindsMockTransactionRepository(transactionRepository: MockTransactionRepository): TransactionRepository
}