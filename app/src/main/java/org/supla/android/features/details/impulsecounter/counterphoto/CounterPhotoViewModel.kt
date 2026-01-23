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

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import org.supla.android.core.infrastructure.UriProxy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.ocr.LoadLatestOcrPhotoUseCase
import org.supla.android.usecases.ocr.LoadOcrPhotosUseCase
import org.supla.android.usecases.ocr.OcrPhoto
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import javax.inject.Inject

@HiltViewModel
class CounterPhotoViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase,
  private val loadLatestOcrPhotoUseCase: LoadLatestOcrPhotoUseCase,
  private val loadOcrPhotosUseCase: LoadOcrPhotosUseCase,
  private val uriProxy: UriProxy,
  schedulers: SuplaSchedulers
) : BaseViewModel<CounterPhotoViewModelState, CounterPhotoViewEvent>(CounterPhotoViewModelState(), schedulers) {

  fun loadData(remoteId: Int) {
    updateState { it.copy(viewState = it.viewState.copy(refreshing = true)) }

    Maybe.zip(
      readChannelByRemoteIdUseCase(remoteId),
      loadActiveProfileUrlUseCase().toMaybe(),
      loadLatestOcrPhotoUseCase(remoteId).firstElement(),
      loadOcrPhotosUseCase(remoteId).firstElement()
    ) { channel, profileUrl, latestPhoto, photos -> Data(channel, profileUrl, latestPhoto, photos) }
      .attachSilent()
      .subscribeBy(
        onSuccess = this::handleChannel,
        onError = { error ->
          defaultErrorHandler("loadData")(error)
          updateState { it.copy(viewState = it.viewState.copy(loadingError = true)) }
        }
      )
      .disposeBySelf()
  }

  fun onCloudClick() {
    currentState().configurationAddress?.let {
      sendEvent(CounterPhotoViewEvent.OpenUrl(uriProxy.toUri(it)))
    }
  }

  fun onRefresh() {
    currentState().remoteId?.let { loadData(it) }
  }

  private fun handleChannel(data: Data) {
    val channel = data.channel
    val url = data.url

    updateState { state ->
      val configurationAddress = url.let { "${it.urlString}/channels/${channel.remoteId}/ocr-settings" }

      state.copy(
        viewState = state.viewState.copy(
          latestPhoto = data.latestPhoto,
          refreshing = false,
          photos = data.photos,
          loadingError = false
        ),
        configurationAddress = configurationAddress,
        remoteId = channel.remoteId
      )
    }
  }

  private data class Data(
    val channel: ChannelDataEntity,
    val url: CloudUrl,
    val latestPhoto: OcrPhoto,
    val photos: List<OcrPhoto>
  )
}

sealed class CounterPhotoViewEvent : ViewEvent {
  data class OpenUrl(val url: Uri) : CounterPhotoViewEvent()
}

data class CounterPhotoViewModelState(
  val viewState: CounterPhotoViewState = CounterPhotoViewState(),
  val configurationAddress: String? = null,
  val remoteId: Int? = null
) : ViewState()
