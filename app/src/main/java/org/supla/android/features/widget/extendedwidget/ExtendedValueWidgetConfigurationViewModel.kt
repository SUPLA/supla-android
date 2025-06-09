package org.supla.android.features.widget.extendedwidget
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
import androidx.glance.GlanceId
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.data.model.general.ChannelBase
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.data.model.spinner.SubjectItemConversionScope
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.dao.WidgetConfigurationDao
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity
import org.supla.android.extensions.guardLet
import org.supla.android.features.widget.shared.BaseWidgetViewModel
import org.supla.android.features.widget.shared.WidgetConfigurationScope
import org.supla.android.features.widget.shared.WidgetConfigurationViewEvent
import org.supla.android.features.widget.shared.WidgetConfigurationViewModelState
import org.supla.android.features.widget.shared.subjectdetail.SubjectDetail
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class ExtendedValueWidgetConfigurationViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val widgetConfigurationDao: WidgetConfigurationDao,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  channelGroupRepository: ChannelGroupRepository,
  channelRepository: RoomChannelRepository,
  sceneRepository: RoomSceneRepository,
  powerManager: PowerManager,
  schedulers: SuplaSchedulers
) : BaseWidgetViewModel(
  getChannelIconUseCase,
  getSceneIconUseCase,
  getCaptionUseCase,
  channelGroupRepository,
  channelRepository,
  sceneRepository,
  powerManager,
  context,
  schedulers
),
  WidgetConfigurationScope,
  SubjectItemConversionScope {

  private var glanceId: GlanceId? = null

  fun onViewCreated(glanceId: GlanceId) {
    this.glanceId = glanceId
    widgetConfigurationDao.findEntityBy(glanceId.toString())
      .attach()
      .subscribeBy(
        onSuccess = { loadForEdit(it) },
        onError = { loadForAdd() }
      )
      .disposeBySelf()
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
                subjects = subjects.asSingleSelectionList(SubjectType.CHANNEL),
                caption = null
              )
            )
          }
        }
      )
      .disposeBySelf()
  }

  private fun loadForEdit(configuration: WidgetConfigurationEntity) {
    readAllProfilesUseCase()
      .flatMapSingle { profiles -> getSubjectsSource(configuration.profileId, configuration.subjectType).map { Pair(profiles, it) } }
      .attach()
      .subscribeBy(
        onNext = { (profiles, subjects) ->
          updateState { state ->
            state.copy(
              entityId = configuration.id,
              viewState = state.viewState.copy(
                profiles = profiles.asSingleSelectionList(configuration.profileId),
                subjects = subjects.asSingleSelectionList(configuration.subjectType, configuration.subjectId),
                subjectType = configuration.subjectType,
                caption = configuration.caption,
                saveEnabled = true
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
    val (glanceId) = guardLet(glanceId) { return }
    val (widgetId) = guardLet(currentState().widgetId) { return }
    val (configuration) = guardLet(currentState().configuration(glanceId)) { return }

    widgetConfigurationDao.insert(configuration)
      .attach()
      .subscribeBy(
        onComplete = {
          sendEvent(WidgetConfigurationViewEvent.Finished(widgetId))
        },
        onError = {
          updateState {
            it.copy(viewState = it.viewState.copy(error = LocalizedString.WithResource(R.string.widget_configure_error)))
          }
        }
      )
      .disposeBySelf()
  }

  override fun filter(channelBase: ChannelBase): Boolean =
    channelBase.function == SuplaFunction.ELECTRICITY_METER
}

fun WidgetConfigurationViewModelState.configuration(glanceId: GlanceId): WidgetConfigurationEntity? {
  val (profileId) = guardLet(viewState.profiles?.selected?.id) { return null }
  val (subjectId) = guardLet(viewState.subjects?.selected?.id) { return null }

  return WidgetConfigurationEntity(
    id = entityId ?: 0,
    subjectId = subjectId,
    subjectType = viewState.subjectType,
    caption = viewState.caption ?: "",
    action = ActionId.NONE,
    profileId = profileId,
    glanceId = glanceId.toString()
  )
}
