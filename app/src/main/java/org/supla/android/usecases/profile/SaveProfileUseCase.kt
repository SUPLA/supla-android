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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.supla.android.Encryption
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.di.RANDOM_GENERATOR
import org.supla.android.lib.SuplaConst
import org.supla.core.shared.extensions.forTrue
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SaveProfileUseCase @Inject constructor(
  private val deleteProfileRelatedDataUseCase: DeleteProfileRelatedDataUseCase,
  private val profileRepository: ProfileRepository,
  @param:Named(RANDOM_GENERATOR) private val randomGenerator: Random,
  @param:ApplicationContext private val context: Context
) {

  operator fun invoke(profile: ProfileEntity): Single<Result> =
    profileRepository.findAllProfiles()
      .firstOrError()
      .flatMap {
        validation(profile, it)
          .andThen(save(profile, it))
      }

  private fun save(profileEntity: ProfileEntity, profiles: List<ProfileEntity>): Single<Result> {
    // No id - insert
    val originalProfile = profiles.firstOrNull { it.id == profileEntity.id }
    if (profileEntity.id == null || originalProfile == null) {
      val toInsert = profileEntity.asNew(profiles)
      return profileRepository.insert(toInsert)
        .map { Result(it, reconnectNeeded = toInsert.active == true) }
    }

    // No authorization data change - just update
    if (!originalProfile.authDataChanged(profileEntity)) {
      return profileRepository.update(profileEntity)
        .andThen(Single.just(Result(profileEntity.id, false)))
    }

    return deleteProfileRelatedDataUseCase(profileEntity.id)
      .andThen(profileRepository.update(profileEntity.asUpdate()))
      .andThen(Single.just(Result(profileEntity.id, profileEntity.active == true)))
  }

  private fun encrypted(bytes: ByteArray): ByteArray {
    val key = Preferences.getDeviceID(SuplaApp.getApp())
    return Encryption.encryptDataWithNullOnException(bytes, key)
  }

  private fun validation(
    profile: ProfileEntity,
    allProfiles: List<ProfileEntity>
  ): Completable = Completable.fromRunnable {
    if (allProfiles.isNotEmpty() && profile.name.isEmpty()) {
      throw SaveAccountException.EmptyName()
    } else if (isNameDuplicated(profile, allProfiles)) {
      throw SaveAccountException.DuplicatedName()
    } else if (!profile.isAuthDataComplete) {
      throw SaveAccountException.DataIncomplete()
    }
  }

  private fun isNameDuplicated(
    profile: ProfileEntity,
    allProfiles: List<ProfileEntity>
  ): Boolean =
    allProfiles
      .filter { it.id != profile.id }
      // New profile name is trimmed by creation. Old profile name may not be trimmed!
      .firstOrNull { it.name.trim() == profile.name } != null

  private fun ProfileEntity.asNew(profiles: List<ProfileEntity>): ProfileEntity =
    copy(
      name = profiles.isEmpty().forTrue { context.getString(R.string.profile_default_name) } ?: name,
      active = profiles.isEmpty(),
      guid = guid.isNullOrEmpty.forTrue { encrypted(randomGenerator.nextBytes(SuplaConst.SUPLA_GUID_SIZE)) } ?: guid,
      authKey = authKey.isNullOrEmpty.forTrue { encrypted(randomGenerator.nextBytes(SuplaConst.SUPLA_AUTHKEY_SIZE)) } ?: authKey
    )

  private fun ProfileEntity.asUpdate(): ProfileEntity =
    copy(serverForEmail = if (isEmailWithAutoDetect) "" else serverForEmail)

  sealed class SaveAccountException : RuntimeException(null, null) {
    class EmptyName : SaveAccountException()
    class DuplicatedName : SaveAccountException()
    class DataIncomplete : SaveAccountException()
  }

  data class Result(
    val profileId: Long,
    val reconnectNeeded: Boolean
  )
}

private val ByteArray?.isNullOrEmpty: Boolean
  get() = this == null || this.isEmpty()

private val ProfileEntity.isEmailWithAutoDetect: Boolean
  get() = emailAuth && serverAutoDetect
