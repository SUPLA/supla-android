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

import com.google.gson.Gson
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.dao.ChannelConfigDao
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.container.SuplaChannelContainerConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeasurementConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ChannelConfigRepository @Inject constructor(
  private val channelConfigDao: ChannelConfigDao,
  @Named(GSON_FOR_REPO) private val gson: Gson
) : CountProvider, RemoveHiddenChannelsUseCase.ChannelsDeletable {

  fun findForRemoteId(remoteId: Int) = channelConfigDao.findForRemoteId(remoteId)

  fun insertOrUpdate(profileId: Long, config: SuplaChannelGeneralPurposeMeasurementConfig): Completable {
    return channelConfigDao.insertOrUpdate(config.toEntity(profileId, gson))
  }

  fun insertOrUpdate(profileId: Long, config: SuplaChannelGeneralPurposeMeterConfig): Completable {
    return channelConfigDao.insertOrUpdate(config.toEntity(profileId, gson))
  }

  fun insertOrUpdate(profileId: Long, config: SuplaChannelFacadeBlindConfig): Completable {
    return channelConfigDao.insertOrUpdate(config.toEntity(profileId, gson))
  }

  fun insertOrUpdate(profileId: Long, config: SuplaChannelContainerConfig): Completable {
    return channelConfigDao.insertOrUpdate(config.toEntity(profileId, gson))
  }

  fun delete(profileId: Long, channelId: Int): Completable {
    return channelConfigDao.delete(profileId, channelId)
  }

  fun findChannelConfig(profileId: Long, channelId: Int, type: ChannelConfigType): Single<SuplaChannelConfig> {
    return channelConfigDao.read(profileId, channelId, type)
      .map {
        when (type) {
          ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT ->
            gson.fromJson(it.config, SuplaChannelGeneralPurposeMeasurementConfig::class.java)

          ChannelConfigType.GENERAL_PURPOSE_METER ->
            gson.fromJson(it.config, SuplaChannelGeneralPurposeMeterConfig::class.java)

          ChannelConfigType.FACADE_BLIND ->
            gson.fromJson(it.config, SuplaChannelFacadeBlindConfig::class.java)

          ChannelConfigType.CONTAINER ->
            gson.fromJson(it.config, SuplaChannelContainerConfig::class.java)

          else ->
            gson.fromJson(it.config, SuplaChannelConfig::class.java)
        }
      }
  }

  override fun count(): Observable<Int> = channelConfigDao.count()

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = channelConfigDao.deleteKtx(remoteId, profileId)
}

private fun SuplaChannelGeneralPurposeMeasurementConfig.toEntity(profileId: Long, gson: Gson): ChannelConfigEntity {
  return ChannelConfigEntity(
    channelId = remoteId,
    profileId = profileId,
    config = gson.toJson(this),
    configType = ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT,
    configCrc32 = crc32
  )
}

private fun SuplaChannelGeneralPurposeMeterConfig.toEntity(profileId: Long, gson: Gson): ChannelConfigEntity {
  return ChannelConfigEntity(
    channelId = remoteId,
    profileId = profileId,
    config = gson.toJson(this),
    configType = ChannelConfigType.GENERAL_PURPOSE_METER,
    configCrc32 = crc32
  )
}

private fun SuplaChannelFacadeBlindConfig.toEntity(profileId: Long, gson: Gson): ChannelConfigEntity {
  return ChannelConfigEntity(
    channelId = remoteId,
    profileId = profileId,
    config = gson.toJson(this),
    configType = ChannelConfigType.FACADE_BLIND,
    configCrc32 = crc32
  )
}

private fun SuplaChannelContainerConfig.toEntity(profileId: Long, gson: Gson): ChannelConfigEntity =
  ChannelConfigEntity(
    channelId = remoteId,
    profileId = profileId,
    config = gson.toJson(this),
    configType = ChannelConfigType.CONTAINER,
    configCrc32 = crc32
  )
