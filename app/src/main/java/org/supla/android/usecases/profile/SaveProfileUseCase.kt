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
import kotlinx.coroutines.rx3.rxSingle
import org.supla.android.R
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.settings.ProfileCredentials
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.di.CoroutineDispatchers
import org.supla.core.shared.extensions.forTrue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveProfileUseCase @Inject constructor(
  private val deleteProfileRelatedDataUseCase: DeleteProfileRelatedDataUseCase,
  private val encryptedPreferences: EncryptedPreferences,
  private val profileRepository: ProfileRepository,
  @param:ApplicationContext private val context: Context,
  private val dispatchers: CoroutineDispatchers
) {

  operator fun invoke(profileDto: ProfileDto): Single<Result> =
    profileRepository.findAllProfiles()
      .firstOrError()
      .flatMap { profiles ->
        validation(profileDto, profiles)
          .andThen(save(profileDto, profiles))
      }

  private fun save(profileDto: ProfileDto, profiles: List<ProfileEntity>): Single<Result> {
    val originalProfile = profiles.firstOrNull { it.id == profileDto.id }

    return if (profileDto.id == 0L || originalProfile == null) {
      insert(profileDto, profiles)
    } else {
      update(profileDto, originalProfile)
    }
  }

  private fun insert(profileDto: ProfileDto, profiles: List<ProfileEntity>): Single<Result> {
    val toInsert = profileDto.entity.asNew(profiles)
    return profileRepository.insert(toInsert)
      .flatMap { updateAccessIdPassword(it, profileDto.accessIdPassword) }
      .map { Result(it, reconnectNeeded = toInsert.active) }
  }

  private fun update(profileDto: ProfileDto, originalProfile: ProfileEntity): Single<Result> =
    rxSingle(dispatchers.io()) { encryptedPreferences.getProfileCredentials(profileDto.id) }
      .flatMap { credentials ->
        val profileEntity = profileDto.entity

        if (!authDataChanged(profileDto, originalProfile, credentials)) {
          profileRepository.update(profileEntity)
            .andThen(Single.just(Result(profileEntity.id, false)))
        } else {
          deleteProfileRelatedDataUseCase(profileEntity.id)
            .andThen(profileRepository.update(profileEntity.asUpdate()))
            .andThen(updateAccessIdPassword(profileEntity.id, profileDto.accessIdPassword, credentials))
            .map { Result(it, profileEntity.active) }
        }
      }

  private fun authDataChanged(profileDto: ProfileDto, profileEntity: ProfileEntity, profileCredentials: ProfileCredentials): Boolean {
    if (profileDto.emailAuth != profileEntity.emailAuth) {
      // Authorization method changed so we're not able to compare if same account will be used.
      return true
    }

    return if (profileDto.emailAuth) {
      (
        profileDto.email != profileEntity.email ||
          profileDto.serverForEmail != profileEntity.serverForEmail ||
          profileDto.serverAutoDetect != profileEntity.serverAutoDetect
        )
    } else {
      (
        profileDto.accessId != profileEntity.accessId ||
          profileDto.serverForAccessId != profileEntity.serverForAccessId ||
          profileDto.accessIdPassword != profileCredentials.accessIdPassword
        )
    }
  }

  private fun validation(
    profile: ProfileDto,
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
    profile: ProfileDto,
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
    )

  private fun ProfileEntity.asUpdate(): ProfileEntity =
    copy(serverForEmail = if (isEmailWithAutoDetect) "" else serverForEmail)

  private fun updateAccessIdPassword(profileId: Long, accessIdPassword: String, credentials: ProfileCredentials? = null) =
    rxSingle(dispatchers.io()) {
      val credentials = credentials ?: encryptedPreferences.getProfileCredentials(profileId)
      if (accessIdPassword != credentials.accessIdPassword) {
        encryptedPreferences.setProfileCredentials(
          profileId = profileId,
          data = credentials.copy(accessIdPassword = accessIdPassword)
        )
      }

      profileId
    }

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

data class ProfileDto(
  val id: Long,
  val name: String,
  val advancedMode: Boolean,
  val emailAuth: Boolean,
  val serverForEmail: String,
  val serverForAccessId: String,
  val serverAutoDetect: Boolean,
  val email: String,
  val accessId: Int,
  val accessIdPassword: String,
  val active: Boolean,
  val position: Int
) {
  val isAuthDataComplete: Boolean
    get() {
      return if (emailAuth) {
        email.isNotEmpty() && (serverAutoDetect || serverForEmail.isNotEmpty())
      } else {
        serverForAccessId.isNotEmpty() && accessId > 0 && accessIdPassword.isNotEmpty()
      }
    }

  val entity: ProfileEntity
    get() = ProfileEntity(
      id = id,
      name = name,
      advancedMode = advancedMode,
      emailAuth = emailAuth,
      serverForEmail = serverForEmail,
      serverForAccessId = serverForAccessId,
      serverAutoDetect = serverAutoDetect,
      email = email,
      accessId = accessId,
      active = active,
      position = position,
      preferredProtocolVersion = 0
    )

  companion object
}

private val ProfileEntity.isEmailWithAutoDetect: Boolean
  get() = emailAuth && serverAutoDetect
