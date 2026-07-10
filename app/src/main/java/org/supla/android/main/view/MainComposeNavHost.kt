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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
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
import org.supla.android.main.topbar.LocalTopBarScreenKey

@Composable
fun MainComposeNavHost(
  backStack: NavBackStack<NavKey>,
  navigator: MainComposeNavigator,
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  LoadingOverlay {
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
          entry<MainRoute.List> { MainListScreen(it.tab, drawerState, navigator) }
          entry<MainRoute.UnlockApp> { EmptyScreenScaffold { LockScreen(it.unlockAction, navigator) } }
          entry<MainRoute.Unlock> { BackScaffold { LockScreen(it.unlockAction, navigator) } }
          entry<MainRoute.StandardDetail> { DetailScreen(it.item, it.pages, navigator) }
          entry<MainRoute.Settings> { BackScaffold { SettingsScreen() } }
          entry<MainRoute.AddWizard> { AddWizardScreen(navigator) }
          entry<MainRoute.DeviceCatalog> { BackScaffold { DeviceCatalogScreen() } }
          entry<MainRoute.NotificationsLog> { BackScaffold { NotificationsLogScreen(navigator) } }
          entry<MainRoute.About> { BackScaffold { AboutScreen(navigator) } }
          entry<MainRoute.DeveloperInfo> { BackScaffold { DeveloperInfoScreen() } }
          entry<MainRoute.LocationReorder> { BackScaffold { LocationReorderScreen() } }
          entry<MainRoute.AndroidAutoItems> { BackScaffold { AndroidAutoItemsScreen(navigator) } }
          entry<MainRoute.AddAndroidAutoItem> { BackScaffold { AddAndroidAutoItemScreen(navigator, it.id) } }
          entry<MainRoute.NfcTagList> { BackScaffold { NfcTagListScreen(navigator) } }
          entry<MainRoute.NfcTagDetail> { BackScaffold { NfcTagDetailScreen(it.id, navigator) } }
          entry<MainRoute.AddNfcTag> { BackScaffold { AddNfcTagScreen(navigator) } }
          entry<MainRoute.EditNfcTag> { EditNfcTagScreen(it.id, it.newItemData, navigator) }
          entry<MainRoute.LockNfcTag> { BackScaffold { LockNfcTagScreen(it.id, navigator) } }
          entry<MainRoute.PinSetup> { BackScaffold { PinSetupScreen(it.lockScreenScope, navigator) } }
          entry<MainRoute.CounterPhoto> { BackScaffold { CounterPhotoScreen(it.remoteId, navigator) } }
          entry<MainRoute.LegacyDimmerSettings> { BackScaffold { LegacyDimmerSettingsScreen(it.item, navigator) } }
          entry<MainRoute.LegacyDetail> { BackScaffold { LegacyDetailScreen(it, navigator) } }
        }
      )
    )
  }
}

fun <T : Any> topBarEntryProvider(
  base: (T) -> NavEntry<T>
): (T) -> NavEntry<T> = { key ->
  val entry = base(key)

  NavEntry(key) {
    CompositionLocalProvider(LocalTopBarScreenKey provides key) {
      entry.Content()
    }
  }
}
