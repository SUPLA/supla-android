package org.supla.android.ui.lists

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.tools.SuplaSchedulers

abstract class BaseListViewModel<S : ViewState, E : ViewEvent>(
  private val preferences: Preferences,
  defaultState: S,
  schedulers: SuplaSchedulers
) : BaseViewModel<S, E>(defaultState, schedulers) {

  private val preferencesChangeListener = OnSharedPreferenceChangeListener { _, key ->
    if (key.equals(Preferences.pref_channel_height)) {
      sendReassignEvent()
    }
  }
  private val suplaMessageListener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { onSuplaMessage(it) }

  init {
    preferences.registerChangeListener(preferencesChangeListener)
  }

  @CallSuper
  override fun onCleared() {
    preferences.unregisterChangeListener(preferencesChangeListener)
    super.onCleared()
  }

  protected abstract fun sendReassignEvent()

  protected abstract fun reloadList()

  @VisibleForTesting
  open fun onSuplaMessage(message: SuplaClientMsg) {
  }

  protected fun observeUpdates(updatesObservable: Observable<Any>) {
    updatesObservable
      .attachSilent()
      .subscribeBy(
        onNext = { reloadList() }
      )
      .disposeBySelf()
  }
}
