package org.supla.android.features.androidauto
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.local.entity.complex.AndroidAutoDataEntity
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import javax.inject.Inject

@HiltViewModel
class AndroidAutoItemsViewModel @Inject constructor(
  private val androidAutoItemRepository: AndroidAutoItemRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseViewModel<AndroidAutoItemsViewModelState, AndroidAutoItemsViewEvent>(AndroidAutoItemsViewModelState(), schedulers),
  AndroidAutoItemsViewScope {

  override fun onViewCreated() {
    updateState { it.copy(viewState = it.viewState.copy(playMessages = preferences.playAndroidAuto())) }
    androidAutoItemRepository.findAll()
      .attach()
      .subscribeBy(
        onNext = this::handleItems,
        onError = defaultErrorHandler("onViewCreated")
      )
      .disposeBySelf()
  }

  override fun onItemClick(item: AndroidAutoItem) {
    sendEvent(AndroidAutoItemsViewEvent.EditItem(item.id))
  }

  override fun onPlayMessagesChange(value: Boolean) {
    updateState { it.copy(viewState = it.viewState.copy(playMessages = value)) }
    preferences.setPlayAndroidAuto(value)
  }

  override fun onAddClick() {
    sendEvent(AndroidAutoItemsViewEvent.AddItem)
  }

  override fun onMove(from: Int, to: Int) {
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          items = it.viewState.items.toMutableList().apply {
            add(to, removeAt(from))
          }
        )
      )
    }
  }

  override fun onMoveFinished() {
    androidAutoItemRepository.setItemsOrder(currentState().viewState.items.map { it.id })
      .attach()
      .subscribeBy(onError = defaultErrorHandler("onMoveFinished"))
      .disposeBySelf()
  }

  private fun handleItems(items: List<AndroidAutoDataEntity>) {
    updateState { state ->
      state.copy(viewState = state.viewState.copy(items = items.map { it.toItem() }))
    }
  }

  private fun AndroidAutoDataEntity.toItem(): AndroidAutoItem =
    AndroidAutoItem(
      id = androidAutoItemEntity.id,
      subjectId = androidAutoItemEntity.subjectId,
      subjectType = androidAutoItemEntity.subjectType,
      action = androidAutoItemEntity.action,
      icon = icon,
      caption = androidAutoItemEntity.caption,
      profileName = profileEntity.name
    )

  private val AndroidAutoDataEntity.icon: ImageId
    get() = when (androidAutoItemEntity.subjectType) {
      SubjectType.GROUP -> getChannelIconUseCase.forState(groupEntity!!, groupEntity.offlineState)
      SubjectType.SCENE -> getSceneIconUseCase(sceneEntity!!)
      SubjectType.CHANNEL -> getChannelIconUseCase.forState(channelEntity!!, channelEntity.offlineState)
    }
}

sealed class AndroidAutoItemsViewEvent : ViewEvent {
  data object AddItem : AndroidAutoItemsViewEvent()
  data class EditItem(val itemId: Long) : AndroidAutoItemsViewEvent()
}

data class AndroidAutoItemsViewModelState(
  val viewState: AndroidAutoItemsViewState = AndroidAutoItemsViewState()
) : ViewState()
