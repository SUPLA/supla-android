package org.supla.android.usecases.location
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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.db.Location
import org.supla.android.lib.SuplaLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateLocationUseCase @Inject constructor(
  private val locationRepository: LocationRepository,
  private val profileRepository: ProfileRepository
) {

  operator fun invoke(suplaLocation: SuplaLocation): Boolean =
    runBlocking {
      withContext(Dispatchers.IO) {
        updateLocation(suplaLocation)
      }
    }

  private suspend fun updateLocation(suplaLocation: SuplaLocation): Boolean {
    val location = locationRepository.findByRemoteId(suplaLocation.Id).awaitSingleOrNull()
      ?: return insertLocation(suplaLocation)

    if (!location.isSameAs(suplaLocation)) {
      locationRepository.updateLocation(
        location.copy(
          remoteId = suplaLocation.Id,
          caption = suplaLocation.Caption,
          visible = 1
        )
      ).await()
      return true
    }

    return false
  }

  private suspend fun insertLocation(suplaLocation: SuplaLocation): Boolean {
    val profile = profileRepository.findActiveProfileKtx() ?: return false

    locationRepository.insert(
      LocationEntity(
        id = null,
        remoteId = suplaLocation.Id,
        caption = suplaLocation.Caption,
        visible = 1,
        collapsed = 0,
        sorting = Location.SortingType.DEFAULT,
        sortOrder = 0,
        profileId = profile.id!!
      )
    )

    return true
  }

  private fun LocationEntity.isSameAs(suplaLocation: SuplaLocation): Boolean =
    remoteId == suplaLocation.Id && caption == suplaLocation.Caption
}
