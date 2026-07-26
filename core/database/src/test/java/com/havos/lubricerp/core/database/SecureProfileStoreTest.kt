package com.havos.lubricerp.core.database

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SecureProfileStoreTest {

    private val mockDao = mock<SecureProfileDao>()
    private val mockCryptoManager = mock<CryptoManager>()
    private lateinit var store: SecureProfileStore

    private val testProfile = ProfileData(
        id = 42L,
        email = "user@example.com",
        fullName = "Test User",
        branchId = 7L,
        roles = listOf("ADMIN", "SALES")
    )

    @Before
    fun setup() {
        store = SecureProfileStoreImpl(mockDao, mockCryptoManager)

        // Simple reversible fake encryption for assertions
        whenever(mockCryptoManager.encrypt(any())).thenAnswer { invocation ->
            "enc(${invocation.arguments[0]})"
        }
        whenever(mockCryptoManager.decrypt(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as String).removePrefix("enc(").removeSuffix(")")
        }
    }

    private fun entityFrom(profile: ProfileData): SecureProfileEntity =
        SecureProfileEntity(
            entityId = SecureProfileEntity.PRIMARY_ID,
            encryptedId = mockCryptoManager.encrypt(profile.id.toString()),
            encryptedEmail = mockCryptoManager.encrypt(profile.email),
            encryptedFullName = mockCryptoManager.encrypt(profile.fullName),
            encryptedBranchId = mockCryptoManager.encrypt(profile.branchId.toString()),
            encryptedRoles = mockCryptoManager.encrypt(profile.roles.joinToString("|,|")),
            updatedAtEpochMillis = 123456789L
        )

    @Test
    fun `getProfile returns decrypted profile when entity exists`() = runTest {
        val entity = entityFrom(testProfile)
        whenever(mockDao.getProfile(SecureProfileEntity.PRIMARY_ID))
            .thenReturn(entity)

        val result = store.getProfile()

        assertNotNull(result)
        assertEquals(testProfile.id, result!!.id)
        assertEquals(testProfile.email, result.email)
        assertEquals(testProfile.fullName, result.fullName)
        assertEquals(testProfile.branchId, result.branchId)
        assertEquals(testProfile.roles, result.roles)
    }

    @Test
    fun `getProfile returns null when entity does not exist`() = runTest {
        whenever(mockDao.getProfile(SecureProfileEntity.PRIMARY_ID)).thenReturn(null)

        val result = store.getProfile()

        assertNull(result)
    }

    @Test
    fun `saveProfile encrypts fields and upserts entity with primary id`() = runTest {
        store.saveProfile(testProfile)

        val captor = argumentCaptor<SecureProfileEntity>()
        verify(mockDao).upsert(captor.capture())

        val saved = captor.firstValue
        assertEquals(SecureProfileEntity.PRIMARY_ID, saved.entityId)
        assertEquals("enc(42)", saved.encryptedId)
        assertEquals("enc(user@example.com)", saved.encryptedEmail)
        assertEquals("enc(Test User)", saved.encryptedFullName)
        assertEquals("enc(7)", saved.encryptedBranchId)
        assertEquals("enc(ADMIN|,|SALES)", saved.encryptedRoles)
    }

    @Test
    fun `clearProfile delegates to dao deleteAll`() = runTest {
        store.clearProfile()

        verify(mockDao).deleteAll()
    }

    @Test
    fun `fromProfileData encrypts all fields`() {
        val entity = SecureProfileEntity.fromProfileData(testProfile, mockCryptoManager)

        assertEquals(SecureProfileEntity.PRIMARY_ID, entity.entityId)
        assertEquals("enc(42)", entity.encryptedId)
        assertEquals("enc(user@example.com)", entity.encryptedEmail)
        assertEquals("enc(Test User)", entity.encryptedFullName)
        assertEquals("enc(7)", entity.encryptedBranchId)
        assertEquals("enc(ADMIN|,|SALES)", entity.encryptedRoles)
    }

    @Test
    fun `toProfileData decrypts all fields`() {
        val entity = entityFrom(testProfile)

        val result = entity.toProfileData(mockCryptoManager)

        assertEquals(testProfile.id, result.id)
        assertEquals(testProfile.email, result.email)
        assertEquals(testProfile.fullName, result.fullName)
        assertEquals(testProfile.branchId, result.branchId)
        assertEquals(testProfile.roles, result.roles)
    }

    @Test
    fun `toProfileData returns empty roles for blank decrypted roles`() = runTest {
        val entity = SecureProfileEntity(
            entityId = SecureProfileEntity.PRIMARY_ID,
            encryptedId = "enc(1)",
            encryptedEmail = "enc(a@b.com)",
            encryptedFullName = "enc(Name)",
            encryptedBranchId = "enc(2)",
            encryptedRoles = "enc()",
            updatedAtEpochMillis = 0L
        )
        whenever(mockDao.getProfile(SecureProfileEntity.PRIMARY_ID)).thenReturn(entity)

        val result = store.getProfile()

        assertNotNull(result)
        assertEquals(emptyList<String>(), result!!.roles)
    }

    @Test
    fun `toProfileData defaults id and branchId to 0 when not numeric`() = runTest {
        whenever(mockCryptoManager.decrypt(eq("bad_id"))).thenReturn("not_a_number")
        whenever(mockCryptoManager.decrypt(eq("bad_branch"))).thenReturn("not_a_number")

        val entity = SecureProfileEntity(
            entityId = SecureProfileEntity.PRIMARY_ID,
            encryptedId = "bad_id",
            encryptedEmail = "enc(a@b.com)",
            encryptedFullName = "enc(Name)",
            encryptedBranchId = "bad_branch",
            encryptedRoles = "enc(ADMIN)",
            updatedAtEpochMillis = 0L
        )
        whenever(mockDao.getProfile(SecureProfileEntity.PRIMARY_ID)).thenReturn(entity)

        val result = store.getProfile()

        assertNotNull(result)
        assertEquals(0L, result!!.id)
        assertEquals(0L, result.branchId)
    }

    @Test
    fun `getProfile does not interact with dao when store cleared`() = runTest {
        whenever(mockDao.getProfile(SecureProfileEntity.PRIMARY_ID)).thenReturn(null)

        store.clearProfile()

        verify(mockDao, never()).upsert(any())
        verify(mockDao).deleteAll()
    }
}
