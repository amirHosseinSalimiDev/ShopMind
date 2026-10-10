package com.shopmind.di

import androidx.room.Room
import com.shopmind.data.local.database.AppDatabase
import com.shopmind.data.local.datastore.UserDataStore
import com.shopmind.data.remote.api.ProductApiService
import com.shopmind.data.repository.AuthRepository
import com.shopmind.data.repository.OrderRepository
import com.shopmind.data.repository.ProductRepository
import com.shopmind.recommendation.data.RecommendationRepository
import com.shopmind.recommendation.data.RecommendationRepositoryImpl
import com.shopmind.recommendation.ml.FeatureMetadata
import com.shopmind.recommendation.ml.LiteRTRecommendationEngine
import com.shopmind.recommendation.ml.ProductFeatureEncoder
import com.shopmind.recommendation.ml.UserProfileBuilder
import com.shopmind.viewmodel.AuthViewModel
import com.shopmind.viewmodel.MainViewModel
import com.shopmind.viewmodel.ProductViewModel
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
    single { ProductRepository(get(), get()) }
    single { OrderRepository(get()) }

    // Recommendation ML Components & Repository
    single { FeatureMetadata.loadFromAssets(androidContext()) }
    single { ProductFeatureEncoder(get()) }
    single { UserProfileBuilder(get()) }
    single { LiteRTRecommendationEngine.create(androidContext()) }
    single<RecommendationRepository> {
        RecommendationRepositoryImpl(
            productDao = get(),
            userDataStore = get(),
            encoder = get(),
            profileBuilder = get(),
            engine = get()
        )
    }

    viewModel { MainViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { ProductViewModel(get(), get(), get(), get()) }
}
