package com.havos.lubricerp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecureProfileDao {
    @Query("SELECT * FROM profile WHERE entity_id = :entityId LIMIT 1")
    fun observeProfile(entityId: Int): Flow<SecureProfileEntity?>

    @Query("SELECT * FROM profile WHERE entity_id = :entityId LIMIT 1")
    suspend fun getProfile(entityId: Int): SecureProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: SecureProfileEntity)

    @Query("DELETE FROM profile")
    suspend fun deleteAll()
}
