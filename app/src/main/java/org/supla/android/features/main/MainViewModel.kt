package org.supla.android.features.main

import androidx.lifecycle.viewModelScope
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.Preferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val profileRepository: RoomProfileRepository,
  private val preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseViewModel<MainViewState, MainViewEvent>(MainViewState(), schedulers) {

  fun getLabelVisibility() =
    if (preferences.isShowBottomLabel) {
      NavigationBarView.LABEL_VISIBILITY_LABELED
    } else {
      NavigationBarView.LABEL_VISIBILITY_UNLABELED
    }

  fun getBottomMenuVisible() = preferences.isShowBottomMenu

  fun checkProfilesCount() {
    viewModelScope.launch {
      val count = schedulers.io { profileRepository.countKtx() }
      if (count > 1) {
        sendEvent(MainViewEvent.ShowProfileSelector)
      } else {
        sendEvent(MainViewEvent.HideProfileSelector)
      }
    }
  }
}

sealed class MainViewEvent : ViewEvent {
  data object ShowProfileSelector : MainViewEvent()
  data object HideProfileSelector : MainViewEvent()
}

class MainViewState : ViewState()
