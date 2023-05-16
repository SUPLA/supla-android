package org.supla.android.features.switchdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.ChannelBase
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class SwitchDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val suplaClientProvider: SuplaClientProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<SwitchDetailViewState, SwitchDetailViewEvent>(SwitchDetailViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

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

  fun toggle(remoteId: Int) {
    Completable.fromRunnable {
      suplaClientProvider.provide()?.run {
        executeAction(ActionParameters(ActionId.TOGGLE, SubjectType.CHANNEL, remoteId))
      }
    }
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }
}

sealed class SwitchDetailViewEvent : ViewEvent {
}

data class SwitchDetailViewState(
  val channelBase: ChannelBase? = null,
  override val loading: Boolean = false
) : ViewState(loading)
