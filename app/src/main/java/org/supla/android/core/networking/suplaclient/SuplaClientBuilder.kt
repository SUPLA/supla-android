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
import org.supla.android.events.ConfigEventsManager
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.SuplaClient
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.channelrelation.DeleteRemovableChannelRelationsUseCase
import org.supla.android.usecases.channelrelation.InsertChannelRelationForProfileUseCase
import org.supla.android.usecases.channelrelation.MarkChannelRelationsAsRemovableUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuplaClientBuilder @Inject constructor(
  private val profileManager: ProfileManager,
  private val updateEventsManager: UpdateEventsManager,
  private val configEventsManager: ConfigEventsManager,
  private val encryptedPreferences: EncryptedPreferences,
  private val markChannelRelationsAsRemovableUseCase: MarkChannelRelationsAsRemovableUseCase,
  private val insertChannelRelationForProfileUseCase: InsertChannelRelationForProfileUseCase,
  private val deleteRemovableChannelRelationsUseCase: DeleteRemovableChannelRelationsUseCase,
  private val suplaCloudConfigHolder: SuplaCloudConfigHolder
) {

  fun build(context: Context, oneTimePassword: String?): SuplaClient =
    SuplaClient(
      context,
      oneTimePassword,
      profileManager,
      updateEventsManager,
      configEventsManager,
      encryptedPreferences,
      markChannelRelationsAsRemovableUseCase,
      insertChannelRelationForProfileUseCase,
      deleteRemovableChannelRelationsUseCase,
      suplaCloudConfigHolder
    )
}
