package org.supla.android.features.standarddetail

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.ChannelBase
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class StandardDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<StandardDetailViewState, StandardDetailViewEvent>(StandardDetailViewState(), schedulers) {

  fun loadData(remoteId: Int, itemType: ItemType) {
    getDataSource(remoteId, itemType)
      .attach()
      .subscribeBy(
        onSuccess = { channel -> updateState { it.copy(channelBase = channel) } }
      )
      .disposeBySelf()
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> readChannelByRemoteIdUseCase(remoteId)
    ItemType.GROUP -> readChannelGroupByRemoteIdUseCase(remoteId)
  }
}

sealed class StandardDetailViewEvent : ViewEvent

data class StandardDetailViewState(
  val channelBase: ChannelBase? = null
) : ViewState()