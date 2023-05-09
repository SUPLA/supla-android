package org.supla.android.features.channellist

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val profileManager: ProfileManager,
  private val channelActionUseCase: ChannelActionUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<ChannelListViewState, ChannelListViewEvent>(ChannelListViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  fun loadChannels() {
    reloadObservable()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } }
      )
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    if (location.isCollapsed()) {
      location.collapsed = (location.collapsed and 0x1.inv())
    } else {
      location.collapsed = (location.collapsed or 0x1)
    }

    Completable.fromRunnable {
      channelRepository.updateLocation(location)
    }
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
      .attachSilent()
      .subscribeBy()
      .disposeBySelf()
  }

  fun performAction(channelId: Int, buttonType: ButtonType) {
    channelActionUseCase(channelId, buttonType)
      .attachSilent()
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

          if (location?.isCollapsed() == false) {
            channels.add(ListItem.ChannelItem(channel))
          }
        } while (cursor.moveToNext())
      }
      cursor.close()

      return@use channels
    }
  }

  private fun Location.isCollapsed(): Boolean = (collapsed and 0x1 > 0)
}

sealed class ChannelListViewEvent : ViewEvent {
  data class ShowValveDialog(val remoteId: Int) : ChannelListViewEvent()
  data class ShowAmperageExceededDialog(val remoteId: Int) : ChannelListViewEvent()
}

data class ChannelListViewState(
  override val loading: Boolean = false,
  val channels: List<ListItem> = emptyList()
) : ViewState(loading)