package org.supla.android.features.lockscreen
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.branding.Configuration
import org.supla.android.core.infrastructure.LocalDateProvider
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalTimeFormatter
import org.supla.android.ui.views.BodyLarge
import org.supla.android.ui.views.BodyMedium
import org.supla.android.ui.views.BodySmall
import org.supla.android.ui.views.HeadlineSmall
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.PinTextField
import org.supla.android.ui.views.buttons.TextButton
import org.supla.core.shared.extensions.guardLet

data class LockScreenViewState(
  val pin: String = "",
  val wrongPin: Boolean = false,
  val unlockAction: UnlockAction = UnlockAction.AuthorizeApplication,
  val authenticatorError: StringProvider? = null,
  val lockedTime: Long? = null,
  val biometricAllowed: Boolean = false
) {
  @Composable
  fun getTimeString(): String {
    val (currentLockedTime) = guardLet(lockedTime) { return "" }

    var timeString by remember { mutableStateOf("") }
    val formatter = LocalTimeFormatter.current
    val dateProvider = LocalDateProvider.current

    LaunchedEffect(lockedTime) {
      var remainingTime = currentLockedTime.minus(dateProvider.currentTimestamp())
      while (remainingTime > 0) {
        timeString = formatter.format(remainingTime.div(1000).toInt())
        delay(100)
        remainingTime = currentLockedTime.minus(dateProvider.currentTimestamp())
      }
      timeString = ""
    }

    return timeString
  }
}

@Composable
fun LockScreenView(
  viewState: LockScreenViewState,
  onPinChange: (String) -> Unit = {},
  onForgottenCodeClick: () -> Unit = {},
  onFingerprintIconClick: () -> Unit = {},
  windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(top = getTopDistance(viewState = viewState))
  ) {
    val logoBottomSpace = if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) 10.dp else 40.dp
    val timeString = viewState.getTimeString()
    val pinLocked = timeString.isNotEmpty()

    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.tiny),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(all = Distance.default)
        .align(Alignment.TopCenter)
    ) {
      if (viewState.unlockAction.showLogo) {
        Image(Configuration.LockScreen.LOGO_RESOURCE, modifier = Modifier.widthIn(max = 144.dp))
        Spacer(modifier = Modifier.height(logoBottomSpace))
      }
      viewState.unlockAction.messageId?.let {
        HeadlineSmall(stringRes = it)
      }
      if (pinLocked) {
        BodyLarge(stringResource(id = R.string.lock_screen_pin_locked, timeString))
      } else {
        PinEntry(viewState = viewState, onPinChange = onPinChange)
      }
      Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.button_default_size)))
      TextButton(
        onClick = onForgottenCodeClick,
        text = stringResource(id = R.string.lock_screen_forgotten_code),
        color = MaterialTheme.colorScheme.primary
      )
    }

    if (viewState.biometricAllowed && pinLocked.not()) {
      AuthenticationView(viewState = viewState, onClick = onFingerprintIconClick)
    }
  }
}

@Composable
private fun PinEntry(viewState: LockScreenViewState, onPinChange: (String) -> Unit) {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  BodyMedium(stringRes = R.string.lock_screen_enter_pin)
  Spacer(modifier = Modifier.height(Distance.tiny))
  PinTextField(
    pin = viewState.pin,
    onPinChange = { pin, _ -> onPinChange(pin) },
    isError = viewState.wrongPin,
    modifier = Modifier.focusRequester(focusRequester)
  )
  if (viewState.wrongPin) {
    BodySmall(stringRes = R.string.lock_screen_wrong_pin, color = MaterialTheme.colorScheme.error)
  }
}

@Composable
private fun BoxScope.AuthenticationView(viewState: LockScreenViewState, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = Modifier
      .align(Alignment.BottomCenter)
      .padding(bottom = 72.dp)
  ) {
    IconButton(
      onClick = onClick,
      enabled = viewState.authenticatorError == null,
      modifier = Modifier
    ) {
      Icon(
        imageVector = Icons.Filled.Fingerprint,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = if (viewState.authenticatorError == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
      )
    }
    if (viewState.authenticatorError != null) {
      BodyMedium(stringProvider = viewState.authenticatorError)
    }
  }
}

@Composable
private fun getTopDistance(viewState: LockScreenViewState): Dp =
  when (viewState.unlockAction) {
    UnlockAction.AuthorizeAccountsCreate,
    is UnlockAction.AuthorizeAccountsEdit -> dimensionResource(id = R.dimen.top_bar_height)

    else -> 0.dp
  }

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    LockScreenView(LockScreenViewState())
  }
}

@Preview
@Composable
private fun Preview_Error() {
  SuplaTheme {
    LockScreenView(LockScreenViewState(pin = "1234", wrongPin = true, authenticatorError = { "error" }))
  }
}

@Preview
@Composable
private fun Preview_Locked() {
  SuplaTheme {
    LockScreenView(LockScreenViewState(lockedTime = System.currentTimeMillis().plus(40000)))
  }
}
