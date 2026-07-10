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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.features.channellist.ChannelListScreen
import org.supla.android.features.grouplist.GroupListScreen
import org.supla.android.features.notificationinfo.NotificationInfoDialog
import org.supla.android.features.scenelist.SceneListScreen
import org.supla.android.main.EventBasedViewModelHost
import org.supla.android.main.ListTab
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.scaffold.LocalScaffoldPadding
import org.supla.android.main.topbar.NavigationType
import org.supla.android.main.topbar.TopBarIcon
import org.supla.android.main.topbar.TopBarState
import org.supla.android.main.topbar.topBarAction
import org.supla.android.main.view.ChannelListLabel
import org.supla.android.main.view.GroupListLabel
import org.supla.android.main.view.LocalDrawerState
import org.supla.android.main.view.MainDrawer
import org.supla.android.main.view.PermanentMainDrawer
import org.supla.android.main.view.SceneListLabel
import org.supla.android.main.view.StandardTopBar
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.navigation.SuplaNavigationBarItem
import org.supla.android.ui.navigation.SuplaRailItem
import org.supla.core.shared.infrastructure.localizedString

@Composable
fun MainListScreen(
  drawerState: DrawerState,
  navigator: MainComposeNavigator,
  viewModel: MainListViewModel = hiltViewModel()
) {
  val wideScreen = LocalWindowInfo.current.containerDpSize.width >= 600.dp && LocalWindowInfo.current.containerDpSize.height >= 500.dp

  CompositionLocalProvider(LocalDrawerState provides drawerState) {
    EventBasedViewModelHost(
      viewModel = viewModel,
      topBarState = TopBarState(
        title = localizedString(R.string.app_name),
        navigationType = if (wideScreen) NavigationType.NONE else NavigationType.DRAWER,
        action = topBarAction(TopBarIcon.Profiles, viewModel::showProfilesPopup)
      ),
      eventHandler = { handleEvent(it, navigator) }
    ) {
      if (wideScreen) {
        WideView(viewModel)
      } else if (LocalConfiguration.current.isPhoneLandscape) {
        LandscapePhoneView(viewModel)
      } else {
        PortraitPhoneView(viewModel)
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
}

@Composable
private fun PortraitPhoneView(
  viewModel: MainListViewModel
) {
  MainDrawer(
    developerOptionsVisibleFlow = viewModel.developerOptionsVisible,
    zWaveVisibleFlow = viewModel.zWaveAvailable,
    zWaveOpenCallback = { viewModel.showAuthorizationDialog(AuthorizationReason.ZWaveWizard) }
  ) {
    Scaffold(
      topBar = { StandardTopBar() },
      bottomBar = {
        if (LocalApplicationPreferences.current.isShowBottomMenu) {
          BottomNavigationBar()
        }
      }
    ) { CommonContent(it) }
  }
}

@Composable
private fun LandscapePhoneView(
  viewModel: MainListViewModel
) {
  MainDrawer(
    developerOptionsVisibleFlow = viewModel.developerOptionsVisible,
    zWaveVisibleFlow = viewModel.zWaveAvailable,
    zWaveOpenCallback = { viewModel.showAuthorizationDialog(AuthorizationReason.ZWaveWizard) }
  ) {
    Row {
      Scaffold(
        topBar = { StandardTopBar() },
        modifier = Modifier.weight(1f)
      ) { CommonContent(it) }
      if (LocalApplicationPreferences.current.isShowBottomMenu) {
        RightNavigationRail()
      }
    }
  }
}

@Composable
private fun WideView(
  viewModel: MainListViewModel
) {
  PermanentMainDrawer(
    developerOptionsVisibleFlow = viewModel.developerOptionsVisible,
    zWaveVisibleFlow = viewModel.zWaveAvailable,
    zWaveOpenCallback = { viewModel.showAuthorizationDialog(AuthorizationReason.ZWaveWizard) }
  ) {
    Scaffold(
      topBar = { StandardTopBar() },
      bottomBar = {
        if (LocalApplicationPreferences.current.isShowBottomMenu) {
          BottomNavigationBar()
        }
      }
    ) { CommonContent(it) }
  }
}

@Composable
private fun CommonContent(paddings: PaddingValues) {
  CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
    val selectedTab = LocalMainListTabController.current.tab
    when (selectedTab) {
      ListTab.CHANNELS -> ChannelListScreen()
      ListTab.GROUPS -> GroupListScreen()
      ListTab.SCENES -> SceneListScreen()
    }
  }
}

@Composable
private fun BottomNavigationBar() =
  NavigationBar(
    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    val tabController = LocalMainListTabController.current
    val selectedTab = tabController.tab

    SuplaNavigationBarItem(
      selected = selectedTab == ListTab.CHANNELS,
      onClick = { tabController.changeTab(ListTab.CHANNELS) },
      iconRes = R.drawable.navbar_channels,
      label = ::ChannelListLabel,
      iconDescription = stringResource(R.string.navbar_channels)
    )
    SuplaNavigationBarItem(
      selected = selectedTab == ListTab.GROUPS,
      onClick = { tabController.changeTab(ListTab.GROUPS) },
      iconRes = R.drawable.navbar_groups,
      label = ::GroupListLabel,
      iconDescription = stringResource(R.string.navbar_groups)
    )
    SuplaNavigationBarItem(
      selected = selectedTab == ListTab.SCENES,
      onClick = { tabController.changeTab(ListTab.SCENES) },
      iconRes = R.drawable.navbar_scenes,
      label = ::SceneListLabel,
      iconDescription = stringResource(R.string.navbar_scenes)
    )
  }

@Composable
private fun RightNavigationRail() =
  NavigationRail(
    modifier = Modifier
      .fillMaxHeight()
      .border(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    val tabController = LocalMainListTabController.current
    val selectedTab = tabController.tab

    Column(
      modifier = Modifier.fillMaxHeight(),
      verticalArrangement = Arrangement.SpaceEvenly
    ) {
      SuplaRailItem(
        selected = selectedTab == ListTab.CHANNELS,
        onClick = { tabController.changeTab(ListTab.CHANNELS) },
        iconRes = R.drawable.navbar_channels,
        label = ::ChannelListLabel,
        iconDescription = stringResource(R.string.navbar_channels)
      )
      SuplaRailItem(
        selected = selectedTab == ListTab.GROUPS,
        onClick = { tabController.changeTab(ListTab.GROUPS) },
        iconRes = R.drawable.navbar_groups,
        label = ::GroupListLabel,
        iconDescription = stringResource(R.string.navbar_groups)
      )
      SuplaRailItem(
        selected = selectedTab == ListTab.SCENES,
        onClick = { tabController.changeTab(ListTab.SCENES) },
        iconRes = R.drawable.navbar_scenes,
        label = ::SceneListLabel,
        iconDescription = stringResource(R.string.navbar_scenes)
      )
    }
  }

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
