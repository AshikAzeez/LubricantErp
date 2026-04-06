package com.havos.lubricerp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SecureProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SecureProfileDatabase : RoomDatabase() {
    abstract fun secureProfileDao(): SecureProfileDao
}
