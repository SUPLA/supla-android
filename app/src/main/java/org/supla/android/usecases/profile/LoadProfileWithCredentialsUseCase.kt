package org.supla.android.usecases.profile
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

import kotlinx.coroutines.rx3.await
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.settings.ProfileCredentials
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadProfileWithCredentialsUseCase @Inject constructor(
  private val encryptedPreferences: EncryptedPreferences,
  private val profileRepository: ProfileRepository,
) {
  suspend operator fun invoke(profileId: Long): ProfileWithCredentials? {
    val profile = runCatching { profileRepository.findProfile(profileId).await() }.getOrNull() ?: return null
    val credentials = encryptedPreferences.getProfileCredentials(profile.id)

    return ProfileWithCredentials(
      profileEntity = profile,
      profileCredentials = credentials
    )
  }
}

data class ProfileWithCredentials(
  val profileEntity: ProfileEntity,
  val profileCredentials: ProfileCredentials
)
