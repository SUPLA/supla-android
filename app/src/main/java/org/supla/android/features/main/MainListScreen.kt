package org.supla.android.features.main
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

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.features.channellist.ChannelListScreen
import org.supla.android.features.grouplist.GroupListScreen
import org.supla.android.features.nfc.call.screens.EventBasedViewModelHost
import org.supla.android.features.notificationinfo.NotificationInfoDialog
import org.supla.android.features.scenelist.SceneListScreen
import org.supla.android.main.ListTab
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.scaffold.LocalScaffoldPadding
import org.supla.android.main.view.ChannelListLabel
import org.supla.android.main.view.GroupListLabel
import org.supla.android.main.view.MainDrawer
import org.supla.android.main.view.MainTopBar
import org.supla.android.main.view.SceneListLabel
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.extensions.ifTrue

@Composable
fun MainListScreen(
  selectedTab: ListTab,
  drawerState: DrawerState,
  navigator: MainComposeNavigator,
  viewModel: MainListViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  var searchText by remember { mutableStateOf("") }

  EventBasedViewModelHost(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, navigator) }
  ) {
    MainDrawer(
      navigator = navigator,
      drawerState = drawerState,
      developerOptionsVisibleFlow = viewModel.developerOptionsVisible,
      zWaveVisibleFlow = viewModel.zWaveAvailable,
      zWaveOpenCallback = { viewModel.showAuthorizationDialog(AuthorizationReason.ZWaveWizard) }
    ) {
      Scaffold(
        topBar = {
          MainTopBar(
            searchText = searchText,
            onMenuClick = { scope.launch { drawerState.open() } },
            onProfilesClick = viewModel::showProfilesPopup,
            onTextChange = { searchText = it }
          )
        },
        bottomBar = { BottomNavigationBar(navigator) }
      ) { paddings ->
        CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
          when (selectedTab) {
            ListTab.CHANNELS -> ChannelListScreen(navigator)
            ListTab.GROUPS -> GroupListScreen(navigator)
            ListTab.SCENES -> SceneListScreen(navigator)
          }
        }
      }
    }

    viewModel.authorizationDialogState.collectAsState().value?.let {
      viewModel.AuthorizationDialog(it)
    }

    viewModel.showNotificationInfo.collectAsState().value.ifTrue {
      NotificationInfo(viewModel)
    }

    viewModel.profileSelectionState.collectAsState().value?.let {
      ProfileSelectionDialog(
        profiles = it.profiles,
        onDismiss = viewModel::onDismissProfileSelection,
        onProfileSelected = viewModel::onProfileSelected
      )
    }
  }
}

@Composable
private fun BottomNavigationBar(navigator: MainComposeNavigator) =
  NavigationBar(
    modifier = Modifier
      .border(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    NavigationItem(
      tab = ListTab.CHANNELS,
      navigator = navigator,
      iconRes = R.drawable.navbar_channels,
      label = ::ChannelListLabel
    )
    NavigationItem(
      tab = ListTab.GROUPS,
      navigator = navigator,
      iconRes = R.drawable.navbar_groups,
      label = ::GroupListLabel
    )
    NavigationItem(
      tab = ListTab.SCENES,
      navigator = navigator,
      iconRes = R.drawable.navbar_scenes,
      label = ::SceneListLabel
    )
  }

@Composable
private fun RowScope.NavigationItem(
  tab: ListTab,
  navigator: MainComposeNavigator,
  @DrawableRes iconRes: Int,
  label: @Composable () -> Unit
) =
  NavigationBarItem(
    selected = navigator.current() == MainRoute.List(tab),
    onClick = { navigator.replace(MainRoute.List(tab)) },
    icon = { Icon(painter = painterResource(iconRes), null) },
    label = if (LocalApplicationPreferences.current.isShowBottomLabel) label else null,
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = MaterialTheme.colorScheme.primary,
      selectedTextColor = MaterialTheme.colorScheme.primary,
      indicatorColor = Color.Transparent,
      unselectedIconColor = MaterialTheme.colorScheme.onBackground,
      unselectedTextColor = MaterialTheme.colorScheme.onBackground
    )
  )

@Composable
private fun NotificationInfo(viewModel: MainListViewModel) {
  val context = LocalContext.current
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      viewModel.onNotificationPermissionGranted(context)
    } else {
      viewModel.onSkipNotification()
    }
  }

  NotificationInfoDialog(
    onSkip = viewModel::onSkipNotification,
    onTurnOn = {
      viewModel.hideNotificationsDialog()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  )
}

private fun handleEvent(event: MainListViewEvent, navigator: MainComposeNavigator) {
  when (event) {
    MainListViewEvent.OpenZWaveWizard -> navigator.navigateToZWaveConfigurationWizard()
  }
}
