package org.supla.android.features.widget.shared
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
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelBase
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.data.model.spinner.SubjectItemConversionScope
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.isGpm
import org.supla.android.data.source.local.entity.isThermometer
import org.supla.android.features.widget.shared.subjectdetail.SubjectDetail
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.util.Objects

abstract class BaseWidgetViewModel(
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  private val channelGroupRepository: ChannelGroupRepository,
  private val channelRepository: RoomChannelRepository,
  private val sceneRepository: RoomSceneRepository,
  private val powerManager: PowerManager,
  @ApplicationContext private val context: Context,
  schedulers: SuplaSchedulers
) : BaseViewModel<WidgetConfigurationViewModelState, WidgetConfigurationViewEvent>(
  WidgetConfigurationViewModelState(),
  schedulers
),
  SubjectItemConversionScope {

  fun setWidgetId(widgetId: Int?) {
    updateState { it.copy(widgetId = widgetId) }
  }

  fun onWarningClick() {
    sendEvent(WidgetConfigurationViewEvent.OpenSettings)
  }

  fun onClose() {
    sendEvent(WidgetConfigurationViewEvent.Close)
  }

  fun onCaptionChange(caption: String) {
    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          caption = caption,
          saveEnabled = caption.isNotEmpty()
        ),
        selections = state.updateSelections(caption)
      )
    }
  }

  override fun onStart() {
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          showWarning = !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        )
      )
    }
  }

  protected fun getSubjectsSource(profileId: Long, subjectType: SubjectType): Single<List<SubjectItem>> =
    when (subjectType) {
      SubjectType.CHANNEL ->
        channelRepository.findProfileChannels(profileId)
          .map { channels -> channelsSubjectItems(channels.filter { filter(it) }) }

      SubjectType.GROUP ->
        channelGroupRepository.findProfileGroups(profileId)
          .map { groups -> groupsSubjectItems(groups.filter { filter(it) }) }

      SubjectType.SCENE ->
        sceneRepository.findProfileScenes(profileId)
          .map { scenes -> scenesSubjectItems(scenes) }
    }

  protected fun filter(channelBase: ChannelBase) =
    channelBase.function.actions.isNotEmpty() ||
      channelBase.isThermometer() || channelBase.isGpm()

  protected fun needsReload(function: SuplaFunction?): Boolean =
    when (function) {
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT -> true

      else -> false
    }
}

sealed class WidgetConfigurationViewEvent : ViewEvent {
  data object Close : WidgetConfigurationViewEvent()
  data object OpenSettings : WidgetConfigurationViewEvent()
  data class Finished(val widgetId: Int) : WidgetConfigurationViewEvent()
}

data class WidgetConfigurationViewModelState(
  val widgetId: Int? = null,
  val viewState: WidgetConfigurationViewState = WidgetConfigurationViewState(),
  val selections: Set<Selection> = emptySet(),
) : ViewState() {

  fun updateSelections(detail: SubjectDetail): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id
      val caption = viewState.caption

      if (profileId != null && subjectId != null && caption != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, caption, detail))
      }
    }

  fun updateSelections(caption: String): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id
      val detail = viewState.subjectDetails?.selected

      if (profileId != null && subjectId != null && detail != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, caption, detail))
      }
    }

  fun lastSubjectType(profileId: Long?): SubjectType? =
    selections.lastOrNull { it.profileId == profileId }?.subjectType

  fun lastSubjectId(profileId: Long?, subjectType: SubjectType): Int? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType }?.subjectId

  fun lastCaption(profileId: Long?, subjectType: SubjectType, subjectId: Int?): String? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.caption

  fun lastDetail(profileId: Long?, subjectType: SubjectType, subjectId: Int?): SubjectDetail? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.subjectDetail

  private fun MutableSet<Selection>.addOrReplace(selection: Selection) {
    if (contains(selection)) {
      remove(selection)
    }

    add(selection)
  }
}

data class Selection(
  val profileId: Long,
  val subjectType: SubjectType,
  val subjectId: Int,
  val caption: String,
  val subjectDetail: SubjectDetail
) {
  override fun hashCode(): Int {
    return Objects.hash(profileId, subjectType, subjectId)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Selection

    if (profileId != other.profileId) return false
    if (subjectId != other.subjectId) return false
    if (subjectType != other.subjectType) return false

    return true
  }
}
