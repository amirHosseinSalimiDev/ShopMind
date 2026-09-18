package com.shopmind.di

import com.shopmind.data.local.datastore.UserDataStore
import com.shopmind.data.repository.AuthRepository
import com.shopmind.viewmodel.AuthViewModel
import com.shopmind.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { UserDataStore(androidContext()) }
    single { AuthRepository(get()) }

    viewModel { MainViewModel(get()) }
    viewModel { AuthViewModel(get()) }
}
