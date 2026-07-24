package org.supla.android.main.view
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

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.supla.android.features.about.AboutScreen
import org.supla.android.features.addwizard.AddWizardScreen
import org.supla.android.features.androidauto.AndroidAutoItemsScreen
import org.supla.android.features.androidauto.add.AddAndroidAutoItemScreen
import org.supla.android.features.appsettings.SettingsScreen
import org.supla.android.features.details.detailbase.base.DetailScreen
import org.supla.android.features.details.impulsecounter.counterphoto.CounterPhotoScreen
import org.supla.android.features.details.legacydetail.LegacyDetailScreen
import org.supla.android.features.details.rgbanddimmer.legacysettings.LegacyDimmerSettingsScreen
import org.supla.android.features.developerinfo.DeveloperInfoScreen
import org.supla.android.features.devicecatalog.DeviceCatalogScreen
import org.supla.android.features.locationreorder.LocationReorderScreen
import org.supla.android.features.lockscreen.LockScreen
import org.supla.android.features.main.MainListScreen
import org.supla.android.features.nfc.add.AddNfcTagScreen
import org.supla.android.features.nfc.detail.NfcTagDetailScreen
import org.supla.android.features.nfc.edit.EditNfcTagScreen
import org.supla.android.features.nfc.list.NfcTagListScreen
import org.supla.android.features.nfc.lock.LockNfcTagScreen
import org.supla.android.features.notificationslog.NotificationsLogScreen
import org.supla.android.features.pinsetup.PinSetupScreen
import org.supla.android.features.status.StatusScreen
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.scaffold.BackScaffold
import org.supla.android.main.scaffold.EmptyScreenScaffold
import org.supla.android.main.topbar.LocalScreenKey
import org.supla.android.main.topbar.StatusBarAppearance

@Composable
fun MainComposeNavHost(
  backStack: NavBackStack<NavKey>,
  navigator: MainComposeNavigator,
  notificationState: EventNotificationState? = null,
  onEventRemoved: () -> Unit = {},
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  MainOverlays(
    notificationState = notificationState,
    onEventRemoved = onEventRemoved
  ) {
    NavDisplay(
      backStack = backStack,
      modifier = Modifier.fillMaxSize(),
      onBack = { navigator.back() },
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      entryProvider = topBarEntryProvider(
        entryProvider {
          entry<MainRoute.Status> { EmptyScreenScaffold { StatusScreen(navigator) } }
          entry<MainRoute.List> { MainListScreen(drawerState, navigator) }
          fadedEntry<MainRoute.UnlockApp> { EmptyScreenScaffold { LockScreen(it.unlockAction, navigator) } }
          fadedEntry<MainRoute.Unlock> { BackScaffold { LockScreen(it.unlockAction, navigator) } }
          fadedEntry<MainRoute.StandardDetail> { DetailScreen(it.item, it.pages) }
          fadedEntry<MainRoute.Settings> { BackScaffold { SettingsScreen() } }
          fadedEntry<MainRoute.AddWizard> { AddWizardScreen(navigator) }
          fadedEntry<MainRoute.DeviceCatalog> { BackScaffold { DeviceCatalogScreen() } }
          fadedEntry<MainRoute.NotificationsLog> { BackScaffold { NotificationsLogScreen(navigator) } }
          fadedEntry<MainRoute.About> { BackScaffold { AboutScreen(navigator) } }
          fadedEntry<MainRoute.DeveloperInfo> { BackScaffold { DeveloperInfoScreen() } }
          fadedEntry<MainRoute.LocationReorder> { BackScaffold { LocationReorderScreen() } }
          fadedEntry<MainRoute.AndroidAutoItems> { BackScaffold { AndroidAutoItemsScreen(navigator) } }
          fadedEntry<MainRoute.AddAndroidAutoItem> { BackScaffold { AddAndroidAutoItemScreen(navigator, it.id) } }
          fadedEntry<MainRoute.NfcTagList> { BackScaffold { NfcTagListScreen(navigator) } }
          fadedEntry<MainRoute.NfcTagDetail> { BackScaffold { NfcTagDetailScreen(it.id, navigator) } }
          fadedEntry<MainRoute.AddNfcTag> { BackScaffold { AddNfcTagScreen(navigator) } }
          fadedEntry<MainRoute.EditNfcTag> { EditNfcTagScreen(it.id, it.newItemData, navigator) }
          fadedEntry<MainRoute.LockNfcTag> { BackScaffold { LockNfcTagScreen(it.id, navigator) } }
          fadedEntry<MainRoute.PinSetup> { BackScaffold { PinSetupScreen(it.lockScreenScope, navigator) } }
          fadedEntry<MainRoute.CounterPhoto> { BackScaffold { CounterPhotoScreen(it.remoteId) } }
          fadedEntry<MainRoute.LegacyDimmerSettings> { BackScaffold { LegacyDimmerSettingsScreen(it.item, navigator) } }
          fadedEntry<MainRoute.LegacyDetail> { BackScaffold { LegacyDetailScreen(it, navigator) } }
        }
      )
    )
  }
}

fun <T : Any> topBarEntryProvider(
  base: (T) -> NavEntry<T>
): (T) -> NavEntry<T> = { key ->
  val entry = base(key)

  NavEntry(
    key = key,
    metadata = entry.metadata
  ) {
    CompositionLocalProvider(LocalScreenKey provides key) {
      StatusBarAppearance()
      entry.Content()
    }
  }
}

private inline fun <reified K : NavKey> EntryProviderScope<NavKey>.fadedEntry(
  noinline clazzContentKey: (key: @JvmSuppressWildcards K) -> Any = { it.toString() },
  noinline content: @Composable (K) -> Unit,
) {
  addEntryProvider(K::class, clazzContentKey, { fadeTransitionMetadata() }, content)
}

private fun fadeTransitionMetadata(): Map<String, Any> =
  metadata {
    put(NavDisplay.TransitionKey) {
      slideInHorizontally(
        initialOffsetX = { width -> width },
        animationSpec = tween(250)
      ) togetherWith slideOutHorizontally(
        targetOffsetX = { width -> -width },
        animationSpec = tween(250)
      )
    }

    put(NavDisplay.PopTransitionKey) {
      slideInHorizontally(
        initialOffsetX = { width -> -width },
        animationSpec = tween(250)
      ) togetherWith slideOutHorizontally(
        targetOffsetX = { width -> width },
        animationSpec = tween(250)
      )
    }
  }
