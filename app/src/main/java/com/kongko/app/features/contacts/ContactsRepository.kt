package com.kongko.app.features.contacts

import android.content.Context
import android.provider.ContactsContract
import com.kongko.app.core.network.KongkoApiService
import com.kongko.app.core.network.dto.SyncContactsRequest
import com.kongko.app.core.network.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class LocalContact(
    val name: String,
    val phoneNumber: String
)

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: KongkoApiService
) {
    suspend fun fetchDeviceContacts(): List<LocalContact> = withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<LocalContact>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: "Tanpa Nama"
                val rawNumber = it.getString(numberIdx) ?: continue
                val normalizedNumber = normalizePhoneNumber(rawNumber)
                if (normalizedNumber.isNotBlank()) {
                    contactsList.add(LocalContact(name, normalizedNumber))
                }
            }
        }

        contactsList.distinctBy { it.phoneNumber }
    }

    suspend fun syncContactsWithServer(): List<UserDto> = withContext(Dispatchers.IO) {
        val localContacts = fetchDeviceContacts()
        val numbers = localContacts.map { it.phoneNumber }
        if (numbers.isEmpty()) return@withContext emptyList()

        val response = apiService.syncContacts(SyncContactsRequest(numbers))
        if (response.isSuccessful && response.body()?.data != null) {
            response.body()!!.data!!
        } else {
            emptyList()
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        var clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.startsWith("0")) {
            clean = "+62" + clean.substring(1)
        } else if (clean.startsWith("62")) {
            clean = "+$clean"
        }
        return clean
    }
}
