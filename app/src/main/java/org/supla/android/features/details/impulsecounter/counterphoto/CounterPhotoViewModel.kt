package org.supla.android.features.details.impulsecounter.counterphoto
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
import android.net.Uri
import androidx.work.ExistingWorkPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.infrastructure.UriProxy
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.core.shared.usecase.channel.ocr.OcrImageNamingProvider
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CounterPhotoViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase,
  private val ocrImageNamingProvider: OcrImageNamingProvider,
  private val updateEventsManager: UpdateEventsManager,
  private val workManagerProxy: WorkManagerProxy,
  private val userStateHolder: UserStateHolder,
  private val valuesFormatter: ValuesFormatter,
  private val uriProxy: UriProxy,
  @ApplicationContext private val context: Context,
  schedulers: SuplaSchedulers
) : BaseViewModel<CounterPhotoViewModelState, CounterPhotoViewEvent>(CounterPhotoViewModelState(), schedulers) {

  fun observeUpdates(remoteId: Int) {
    updateEventsManager.observeChannel(remoteId)
      .flatMapMaybe { readChannelByRemoteIdUseCase(remoteId) }
      .attach()
      .subscribeBy(
        onNext = { handleChannel(Pair(it, null)) },
        onError = defaultErrorHandler("observeUpdates")
      )
      .disposeBySelf()
  }

  fun loadData(remoteId: Int) {
    Maybe.zip(
      readChannelByRemoteIdUseCase(remoteId),
      loadActiveProfileUrlUseCase().toMaybe()
    ) { channel, profileUrl -> Pair(channel, profileUrl) }
      .attach()
      .subscribeBy(
        onSuccess = this::handleChannel,
        onError = defaultErrorHandler("loadData")
      )
      .disposeBySelf()
  }

  fun onCloudClick() {
    currentState().configurationAddress?.let {
      sendEvent(CounterPhotoViewEvent.OpenUrl(uriProxy.toUri(it)))
    }
  }

  fun onRefresh() {
    updateState {
      if (it.remoteId != null && it.profileId != null) {
        workManagerProxy.enqueueUniqueWork(
          "${DownloadPhotoWorker.WORK_ID}.${it.remoteId}",
          ExistingWorkPolicy.KEEP,
          DownloadPhotoWorker.build(it.remoteId, it.profileId)
        )
      }

      it.copy(viewState = it.viewState.copy(refreshing = true))
    }
  }

  private fun handleChannel(data: Pair<ChannelDataEntity, CloudUrl?>) {
    val channel = data.first
    updateState { state ->
      val ocrDirectory = File(context.cacheDir, ocrImageNamingProvider.directory)
      val ocrImage = File(ocrDirectory, ocrImageNamingProvider.imageName(channel.profileId, channel.remoteId))
      val ocrImageCropped = File(ocrDirectory, ocrImageNamingProvider.imageCroppedName(channel.profileId, channel.remoteId))
      val configurationAddress =
        data.second?.let { "${it.urlString}channels/${channel.remoteId}/ocr-settings" } ?: state.configurationAddress

      state.copy(
        viewState = state.viewState.copy(
          imageFile = ocrImage,
          croppedImageFile = ocrImageCropped,
          refreshing = false,
          date = userStateHolder.getOcrPhotoCreationTime(channel.profileId, channel.remoteId)?.let { valuesFormatter.getFullDateString(it) }
        ),
        configurationAddress = configurationAddress,
        remoteId = channel.remoteId,
        profileId = channel.profileId
      )
    }
  }
}

sealed class CounterPhotoViewEvent : ViewEvent {
  data class OpenUrl(val url: Uri) : CounterPhotoViewEvent()
}

data class CounterPhotoViewModelState(
  val viewState: CounterPhotoViewState = CounterPhotoViewState(),
  val configurationAddress: String? = null,
  val remoteId: Int? = null,
  val profileId: Long? = null
) : ViewState()
