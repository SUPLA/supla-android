package org.supla.android.features.widget.doublewidget
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.data.model.spinner.SubjectItemConversionScope
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.widget.shared.BaseWidgetViewModel
import org.supla.android.features.widget.shared.WidgetConfigurationScope
import org.supla.android.features.widget.shared.WidgetConfigurationViewEvent
import org.supla.android.features.widget.shared.subjectdetail.SubjectDetail
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import javax.inject.Inject

@HiltViewModel
class DoubleWidgetConfigurationViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  private val widgetPreferences: WidgetPreferences,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  channelGroupRepository: ChannelGroupRepository,
  channelRepository: RoomChannelRepository,
  sceneRepository: RoomSceneRepository,
  powerManager: PowerManager,
  schedulers: SuplaSchedulers
) : BaseWidgetViewModel(
  getChannelIconUseCase,
  getSceneIconUseCase,
  getCaptionUseCase,
  getChannelValueStringUseCase,
  channelGroupRepository,
  channelRepository,
  sceneRepository,
  powerManager,
  context,
  schedulers
),
  WidgetConfigurationScope,
  SubjectItemConversionScope {

  override fun onViewCreated() {
    val configuration = currentState().widgetId?.let { widgetPreferences.getWidgetConfiguration(it) }

    if (configuration != null) {
      loadForEdit(configuration)
    } else {
      loadForAdd()
    }
  }

  private fun loadForAdd() {
    readAllProfilesUseCase()
      .flatMapSingle { profiles ->
        profiles.firstOrNull { it.active == true }?.id?.let { profileId ->
          return@flatMapSingle getSubjectsSource(profileId, SubjectType.CHANNEL).map { Pair(profiles, it) }
        }
        Single.just(Pair(profiles, emptyList()))
      }
      .attach()
      .subscribeBy(
        onNext = { (profiles, subjects) ->
          val activeProfile = profiles.first { it.active == true }
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = profiles.asSingleSelectionList(activeProfile.id),
                subjectTypes = SubjectType.entries.minus(SubjectType.SCENE),
                subjects = subjects.asSingleSelectionList(SubjectType.CHANNEL),
                caption = null
              )
            )
          }
        },
        onError = {
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = null,
                subjects = null,
                caption = null
              )
            )
          }
        }
      )
      .disposeBySelf()
  }

  private fun loadForEdit(configuration: WidgetConfiguration) {
    readAllProfilesUseCase()
      .flatMapSingle { profiles -> getSubjectsSource(configuration.profileId, configuration.subjectType).map { Pair(profiles, it) } }
      .attach()
      .subscribeBy(
        onNext = { (profiles, subjects) ->
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = profiles.asSingleSelectionList(configuration.profileId),
                subjects = subjects.asSingleSelectionList(configuration.subjectType, configuration.itemId),
                subjectTypes = SubjectType.entries.minus(SubjectType.SCENE),
                subjectType = configuration.subjectType,
                caption = configuration.caption,
                saveEnabled = true
              )
            )
          }
        },
        onError = {
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = null,
                subjects = null,
                caption = null
              )
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onProfileSelected(profileItem: ProfileItem) {
    val subjectType = currentState().lastSubjectType(profileItem.id) ?: currentState().viewState.subjectType
    getSubjectsSource(profileItem.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = state.lastSubjectId(profileItem.id, subjectType)
            val lastCaption = state.lastCaption(profileItem.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                profiles = state.viewState.profiles?.copy(selected = profileItem),
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
                caption = lastCaption
              ),
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onSubjectTypeSelected(subjectType: SubjectType) {
    val (profile) = guardLet(currentState().viewState.profiles?.selected) { return }
    getSubjectsSource(profile.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = state.lastSubjectId(profile.id, subjectType)
            val lastCaption = state.lastCaption(profile.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
                caption = lastCaption
              ),
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onSubjectSelected(subjectItem: SubjectItem) {
    updateState { state ->
      val profileId = state.viewState.profiles?.selected?.id
      val lastCaption = state.lastCaption(profileId, state.viewState.subjectType, subjectItem.id)
      state.copy(
        viewState = state.viewState.copy(
          subjects = state.viewState.subjects?.copy(selected = subjectItem),
          caption = lastCaption
        )
      )
    }
  }

  override fun onDetailChange(detail: SubjectDetail) {
    // nothing to do, currently there is no detail for double widget
  }

  override fun onOk() {
    val (widgetId) = guardLet(currentState().widgetId) { return }
    val (profileId) = guardLet(currentState().viewState.profiles?.selected?.id) { return }
    val (subject) = guardLet(currentState().viewState.subjects?.selected) { return }
    val (caption) = guardLet(currentState().viewState.caption) { return }

    val configuration = WidgetConfiguration(
      itemId = subject.id,
      subjectType = currentState().viewState.subjectType,
      caption = caption,
      subjectFunction = subject.function ?: SuplaFunction.NONE,
      value = subject.value ?: NO_VALUE_TEXT,
      profileId = profileId,
      visibility = true,
      actionId = null,
      altIcon = subject.altIcon ?: 0,
      userIcon = subject.userIcon ?: 0
    )

    widgetPreferences.setWidgetConfiguration(widgetId, configuration)
    sendEvent(WidgetConfigurationViewEvent.Finished(widgetId))
  }
}
