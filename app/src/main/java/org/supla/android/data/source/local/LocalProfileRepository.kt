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

import org.supla.android.Encryption
import org.supla.android.Preferences
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ColorEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.db.AuthProfileItem
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.AuthInfo
import kotlin.random.Random

class LocalProfileRepository(provider: DatabaseAccessProvider) : ProfileRepository, BaseDao(provider) {

  private val randomGenerator = Random(System.nanoTime())

  override fun createProfile(profile: AuthProfileItem): Long {
    profile.authInfo.guid = encrypted(randomGenerator.nextBytes(SuplaConst.SUPLA_GUID_SIZE))
    profile.authInfo.authKey = encrypted(randomGenerator.nextBytes(SuplaConst.SUPLA_AUTHKEY_SIZE))
    return insert(profile, ProfileEntity.TABLE_NAME)
  }

  override fun getProfile(id: Long): AuthProfileItem? {
    return getItem(
      { makeEmptyAuthItem() },
      ProfileEntity.ALL_COLUMNS,
      ProfileEntity.TABLE_NAME,
      key(ProfileEntity.COLUMN_ID, id)
    )
  }

  override fun deleteProfile(id: Long) {
    delete(
      ProfileEntity.TABLE_NAME,
      key(ProfileEntity.COLUMN_ID, id)
    )
    removeProfileBoundData(id)
  }

  override fun updateProfile(profile: AuthProfileItem) {
    val prev = getProfile(profile.id)
    if (prev != null && prev.authInfoChanged(profile)) {
      removeProfileBoundData(profile.id)
      if (profile.authInfo.emailAddress != prev.authInfo.emailAddress && profile.authInfo.serverAutoDetect) {
        profile.authInfo = profile.authInfo.copy(serverForEmail = "")
      }
    }
    update(
      profile,
      ProfileEntity.TABLE_NAME,
      key(ProfileEntity.COLUMN_ID, profile.id)
    )
  }

  override val allProfiles: List<AuthProfileItem>
    get() {
      return read {
        val rv = mutableListOf<AuthProfileItem>()
        val cursor =
          it.query(
            ProfileEntity.TABLE_NAME,
            ProfileEntity.ALL_COLUMNS,
            null /*selection*/,
            null /*selectionArgs*/,
            null /*groupBy*/,
            null /*having*/,
            ProfileEntity.COLUMN_ID /*order by*/,
            null /*limit*/
          )
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
          val itm = makeEmptyAuthItem()
          itm.AssignCursorData(cursor)
          rv.add(itm)
          cursor.moveToNext()
        }
        cursor.close()
        rv
      }
    }

  private fun makeEmptyAuthItem(): AuthProfileItem {
    return AuthProfileItem(
      name = "",
      authInfo = AuthInfo(
        emailAuth = true,
        serverAutoDetect = true
      ),
      advancedAuthSetup = false,
      isActive = false
    )
  }

  private fun encrypted(bytes: ByteArray): ByteArray {
    val key = Preferences.getDeviceID(SuplaApp.getApp())
    return Encryption.encryptDataWithNullOnException(bytes, key)
  }

  private fun removeProfileBoundData(id: Long) {
    val tables = mapOf(
      LocationEntity.TABLE_NAME to LocationEntity.COLUMN_PROFILE_ID,
      ChannelEntity.TABLE_NAME to ChannelEntity.COLUMN_PROFILE_ID,
      ChannelValueEntity.TABLE_NAME to ChannelValueEntity.COLUMN_PROFILE_ID,
      ChannelExtendedValueEntity.TABLE_NAME to ChannelExtendedValueEntity.COLUMN_PROFILE_ID,
      ColorEntity.TABLE_NAME to ColorEntity.COLUMN_PROFILE_ID,
      ChannelGroupEntity.TABLE_NAME to ChannelGroupEntity.COLUMN_PROFILE_ID,
      ChannelGroupRelationEntity.TABLE_NAME to ChannelGroupRelationEntity.COLUMN_PROFILE_ID,
      SceneEntity.TABLE_NAME to SceneEntity.COLUMN_PROFILE_ID,
      UserIconEntity.TABLE_NAME to UserIconEntity.COLUMN_PROFILE_ID,
      ChannelRelationEntity.TABLE_NAME to ChannelRelationEntity.COLUMN_PROFILE_ID,
      ChannelConfigEntity.TABLE_NAME to ChannelConfigEntity.COLUMN_PROFILE_ID,
      ChannelStateEntity.TABLE_NAME to ChannelStateEntity.COLUMN_PROFILE_ID,
      AndroidAutoItemEntity.TABLE_NAME to AndroidAutoItemEntity.COLUMN_PROFILE_ID
    )
    for ((table, column) in tables) {
      delete(table, key(column, id))
    }
  }
}
