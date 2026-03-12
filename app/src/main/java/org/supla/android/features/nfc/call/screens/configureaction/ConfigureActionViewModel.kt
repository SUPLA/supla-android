package org.supla.android.features.nfc.call.screens.configureaction
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

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.features.nfc.shared.edit.BaseEditNfcTagViewModel
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewEvent
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ReadAllChannelsWithChildrenUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class ConfigureActionViewModel @Inject constructor(
  readAllChannelsWithChildrenUseCase: ReadAllChannelsWithChildrenUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  readAllProfilesUseCase: ReadAllProfilesUseCase,
  channelGroupRepository: ChannelGroupRepository,
  sceneRepository: RoomSceneRepository,
  nfcTagRepository: NfcTagRepository,
  schedulers: SuplaSchedulers,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase
) : BaseEditNfcTagViewModel(
  readAllChannelsWithChildrenUseCase,
  getChannelValueStringUseCase,
  readAllProfilesUseCase,
  channelGroupRepository,
  sceneRepository,
  nfcTagRepository,
  schedulers,
  getChannelIconUseCase,
  getSceneIconUseCase,
  getCaptionUseCase
),
  ConfigureActionScreenScope {
  override fun onClose() {
    sendEvent(EditNfcTagViewEvent.Close)
  }
}
