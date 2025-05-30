package org.supla.android.core.networking.suplaclient
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
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.db.room.app.AppDatabase
import org.supla.android.db.room.measurements.MeasurementsDatabase
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.events.OnlineEventsManager
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.channelscleanup.RemoveHiddenChannelsManager
import org.supla.android.features.scenescleanup.RemoveHiddenScenesManager
import org.supla.android.lib.SuplaClient
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.channel.ChannelToRootRelationHolderUseCase
import org.supla.android.usecases.channel.UpdateChannelExtendedValueUseCase
import org.supla.android.usecases.channel.UpdateChannelUseCase
import org.supla.android.usecases.channel.UpdateChannelValueUseCase
import org.supla.android.usecases.channelconfig.InsertChannelConfigUseCase
import org.supla.android.usecases.channelrelation.DeleteRemovableChannelRelationsUseCase
import org.supla.android.usecases.channelrelation.InsertChannelRelationForProfileUseCase
import org.supla.android.usecases.channelrelation.MarkChannelRelationsAsRemovableUseCase
import org.supla.android.usecases.channelstate.UpdateChannelStateUseCase
import org.supla.android.usecases.group.UpdateChannelGroupTotalValueUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuplaClientBuilder @Inject constructor(private val dependencies: SuplaClientDependencies) {

  fun build(context: Context, oneTimePassword: String?): SuplaClient =
    SuplaClient(context, oneTimePassword, dependencies)
}

@Singleton
data class SuplaClientDependencies @Inject constructor(
  val profileManager: ProfileManager,
  val updateEventsManager: UpdateEventsManager,
  val channelConfigEventsManager: ChannelConfigEventsManager,
  val deviceConfigEventsManager: DeviceConfigEventsManager,
  val encryptedPreferences: EncryptedPreferences,
  val suplaCloudConfigHolder: SuplaCloudConfigHolder,

  val markChannelRelationsAsRemovableUseCase: MarkChannelRelationsAsRemovableUseCase,
  val insertChannelRelationForProfileUseCase: InsertChannelRelationForProfileUseCase,
  val deleteRemovableChannelRelationsUseCase: DeleteRemovableChannelRelationsUseCase,

  val insertChannelConfigUseCase: InsertChannelConfigUseCase,

  val updateChannelUseCase: UpdateChannelUseCase,
  val updateChannelValueUseCase: UpdateChannelValueUseCase,
  val updateChannelExtendedValueUseCase: UpdateChannelExtendedValueUseCase,
  val updateChannelStateUseCase: UpdateChannelStateUseCase,

  val appDatabase: AppDatabase,
  val measurementsDatabase: MeasurementsDatabase,
  val profileIdHolder: ProfileIdHolder,

  val updateChannelGroupTotalValueUseCase: UpdateChannelGroupTotalValueUseCase,
  val channelToRootRelationHolderUseCase: ChannelToRootRelationHolderUseCase,

  val suplaClientStateHolder: SuplaClientStateHolder,
  val removeHiddenChannelsManager: RemoveHiddenChannelsManager,
  val removeHiddenScenesManager: RemoveHiddenScenesManager,
  val onlineEventsManager: OnlineEventsManager
)
