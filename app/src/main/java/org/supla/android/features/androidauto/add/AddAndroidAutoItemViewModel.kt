package org.supla.android.features.androidauto.add
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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.extensions.guardLet
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.usecases.extensions.invoke
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class AddAndroidAutoItemViewModel @Inject constructor(
  private val androidAutoItemRepository: AndroidAutoItemRepository,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val channelGroupRepository: ChannelGroupRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val channelRepository: RoomChannelRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val sceneRepository: RoomSceneRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<AddAndroidAutoItemViewModelState, AddAndroidAutoItemViewEvent>(AddAndroidAutoItemViewModelState(), schedulers),
  AddAndroidAutoItemScope {

  fun onViewCreated(itemId: Long?) {
    if (itemId == null) {
      loadForAdd()
    } else {
      loadForEdit(itemId)
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
                subjects = subjects.asSingleSelectionList(SubjectType.CHANNEL),
                caption = null,
                actions = subjects.firstOrNull { it.isLocation.not() }?.actionsList()
              )
            )
          }
        }
      )
      .disposeBySelf()
  }

  private fun loadForEdit(itemId: Long) {
    androidAutoItemRepository.findById(itemId)
      .flatMap { item -> readAllProfilesUseCase().map { profiles -> Pair(item, profiles) } }
      .flatMapSingle { (item, profiles) -> getSubjectsSource(item.profileId, item.subjectType).map { Triple(item, profiles, it) } }
      .attach()
      .subscribeBy(
        onNext = { (item, profiles, subjects) ->
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = profiles.asSingleSelectionList(item.profileId),
                subjects = subjects.asSingleSelectionList(item.subjectType, item.subjectId),
                subjectType = item.subjectType,
                caption = item.caption,
                actions = subjects.firstOrNull { it.id == item.subjectId }?.actionsList(item.action),
                showDelete = true
              ),
              id = itemId
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
            val lastActionId = state.lastActionId(profileItem.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                profiles = state.viewState.profiles?.copy(selected = profileItem),
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
                caption = lastCaption,
                actions = subjects.firstOrNull { it.isLocation.not() }?.actionsList(lastActionId)
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
            val lastActionId = state.lastActionId(profile.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
                caption = lastCaption,
                actions = subjects.firstOrNull { it.isLocation.not() }?.actionsList(lastActionId)
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
      val lastActionId = state.lastActionId(profileId, state.viewState.subjectType, subjectItem.id)
      state.copy(
        viewState = state.viewState.copy(
          subjects = state.viewState.subjects?.copy(selected = subjectItem),
          caption = lastCaption,
          actions = subjectItem.actionsList(lastActionId)
        )
      )
    }
  }

  override fun onCaptionChange(caption: String) {
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

  override fun onActionChange(actionId: ActionId) {
    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          actions = state.viewState.actions?.copy(selected = actionId)
        ),
        selections = state.updateSelections(actionId)
      )
    }
  }

  override fun onSave() {
    val state = currentState()
    val (profileId) = guardLet(state.viewState.profiles?.selected?.id) { return }
    val (subjectId) = guardLet(state.viewState.subjects?.selected?.id) { return }
    val (caption) = guardLet(state.viewState.caption) { return }
    val (action) = guardLet(state.viewState.actions?.selected) { return }

    androidAutoItemRepository.count()
      .map { count ->
        AndroidAutoItemEntity(
          id = state.id ?: 0,
          subjectId = subjectId,
          subjectType = state.viewState.subjectType,
          caption = caption,
          action = action,
          profileId = profileId,
          order = count + 1
        )
      }.flatMapCompletable { androidAutoItemRepository.insert(it) }
      .attach()
      .subscribeBy(
        onComplete = {
          sendEvent(AddAndroidAutoItemViewEvent.Close)
        }
      )
      .disposeBySelf()
  }

  override fun onDelete() {
    updateState { it.copy(showDeletePopup = true) }
  }

  fun onDeleteConfirmed() {
    val (id) = guardLet(currentState().id) { return }

    androidAutoItemRepository.delete(id)
      .attach()
      .subscribeBy(
        onComplete = {
          sendEvent(AddAndroidAutoItemViewEvent.Close)
        }
      )
      .disposeBySelf()
  }

  fun onDeleteCanceled() {
    updateState { it.copy(showDeletePopup = false) }
  }

  private fun getSubjectsSource(profileId: Long, subjectType: SubjectType): Single<List<SubjectItem>> =
    when (subjectType) {
      SubjectType.CHANNEL ->
        channelRepository.findProfileChannels(profileId)
          .map { channels -> channelsSubjectItems(channels.filter { it.function.actions.isNotEmpty() }) }

      SubjectType.GROUP ->
        channelGroupRepository.findProfileGroups(profileId)
          .map { groups -> groupsSubjectItems(groups.filter { it.function.actions.isNotEmpty() }) }

      SubjectType.SCENE ->
        sceneRepository.findProfileScenes(profileId)
          .map { scenes -> scenesSubjectItems(scenes) }
    }

  private fun List<SubjectItem>.asSingleSelectionList(type: SubjectType, selectedId: Int? = null): SingleSelectionList<SubjectItem>? =
    if (isEmpty()) {
      null
    } else {
      SingleSelectionList(
        selected = firstOrNull { it.id == selectedId } ?: first { !it.isLocation },
        label = type.nameRes,
        items = this
      )
    }

  private fun List<ProfileEntity>.asSingleSelectionList(selectedId: Long? = null): SingleSelectionList<ProfileItem>? =
    map { ProfileItem(it.id!!, LocalizedString.Constant(it.name)) }
      .asSingleSelectionList(R.string.widget_configure_profile_label)
      ?.let { list ->
        list.copy(selected = list.items.firstOrNull { it.id == selectedId } ?: list.items.first())
      }

  private fun <T : SpinnerItem> List<T>.asSingleSelectionList(label: Int): SingleSelectionList<T>? =
    if (isEmpty()) {
      null
    } else {
      SingleSelectionList(
        selected = first(),
        label = label,
        items = this
      )
    }

  private fun channelsSubjectItems(items: List<ChannelDataEntity>): List<SubjectItem> =
    toSubjectItems(items, { it.locationEntity }, { it.subjectItem })

  private fun groupsSubjectItems(items: List<ChannelGroupDataEntity>): List<SubjectItem> =
    toSubjectItems(items, { it.locationEntity }, { it.subjectItem })

  private fun scenesSubjectItems(items: List<SceneDataEntity>): List<SubjectItem> =
    toSubjectItems(items, { it.locationEntity }, { it.subjectItem })

  private fun <T> toSubjectItems(
    items: List<T>,
    locationExporter: (T) -> LocationEntity,
    converter: (T) -> SubjectItem
  ): List<SubjectItem> {
    if (items.isEmpty()) {
      return emptyList()
    }

    return mutableListOf<SubjectItem>().apply {
      var location = locationExporter(items.first())
      add(location.subjectItem)

      items.forEach {
        val currentLocation = locationExporter(it)
        if (location.caption != currentLocation.caption) {
          location = currentLocation
          add(location.subjectItem)
        }
        add(converter(it))
      }
    }
  }

  private val LocationEntity.subjectItem: SubjectItem
    get() = SubjectItem(remoteId, LocalizedString.Constant(caption), emptyList(), null, true)

  private val ChannelDataEntity.subjectItem: SubjectItem
    get() = SubjectItem(
      id = remoteId,
      caption = getCaptionUseCase(shareable),
      actions = function.actions,
      icon = getChannelIconUseCase.forState(channelEntity, offlineState),
      isLocation = false
    )

  private val ChannelGroupDataEntity.subjectItem: SubjectItem
    get() = SubjectItem(
      id = remoteId,
      caption = getCaptionUseCase(shareable),
      actions = function.actions,
      icon = getChannelIconUseCase.forState(channelGroupEntity, offlineState),
      isLocation = false
    )

  private val SceneDataEntity.subjectItem: SubjectItem
    get() = SubjectItem(
      id = remoteId,
      caption = getCaptionUseCase(sceneEntity),
      actions = listOf(ActionId.EXECUTE, ActionId.INTERRUPT, ActionId.INTERRUPT_AND_EXECUTE),
      icon = getSceneIconUseCase(sceneEntity),
      isLocation = false
    )
}

sealed class AddAndroidAutoItemViewEvent : ViewEvent {
  data object Close : AddAndroidAutoItemViewEvent()
}

data class AddAndroidAutoItemViewModelState(
  val id: Long? = null,
  val viewState: AddAndroidAutoItemViewState = AddAndroidAutoItemViewState(),
  val selections: Set<Selection> = emptySet(),
  val showDeletePopup: Boolean = false
) : ViewState() {

  fun updateSelections(actionId: ActionId): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id
      val caption = viewState.caption

      if (profileId != null && subjectId != null && caption != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, caption, actionId))
      }
    }

  fun updateSelections(caption: String): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id
      val actionId = viewState.actions?.selected

      if (profileId != null && subjectId != null && actionId != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, caption, actionId))
      }
    }

  fun lastSubjectType(profileId: Long?): SubjectType? =
    selections.lastOrNull { it.profileId == profileId }?.subjectType

  fun lastSubjectId(profileId: Long?, subjectType: SubjectType): Int? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType }?.subjectId

  fun lastCaption(profileId: Long?, subjectType: SubjectType, subjectId: Int?): String? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.caption

  fun lastActionId(profileId: Long?, subjectType: SubjectType, subjectId: Int?): ActionId? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.action

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
  val action: ActionId
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

private val SuplaFunction.actions: List<ActionId>
  get() = when (this) {
    SuplaFunction.OPEN_SENSOR_GATEWAY,
    SuplaFunction.OPEN_SENSOR_GATE,
    SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaFunction.OPEN_SENSOR_DOOR,
    SuplaFunction.NO_LIQUID_SENSOR,
    SuplaFunction.DEPTH_SENSOR,
    SuplaFunction.DISTANCE_SENSOR,
    SuplaFunction.OPENING_SENSOR_WINDOW,
    SuplaFunction.HOTEL_CARD_SENSOR,
    SuplaFunction.ALARM_ARMAMENT_SENSOR,
    SuplaFunction.MAIL_SENSOR,
    SuplaFunction.WIND_SENSOR,
    SuplaFunction.PRESSURE_SENSOR,
    SuplaFunction.RAIN_SENSOR,
    SuplaFunction.WEIGHT_SENSOR,
    SuplaFunction.WEATHER_STATION,
    SuplaFunction.THERMOMETER,
    SuplaFunction.HUMIDITY,
    SuplaFunction.HUMIDITY_AND_TEMPERATURE,
    SuplaFunction.UNKNOWN,
    SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaFunction.RING,
    SuplaFunction.ALARM,
    SuplaFunction.NOTIFICATION,
    SuplaFunction.ELECTRICITY_METER,
    SuplaFunction.IC_ELECTRICITY_METER,
    SuplaFunction.IC_GAS_METER,
    SuplaFunction.IC_WATER_METER,
    SuplaFunction.IC_HEAT_METER,
    SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaFunction.GENERAL_PURPOSE_METER,
    SuplaFunction.DIGIGLASS_HORIZONTAL,
    SuplaFunction.DIGIGLASS_VERTICAL,
    SuplaFunction.CONTAINER,
    SuplaFunction.SEPTIC_TANK,
    SuplaFunction.WATER_TANK,
    SuplaFunction.CONTAINER_LEVEL_SENSOR,
    SuplaFunction.FLOOD_SENSOR,
    SuplaFunction.PUMP_SWITCH,
    SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
    SuplaFunction.NONE -> emptyList()

    SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> listOf(ActionId.OPEN)
    SuplaFunction.CONTROLLING_THE_GATE,
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR -> listOf(ActionId.OPEN_CLOSE, ActionId.OPEN, ActionId.CLOSE)

    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.ROLLER_GARAGE_DOOR -> listOf(ActionId.SHUT, ActionId.REVEAL)

    SuplaFunction.POWER_SWITCH,
    SuplaFunction.LIGHTSWITCH,
    SuplaFunction.STAIRCASE_TIMER,
    SuplaFunction.DIMMER,
    SuplaFunction.RGB_LIGHTING,
    SuplaFunction.DIMMER_AND_RGB_LIGHTING,
    SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
    SuplaFunction.HVAC_THERMOSTAT,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> listOf(ActionId.TURN_ON, ActionId.TURN_OFF, ActionId.TOGGLE)

    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE -> listOf(ActionId.OPEN, ActionId.CLOSE)

    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.PROJECTOR_SCREEN,
    SuplaFunction.CURTAIN -> listOf(ActionId.EXPAND, ActionId.COLLAPSE)
  }
