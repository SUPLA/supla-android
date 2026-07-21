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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.branding.Configuration.Menu
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.main.LocalMainListTabController
import org.supla.android.main.ListTab
import org.supla.android.main.LocalNavigator
import org.supla.android.main.MainRoute
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.texts.HeadlineSmall

@Composable
fun MainDrawer(
  developerOptionsVisibleFlow: StateFlow<Boolean>,
  zWaveVisibleFlow: StateFlow<Boolean>,
  zWaveOpenCallback: () -> Unit,
  content: @Composable (() -> Unit)
) =
  ModalNavigationDrawer(
    drawerState = requireNotNull(LocalDrawerState.current),
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerTonalElevation = 0.dp,
        drawerShape = RectangleShape,
        windowInsets = DrawerDefaults.windowInsets.exclude(WindowInsets.statusBars)
      ) {
        DrawerContent(developerOptionsVisibleFlow, zWaveVisibleFlow, zWaveOpenCallback)
      }
    },
    scrimColor = Color.Black.copy(alpha = 0.32f),
    content = content
  )

@Composable
fun PermanentMainDrawer(
  developerOptionsVisibleFlow: StateFlow<Boolean>,
  zWaveVisibleFlow: StateFlow<Boolean>,
  zWaveOpenCallback: () -> Unit,
  content: @Composable (() -> Unit)
) =
  PermanentNavigationDrawer(
    drawerContent = {
      PermanentDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerTonalElevation = 0.dp
      ) {
        DrawerContent(developerOptionsVisibleFlow, zWaveVisibleFlow, zWaveOpenCallback)
      }
    },
    content = content
  )

@Composable
@Suppress("SimplifyBooleanWithConstants")
private fun DrawerContent(
  developerOptionsVisibleFlow: StateFlow<Boolean>,
  zWaveVisibleFlow: StateFlow<Boolean>,
  zWaveOpenCallback: () -> Unit
) {
  Column(
    modifier = Modifier.verticalScroll(rememberScrollState())
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = Distance.tiny)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .statusBarsPadding()
        .height(dimensionResource(R.dimen.top_bar_height))
    ) {
      val drawerState = LocalDrawerState.current
      val scope = rememberCoroutineScope()
      DrawerBackButton(
        modifier = Modifier.align(Alignment.CenterStart),
        onClick = { scope.launch { drawerState?.close() } }
      )
      HeadlineSmall(
        text = stringResource(R.string.app_name),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.align(Alignment.Center)
      )
    }

    if (!LocalApplicationPreferences.current.isShowBottomMenu) {
      val tabController = LocalMainListTabController.current
      val selectedTab = tabController.tab
      DrawerItem(
        iconRes = R.drawable.navbar_channels,
        labelRes = R.string.navbar_channels,
        selected = selectedTab == ListTab.CHANNELS,
        onNavigate = { tabController.changeTab(ListTab.CHANNELS) },
      )
      DrawerItem(
        iconRes = R.drawable.navbar_groups,
        labelRes = R.string.navbar_groups,
        selected = selectedTab == ListTab.GROUPS,
        onNavigate = { tabController.changeTab(ListTab.GROUPS) },
      )
      DrawerItem(
        iconRes = R.drawable.navbar_scenes,
        labelRes = R.string.navbar_scenes,
        selected = selectedTab == ListTab.SCENES,
        onNavigate = { tabController.changeTab(ListTab.SCENES) },
      )
      HorizontalDivider(modifier = Modifier.padding(top = Distance.small, bottom = Distance.tiny))
    }

    val navigator = LocalNavigator.current
    DrawerItem(
      iconRes = R.drawable.ic_menu_profiles,
      labelRes = R.string.profile_plural,
      onNavigate = { navigator?.navigateToProfiles() }
    )
    DrawerItem(
      iconRes = R.drawable.ic_menu_settings,
      labelRes = R.string.settings,
      onNavigate = { navigator?.navigateTo(MainRoute.Settings) }
    )
    HorizontalDivider(modifier = Modifier.padding(top = Distance.small, bottom = Distance.tiny))

    DrawerItem(
      iconRes = R.drawable.ic_menu_add_device,
      labelRes = R.string.add_device,
      onNavigate = { navigator?.navigateTo(MainRoute.AddWizard) }
    )

    val zWaveVisible by zWaveVisibleFlow.collectAsState()
    if (Menu.Z_WAVE_OPTION_VISIBLE && zWaveVisible) {
      DrawerItem(
        iconRes = R.drawable.ic_menu_z_wave,
        labelRes = R.string.z_wave,
        onNavigate = zWaveOpenCallback
      )
    }
    if (Menu.DEVICES_OPTION_VISIBLE) {
      DrawerItem(
        iconRes = R.drawable.ic_menu_device_catalog,
        labelRes = R.string.menu_device_catalog,
        onNavigate = { navigator?.navigateTo(MainRoute.DeviceCatalog) }
      )
    }
    DrawerItem(
      iconRes = R.drawable.ic_notification,
      labelRes = R.string.menu_notifications,
      onNavigate = { navigator?.navigateTo(MainRoute.NotificationsLog) }
    )
    HorizontalDivider(modifier = Modifier.padding(top = Distance.small, bottom = Distance.tiny))
    DrawerItem(
      iconRes = R.drawable.ic_menu_cloud,
      labelRes = R.string.supla_cloud,
      onNavigate = { navigator?.navigateToCloudExternal() }
    )
    if (Menu.HELP_OPTION_VISIBLE) {
      val url = stringResource(R.string.forumpage_url)
      DrawerItem(
        iconRes = R.drawable.ic_menu_help,
        labelRes = R.string.help,
        onNavigate = { navigator?.navigateToWeb(url.toUri()) }
      )
    }
    if (Menu.ABOUT_OPTION_VISIBLE) {
      DrawerItem(
        iconRes = R.drawable.ic_menu_about,
        labelRes = R.string.about,
        onNavigate = { navigator?.navigateTo(MainRoute.About) }
      )
    }
    val developerOptionsVisible by developerOptionsVisibleFlow.collectAsState()
    if (developerOptionsVisible) {
      DrawerItem(
        iconRes = R.drawable.ic_dev_option,
        labelRes = R.string.developer_option,
        onNavigate = { navigator?.navigateTo(MainRoute.DeveloperInfo) }
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = Distance.small),
      horizontalArrangement = Arrangement.Center
    ) {
      TextButton(
        text = stringResource(R.string.homepage),
        onClick = { navigator?.navigateToSuplaOrgExternal() }
      )
    }
  }
}

@Composable
private fun DrawerItem(
  @DrawableRes iconRes: Int,
  @StringRes labelRes: Int,
  selected: Boolean = false,
  onNavigate: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val drawerState = LocalDrawerState.current
  NavigationDrawerItem(
    icon = { Icon(painterResource(iconRes), null) },
    label = { DrawerLabel(labelRes) },
    selected = selected,
    onClick = {
      onNavigate()
      drawerState?.let { scope.launch { it.close() } }
    },
    modifier = Modifier
      .padding(start = Distance.small, top = 4.dp, end = Distance.small)
      .height(48.dp),
    colors = NavigationDrawerItemDefaults.colors(
      selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      selectedIconColor = MaterialTheme.colorScheme.onSurface,
      unselectedIconColor = MaterialTheme.colorScheme.onSurface,
      selectedTextColor = MaterialTheme.colorScheme.onSurface,
      unselectedTextColor = MaterialTheme.colorScheme.onSurface
    )
  )
}

@Composable
private fun DrawerLabel(@StringRes stringRes: Int) =
  Text(
    text = stringResource(stringRes),
    style = MaterialTheme.typography.labelMedium,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
  )

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    val context = LocalContext.current
    CompositionLocalProvider(LocalApplicationPreferences provides ApplicationPreferences(context)) {
      Column {
        DrawerContent(
          developerOptionsVisibleFlow = MutableStateFlow(true),
          zWaveVisibleFlow = MutableStateFlow(false),
          zWaveOpenCallback = {}
        )
      }
    }
  }
}

val LocalDrawerState = staticCompositionLocalOf<DrawerState?> { null }
