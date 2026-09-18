package com.shopmind.di

import androidx.room.Room
import com.shopmind.data.local.database.AppDatabase
import com.shopmind.data.local.datastore.UserDataStore
import com.shopmind.data.remote.api.ProductApiService
import com.shopmind.data.repository.AuthRepository
import com.shopmind.viewmodel.AuthViewModel
import com.shopmind.viewmodel.MainViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val appModule = module {
    // Local Database
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

    // Network
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(get())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    single { get<Retrofit>().create(ProductApiService::class.java) }

    // DataStore & Repositories
    single { UserDataStore(androidContext()) }
    single { AuthRepository(get(), get()) }

    viewModel { MainViewModel(get()) }
    viewModel { AuthViewModel(get()) }
}
