package com.havos.lubricerp.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class SecureProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "entity_id")
    val entityId: Int = PRIMARY_ID,
    @ColumnInfo(name = "encrypted_id")
    val encryptedId: String,
    @ColumnInfo(name = "encrypted_email")
    val encryptedEmail: String,
    @ColumnInfo(name = "encrypted_full_name")
    val encryptedFullName: String,
    @ColumnInfo(name = "encrypted_branch_id")
    val encryptedBranchId: String,
    @ColumnInfo(name = "encrypted_roles")
    val encryptedRoles: String,
    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long
) {
    companion object {
        const val PRIMARY_ID = 1

        fun fromProfileData(profile: ProfileData, cryptoManager: SecureCryptoManager): SecureProfileEntity {
            return SecureProfileEntity(
                entityId = PRIMARY_ID,
                encryptedId = cryptoManager.encrypt(profile.id.toString()),
                encryptedEmail = cryptoManager.encrypt(profile.email),
                encryptedFullName = cryptoManager.encrypt(profile.fullName),
                encryptedBranchId = cryptoManager.encrypt(profile.branchId.toString()),
                encryptedRoles = cryptoManager.encrypt(profile.roles.joinToString(ROLES_SEPARATOR)),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        }
    }
}

internal fun SecureProfileEntity.toProfileData(cryptoManager: SecureCryptoManager): ProfileData {
    val decryptedRoles = cryptoManager.decrypt(encryptedRoles)
        .split(ROLES_SEPARATOR)
        .map { it.trim() }
        .filter { it.isNotBlank() }

    return ProfileData(
        id = cryptoManager.decrypt(encryptedId).toLongOrNull() ?: 0L,
        email = cryptoManager.decrypt(encryptedEmail),
        fullName = cryptoManager.decrypt(encryptedFullName),
        branchId = cryptoManager.decrypt(encryptedBranchId).toLongOrNull() ?: 0L,
        roles = decryptedRoles
    )
}

private const val ROLES_SEPARATOR = "|,|"
