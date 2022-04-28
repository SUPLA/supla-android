package org.supla.android.data.source.local

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import kotlin.random.Random
import android.content.ContentValues
import org.supla.android.Preferences
import org.supla.android.SuplaApp
import org.supla.android.Encryption
import org.supla.android.lib.SuplaConst
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.SuplaContract
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.AuthInfo

class LocalProfileRepository(provider: DatabaseAccessProvider): ProfileRepository, BaseDao(provider) {


    override fun createNamedProfile(name: String): Long {
        var itm = makeEmptyAuthItem()
        itm.name = name
        itm.authInfo.guid = encrypted(Random.nextBytes(SuplaConst.SUPLA_GUID_SIZE))
        itm.authInfo.authKey = encrypted(Random.nextBytes(SuplaConst.SUPLA_AUTHKEY_SIZE))
        return insert(itm, 
                      SuplaContract.AuthProfileEntry.TABLE_NAME)
    }

    override fun getCurrentProfileId(): Long? {
        return allProfiles.filter { it.isActive == true }.first()?.id
    }

    override fun getProfile(id: Long): AuthProfileItem? {
        return getItem({ makeEmptyAuthItem() }, 
                       SuplaContract.AuthProfileEntry.ALL_COLUMNS, 
                       SuplaContract.AuthProfileEntry.TABLE_NAME,
                       key(SuplaContract.AuthProfileEntry._ID, id))
    }

    override fun deleteProfile(id: Long) {
        delete(SuplaContract.AuthProfileEntry.TABLE_NAME,
               key(SuplaContract.AuthProfileEntry._ID, id))
        val tables = listOf(SuplaContract.LocationEntry.TABLE_NAME,
                            SuplaContract.ChannelEntry.TABLE_NAME,
                            SuplaContract.ChannelValueEntry.TABLE_NAME,
                            SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
                            SuplaContract.ColorListItemEntry.TABLE_NAME,
                            SuplaContract.ChannelGroupEntry.TABLE_NAME,
                            SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                            SuplaContract.UserIconsEntry.TABLE_NAME)
        for(table in tables) {
            delete(table, key(SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID, id))
        }
    }

    override fun updateProfile(profile: AuthProfileItem) {
        update(profile, SuplaContract.AuthProfileEntry.TABLE_NAME,
               key(SuplaContract.AuthProfileEntry._ID, profile.id))
    }

    override val allProfiles: List<AuthProfileItem>
        get() {
            return read() {
                var rv = mutableListOf<AuthProfileItem>()
                val cursor = 
                    it.query(SuplaContract.AuthProfileEntry.TABLE_NAME,
                             SuplaContract.AuthProfileEntry.ALL_COLUMNS,
                             null /*selection*/,
                             null /*selectionArgs*/,
                             null /*groupBy*/,
                             null /*having*/,
                             SuplaContract.AuthProfileEntry._ID /*order by*/,
                             null /*limit*/)
                cursor.moveToFirst()
                while(!cursor.isAfterLast()) {
                    val itm = makeEmptyAuthItem()
                    itm.AssignCursorData(cursor)
                    rv.add(itm)
                    cursor.moveToNext()
                }
                cursor.close()
                rv
            }
        }

    override fun setProfileActive(id: Long): Boolean {
        return write<Boolean> { db ->
                var rv = false                   
                db.beginTransaction()
                try {
                    val cv1 = ContentValues()
                    cv1.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_IS_ACTIVE, 0)
                    db.update(SuplaContract.AuthProfileEntry.TABLE_NAME, cv1, null,
                              null)
                    val cv2 = ContentValues()
                    cv2.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_IS_ACTIVE, 1)
                    db.update(SuplaContract.AuthProfileEntry.TABLE_NAME, cv2,
                              SuplaContract.AuthProfileEntry._ID + " = ?",
                              arrayOf(id.toString()))
                    android.util.Log.d("SuplaProfile", "activated profile: " + id)
                    db.setTransactionSuccessful()
                    rv = true
                } catch(e: Exception) {
                    android.util.Log.e("SuplaProfile", "failed to activate profile: " + id, e)
                } finally {
                    db.endTransaction()
                }
                rv
        }
    }

    private fun makeEmptyAuthItem(): AuthProfileItem {
        return AuthProfileItem(name = "",
                               authInfo = AuthInfo(emailAuth = true,
                                                   serverAutoDetect = true),
                               advancedAuthSetup = false,
                               isActive = false)
    }

    private fun encrypted(bytes: ByteArray): ByteArray {
        val key = Preferences.getDeviceID(SuplaApp.getApp())
        return Encryption.encryptDataWithNullOnException(bytes, key)
    }

}
