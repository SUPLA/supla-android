package org.supla.android.features.nfc.call
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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.infrastructure.nfc.navKey
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.setStatusBarColor
import org.supla.android.features.nfc.call.screens.Navigator
import org.supla.android.features.nfc.call.screens.callaction.CallActionScreen
import org.supla.android.features.nfc.call.screens.configureaction.ConfigureActionScreen
import javax.inject.Inject

@AndroidEntryPoint
class CallTagActionActivity : ComponentActivity() {

  @Inject
  lateinit var navigator: Navigator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStatusBarColor(R.color.primary_container, R.color.primary_container, false)

    setContent {
      Content(intent.navKey, navigator)
    }
  }

  @Composable
  private fun Content(initialStackEntry: NavKey, navigator: Navigator) {
    val backStack = rememberNavBackStack(initialStackEntry)

    DisposableEffect(backStack) {
      navigator.bind(backStack)
      onDispose { navigator.unbind(backStack) }
    }

    LaunchedEffect(backStack) {
      snapshotFlow { backStack.toList() }
        .collect { updateBackgroundColor(it.lastOrNull()) }
    }

    SuplaTheme {
      NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None) },
        entryDecorators = listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
          entry<CallActionFromUrl> { CallActionScreen(it, navigator) }
          entry<CallActionFromData> { CallActionScreen(it, navigator) }
          entry<EditMissingAction> { ConfigureActionScreen(it.id, navigator) }
          entry<SaveNewNfcTag> { ConfigureActionScreen(it.uuid, it.readOnly, navigator) }
        }
      )
    }
  }

  private fun updateBackgroundColor(screen: NavKey?) {
    when (screen) {
      is EditMissingAction,
      is SaveNewNfcTag ->
        setStatusBarColor(R.color.background, R.color.background, true)

      else ->
        setStatusBarColor(R.color.primary_container, R.color.primary_container, false)
    }
  }
}
