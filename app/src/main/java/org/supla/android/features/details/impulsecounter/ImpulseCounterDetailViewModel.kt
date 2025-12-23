package org.supla.android.features.details.impulsecounter
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

import androidx.work.ExistingWorkPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.shared.shareable
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.BaseDetailViewEvent
import org.supla.android.features.details.detailbase.base.BaseDetailViewModel
import org.supla.android.features.details.detailbase.base.BaseDetailViewState
import org.supla.android.features.details.impulsecounter.counterphoto.DownloadPhotoWorker
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.CheckOcrPhotoExistsUseCase
import javax.inject.Inject

@HiltViewModel
class ImpulseCounterDetailViewModel @Inject constructor(
  private val getCaptionUseCase: GetCaptionUseCase,
  private val workManagerProxy: WorkManagerProxy,
  private val checkOcrPhotoExistsUseCase: CheckOcrPhotoExistsUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  updateEventsManager: UpdateEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseDetailViewModel<ImpulseCounterDetailViewState, ImpulseCounterDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  updateEventsManager,
  preferences,
  ImpulseCounterDetailViewState(),
  schedulers
) {
  override fun closeEvent() = ImpulseCounterDetailViewEvent.Close

  override fun updatedState(state: ImpulseCounterDetailViewState, channelDataBase: ChannelDataBase) =
    state.copy(caption = getCaptionUseCase(channelDataBase.shareable))

  override fun handleChannelBase(channelDataBase: ChannelDataBase, initialFunction: SuplaFunction) {
    super.handleChannelBase(channelDataBase, initialFunction)

    if (SuplaChannelFlag.OCR notInside channelDataBase.flags) {
      updateState {
        val hasPhoto = checkOcrPhotoExistsUseCase(channelDataBase.profileId, channelDataBase.remoteId)

        if (it.photoDownloaded) {
          it.copy(hasPhoto = hasPhoto)
        } else {
          workManagerProxy.enqueueUniqueWork(
            "${DownloadPhotoWorker.WORK_ID}.${channelDataBase.remoteId}",
            ExistingWorkPolicy.KEEP,
            DownloadPhotoWorker.build(channelDataBase.remoteId, channelDataBase.profileId)
          )

          it.copy(photoDownloaded = true, hasPhoto = hasPhoto)
        }
      }
    }
  }
}

sealed interface ImpulseCounterDetailViewEvent : BaseDetailViewEvent {
  data object Close : ImpulseCounterDetailViewEvent
}

data class ImpulseCounterDetailViewState(
  override val caption: LocalizedString? = null,
  val photoDownloaded: Boolean = false,
  val hasPhoto: Boolean = false
) : BaseDetailViewState(caption)
