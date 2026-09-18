package com.shopmind.di

import androidx.room.Room
import com.shopmind.data.local.database.AppDatabase
import com.shopmind.data.local.datastore.UserDataStore
import com.shopmind.data.repository.AuthRepository
import com.shopmind.viewmodel.AuthViewModel
import com.shopmind.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "shopmind_db"
        ).build()
    }

    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().orderDao() }

    single { UserDataStore(androidContext()) }
    single { AuthRepository(get(), get()) }

    viewModel { MainViewModel(get()) }
    viewModel { AuthViewModel(get()) }
}
