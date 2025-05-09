package org.supla.android.features.main

import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
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
}

sealed class MainViewEvent : ViewEvent

class MainViewState : ViewState()
