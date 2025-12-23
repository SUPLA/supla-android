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
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelStateRepository
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.ColorListRepository
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HomePlusThermostatLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.PowerActiveLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.VoltageLogRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaClient
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.widget.WidgetManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteProfileUseCase @Inject constructor(
  @ApplicationContext private val context: Context,
  private val profileRepository: RoomProfileRepository,
  private val suplaAppProvider: SuplaAppProvider,
  private val profileIdHolder: ProfileIdHolder,
  private val activateProfileUseCase: ActivateProfileUseCase,
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val disconnectUseCase: DisconnectUseCase,
  private val singleCallProvider: SingleCall.Provider,
  private val widgetManager: WidgetManager,

  // Connected repositories
  androidAutoItemRepository: AndroidAutoItemRepository,
  channelRepository: RoomChannelRepository,
  channelConfigRepository: ChannelConfigRepository,
  channelExtendedValueRepository: ChannelExtendedValueRepository,
  channelRelationRepository: ChannelRelationRepository,
  channelStateRepository: ChannelStateRepository,
  channelValueRepository: ChannelValueRepository,
  channelGroupRepository: ChannelGroupRepository,
  channelGroupRelationRepository: ChannelGroupRelationRepository,
  colorListRepository: ColorListRepository,
  locationRepository: LocationRepository,
  sceneRepository: RoomSceneRepository,
  userIconRepository: RoomUserIconRepository,
  currentLogRepository: CurrentLogRepository,
  electricityMeterLogRepository: ElectricityMeterLogRepository,
  generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  humidityLogRepository: HumidityLogRepository,
  impulseCounterLogRepository: ImpulseCounterLogRepository,
  powerActiveLogRepository: PowerActiveLogRepository,
  temperatureLogRepository: TemperatureLogRepository,
  temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  homePlusThermostatLogRepository: HomePlusThermostatLogRepository,
  voltageLogRepository: VoltageLogRepository
) {

  private val profileDependencyRemovers: List<ProfileRemover> = listOf(
    androidAutoItemRepository,
    channelRepository,
    channelConfigRepository,
    channelExtendedValueRepository,
    channelRelationRepository,
    channelStateRepository,
    channelValueRepository,
    channelGroupRepository,
    channelGroupRelationRepository,
    colorListRepository,
    locationRepository,
    sceneRepository,
    userIconRepository,
    currentLogRepository,
    electricityMeterLogRepository,
    generalPurposeMeterLogRepository,
    generalPurposeMeasurementLogRepository,
    humidityLogRepository,
    impulseCounterLogRepository,
    powerActiveLogRepository,
    temperatureLogRepository,
    temperatureAndHumidityLogRepository,
    homePlusThermostatLogRepository,
    voltageLogRepository
  )

  operator fun invoke(profileId: Long): Completable =
    profileRepository.findProfile(profileId)
      .flatMapCompletable(this::removeProfile)

  private fun removeProfile(profile: ProfileEntity): Completable {
    return if (profile.active != true) {
      deleteProfile(profile)
    } else {
      removeActiveProfile(profile)
    }
  }

  private fun removeActiveProfile(profile: ProfileEntity): Completable =
    disconnectUseCase()
      .andThen(profileRepository.findAllProfiles().firstOrError())
      .map { profiles -> profiles.filter { it.active != true } }
      .flatMapCompletable { profiles ->
        return@flatMapCompletable if (profiles.isEmpty()) {
          removeLastProfile(profile)
        } else {
          removeAndActivate(
            toRemove = profile,
            toActivate = profiles.first()
          ).andThen(Completable.fromRunnable { startClient() })
        }
      }

  private fun removeLastProfile(profile: ProfileEntity): Completable =
    deleteProfile(profile)
      .andThen(
        Completable.fromRunnable {
          profileIdHolder.profileId = null
          suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
        }
      )

  private fun removeAndActivate(toRemove: ProfileEntity, toActivate: ProfileEntity): Completable =
    activateProfileUseCase(toActivate.id!!, true)
      .andThen(deleteProfile(toRemove))

  private fun startClient() {
    suplaAppProvider.provide().SuplaClientInitIfNeed(context)
  }

  private fun deleteProfile(profileEntity: ProfileEntity): Completable =
    Completable.fromRunnable {
      profileEntity.id?.let { id ->
        try {
          singleCallProvider.provide(id).registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profileEntity)
        } catch (ex: Exception) {
          Timber.w(ex, "Token cleanup failed while profile removal (profile id: `$id`)")
        }
        widgetManager.onProfileRemoved(id)
      }
    }
      .andThen(profileRepository.deleteProfile(profileEntity))
      .let { completable ->
        profileEntity.id?.let { id ->
          completable.andThen(Completable.merge(profileDependencyRemovers.map { it.deleteByProfile(id) }))
        } ?: completable
      }

  interface ProfileRemover {
    fun deleteByProfile(profileId: Long): Completable
  }
}
