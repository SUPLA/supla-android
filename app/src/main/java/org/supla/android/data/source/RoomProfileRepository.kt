package org.supla.android.data.source
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

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.dao.ProfileDao
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.usecases.developerinfo.CountProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomProfileRepository @Inject constructor(private val profileDao: ProfileDao) : CountProvider {

  fun findActiveProfile() = profileDao.findActiveProfile()

  suspend fun findActiveProfileKtx() = profileDao.findActiveProfileKtx()

  fun findAllProfiles() = profileDao.findAllProfiles()

  suspend fun findAllProfilesKtx() = profileDao.findAllProfilesKtx()

  fun findProfile(id: Long) = profileDao.findProfile(id)

  fun activateProfile(id: Long) = profileDao.activateProfile(id)

  fun deleteProfile(profileEntity: ProfileEntity) = profileDao.delete(profileEntity)

  fun setItemsOrder(orderedIds: List<Long>) = profileDao.setItemsOrder(orderedIds)

  override fun count(): Observable<Int> = profileDao.count()
}
