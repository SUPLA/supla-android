package org.supla.android.core.infrastructure.suplaclient
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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.settings.ProfileCredentials
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.dto.AuthDataDto
import org.supla.android.lib.singlecall.SingleCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingleCallProvider @Inject constructor(
  private val profileRepository: ProfileRepository,
  private val encryptedPreferences: EncryptedPreferences,
  @param:ApplicationContext private val context: Context
) {
  fun provide(profileId: Long) = SingleCall(context, profileId) {
    runBlocking {
      val credentials = encryptedPreferences.getProfileCredentials(profileId)
      profileRepository.findProfile(profileId).await().getAuthDataDto(credentials)
    }
  }
}

private fun ProfileEntity.getAuthDataDto(credentials: ProfileCredentials): AuthDataDto =
  AuthDataDto(
    emailAuth = emailAuth,
    emailAddress = email,
    serverForEmail = serverForEmail,
    accessId = accessId,
    accessIdPassword = credentials.accessIdPassword,
    serverForAccessId = serverForAccessId,
    preferredProtocolVersion = preferredProtocolVersion,
    guid = credentials.guid,
    authKey = credentials.authKey
  )
