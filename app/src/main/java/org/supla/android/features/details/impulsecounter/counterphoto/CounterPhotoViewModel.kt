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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.infrastructure.UriProxy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
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
  private val uriProxy: UriProxy,
  @ApplicationContext private val context: Context,
  schedulers: SuplaSchedulers
) : BaseViewModel<CounterPhotoViewModelState, CounterPhotoViewEvent>(CounterPhotoViewModelState(), schedulers) {

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

  private fun handleChannel(data: Pair<ChannelDataEntity, CloudUrl>) {
    val channel = data.first
    updateState {
      val ocrDirectory = File(context.cacheDir, ocrImageNamingProvider.directory)
      val ocrImage = File(ocrDirectory, ocrImageNamingProvider.imageName(channel.profileId, channel.remoteId))
      val ocrImageCropped = File(ocrDirectory, ocrImageNamingProvider.imageCroppedName(channel.profileId, channel.remoteId))

      it.copy(
        viewState = it.viewState.copy(
          imageFile = ocrImage,
          croppedImageFile = ocrImageCropped
        ),
        configurationAddress = "${data.second.urlString}/channels/${channel.remoteId}/ocr-settings"
      )
    }
  }
}

sealed class CounterPhotoViewEvent : ViewEvent {
  data class OpenUrl(val url: Uri) : CounterPhotoViewEvent()
}

data class CounterPhotoViewModelState(
  val viewState: CounterPhotoViewState = CounterPhotoViewState(),
  val configurationAddress: String? = null
) : ViewState()
