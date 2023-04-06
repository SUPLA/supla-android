package org.supla.android.features.webcontent

import org.supla.android.core.ui.ViewEvent

sealed class WebContentViewEvent : ViewEvent {
  object LoadRegistrationScript: WebContentViewEvent()
}