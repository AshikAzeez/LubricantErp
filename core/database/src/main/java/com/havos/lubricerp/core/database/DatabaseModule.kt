package com.havos.lubricerp.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule = module {
    single { SecureCryptoManager() }
    single<SecureSessionStore> { SecureSessionStoreImpl(androidContext(), get()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            SecureProfileDatabase::class.java,
            "lubricerp_secure.db"
        ).build()
    }
    single { get<SecureProfileDatabase>().secureProfileDao() }
    single<SecureProfileStore> { SecureProfileStoreImpl(get(), get()) }
}
