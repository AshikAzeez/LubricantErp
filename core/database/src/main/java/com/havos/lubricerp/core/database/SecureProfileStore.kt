package com.havos.lubricerp.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class ProfileData(
    val id: Long,
    val email: String,
    val fullName: String,
    val branchId: Long,
    val roles: List<String>
)

interface SecureProfileStore {
    val profileFlow: Flow<ProfileData?>

    suspend fun getProfile(): ProfileData?
    suspend fun saveProfile(profile: ProfileData)
    suspend fun clearProfile()
}

class SecureProfileStoreImpl(
    private val profileDao: SecureProfileDao,
    private val cryptoManager: SecureCryptoManager
) : SecureProfileStore {

    override val profileFlow: Flow<ProfileData?> = profileDao.observeProfile(SecureProfileEntity.PRIMARY_ID)
        .catch { emit(null) }
        .map { it?.toProfileData(cryptoManager) }
        .flowOn(Dispatchers.IO)

    override suspend fun getProfile(): ProfileData? = withContext(Dispatchers.IO) {
        profileDao.getProfile(SecureProfileEntity.PRIMARY_ID)?.toProfileData(cryptoManager)
    }

    override suspend fun saveProfile(profile: ProfileData) = withContext(Dispatchers.IO) {
        profileDao.upsert(
            SecureProfileEntity.fromProfileData(profile, cryptoManager)
        )
    }

    override suspend fun clearProfile() = withContext(Dispatchers.IO) {
        profileDao.deleteAll()
    }
}
