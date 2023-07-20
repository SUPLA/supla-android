package org.supla.android.features.standarddetail

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.ChannelBase
import org.supla.android.events.ListsEventsManager
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class StandardDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val listsEventsManager: ListsEventsManager,
  schedulers: SuplaSchedulers
) : BaseViewModel<StandardDetailViewState, StandardDetailViewEvent>(StandardDetailViewState(), schedulers) {

  fun observeUpdates(remoteId: Int, itemType: ItemType, initialFunction: Int) {
    getEventsSource(itemType)
      .flatMapMaybe { getDataSource(remoteId, itemType) }
      .attachSilent()
      .subscribeBy(onNext = { handleChannelBase(it, initialFunction) })
      .disposeBySelf()
  }

  fun loadData(remoteId: Int, itemType: ItemType, initialFunction: Int) {
    getDataSource(remoteId, itemType)
      .attach()
      .subscribeBy(onSuccess = { handleChannelBase(it, initialFunction) })
      .disposeBySelf()
  }

  private fun handleChannelBase(channelBase: ChannelBase, initialFunction: Int) {
    if (channelBase.visible > 0 && channelBase.func == initialFunction) {
      updateState { it.copy(channelBase = channelBase) }
    } else {
      sendEvent(StandardDetailViewEvent.Close)
    }
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> readChannelByRemoteIdUseCase(remoteId)
    ItemType.GROUP -> readChannelGroupByRemoteIdUseCase(remoteId)
  }

  private fun getEventsSource(itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> listsEventsManager.observeChannelUpdates()
    ItemType.GROUP -> listsEventsManager.observeGroupUpdates()
  }
}

sealed class StandardDetailViewEvent : ViewEvent {
  object Close : StandardDetailViewEvent()
}

data class StandardDetailViewState(
  val channelBase: ChannelBase? = null
) : ViewState()
