package org.supla.android.features.channellist

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.details.DetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val profileManager: ProfileManager,
  private val channelActionUseCase: ChannelActionUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val provideDetailTypeUseCase: ProvideDetailTypeUseCase,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseListViewModel<ChannelListViewState, ChannelListViewEvent>(preferences, ChannelListViewState(), schedulers) {

  override fun sendReassignEvent() = sendEvent(ChannelListViewEvent.ReassignAdapter)
  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading, channels = null)

  fun loadChannels() {
    reloadObservable()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } }
      )
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    toggleLocationUseCase(location, CollapsedFlag.CHANNEL)
      .andThen(reloadObservable())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } }
      )
      .disposeBySelf()
  }

  fun swapItems(firstItem: ChannelBase?, secondItem: ChannelBase?) {
    if (firstItem == null || secondItem == null) {
      return // nothing to swap
    }

    channelRepository.reorderChannels(firstItem.id, firstItem.locationId.toInt(), secondItem.id)
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }

  fun performAction(channelId: Int, buttonType: ButtonType) {
    channelActionUseCase(channelId, buttonType)
      .attach()
      .subscribeBy(
        onError = { throwable ->
          when (throwable) {
            is ActionException.ChannelClosedManually -> sendEvent(ChannelListViewEvent.ShowValveDialog(throwable.remoteId))
            is ActionException.ChannelExceedAmperage -> sendEvent(ChannelListViewEvent.ShowAmperageExceededDialog(throwable.remoteId))
          }
        }
      )
      .disposeBySelf()
  }

  fun onListItemClick(channelId: Int) {
    readChannelByRemoteIdUseCase(channelId)
      .attach()
      .subscribeBy(
        onSuccess = { openDetailsByChannelFunction(it) }
      )
      .disposeBySelf()
  }

  private fun openDetailsByChannelFunction(channel: Channel) {
    if (channel.onLine.not()) {
      return // do not open details for offline channels
    }

    if (channel.func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT) {
      sendEvent(ChannelListViewEvent.OpenThermostatDetails)
      return
    }

    val detailType = provideDetailTypeUseCase(channel)
    if (detailType != null) {
      sendEvent(ChannelListViewEvent.OpenLegacyDetails(channel.channelId, detailType))
    }
  }

  private fun reloadObservable(): Observable<List<ListItem>> = Observable.fromCallable {
    channelRepository.getAllProfileChannels(profileManager.getCurrentProfile().blockingGet()!!.id).use { cursor ->
      val channels = mutableListOf<ListItem>()

      var location: Location? = null
      if (cursor.moveToFirst()) {
        do {
          val channel = Channel()
          channel.AssignCursorData(cursor)

          if (location == null || location.locationId != channel.locationId.toInt()) {
            location = channelRepository.getLocation(channel.locationId.toInt())
            channels.add(ListItem.LocationItem(location))
          }

          if (location?.isCollapsed(CollapsedFlag.CHANNEL) == false) {
            channels.add(ListItem.ChannelItem(channel))
          }
        } while (cursor.moveToNext())
      }
      cursor.close()

      return@use channels
    }
  }
}

sealed class ChannelListViewEvent : ViewEvent {
  data class ShowValveDialog(val remoteId: Int) : ChannelListViewEvent()
  data class ShowAmperageExceededDialog(val remoteId: Int) : ChannelListViewEvent()
  data class OpenLegacyDetails(val remoteId: Int, val type: DetailType) : ChannelListViewEvent()
  object OpenThermostatDetails : ChannelListViewEvent()
  object ReassignAdapter : ChannelListViewEvent()
}

data class ChannelListViewState(
  override val loading: Boolean = false,
  val channels: List<ListItem>? = null
) : ViewState(loading)