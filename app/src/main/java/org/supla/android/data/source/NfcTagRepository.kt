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

import org.supla.android.data.source.local.dao.NfcTagDao
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.data.source.local.entity.complex.NfcTagDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcTagRepository @Inject constructor(
  private val nfcTagDao: NfcTagDao
) {
  suspend fun save(entity: NfcTagEntity): Long = nfcTagDao.save(entity)

  suspend fun findAll(): List<NfcTagEntity> = nfcTagDao.findAll()

  suspend fun findAllWithDependencies(): List<NfcTagDataEntity> = nfcTagDao.findAllWithDependencies()

  suspend fun findById(id: Long): NfcTagEntity? = nfcTagDao.findById(id)

  suspend fun findByUuid(uuid: String): NfcTagEntity? = nfcTagDao.findByUuid(uuid)

  suspend fun delete(id: Long) = nfcTagDao.delete(id)
}
