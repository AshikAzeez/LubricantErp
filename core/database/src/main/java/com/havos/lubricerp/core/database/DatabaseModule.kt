package com.havos.lubricerp.core.database

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule = module {
    single { SecureCryptoManager() }
    single<SecureSessionStore> { SecureSessionStoreImpl(androidContext(), get()) }
}
