package org.supla.android.main
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

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.features.nfc.edit.NewItemData
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

@Serializable
sealed interface MainRoute : NavKey {
  val screenTakeoverAllowed: Boolean

  @Composable
  fun title(): String = ""

  @Serializable
  data object Status : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class List(val tab: ListTab = ListTab.CHANNELS) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class StandardDetail(
    val item: ItemBundle,
    val title: LocalizedString,
    val pages: kotlin.collections.List<DetailPage>
  ) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class Lock(val unlockAction: UnlockAction) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.pin_setup_title)()
  }

  @Serializable
  data object Settings : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.settings)()
  }

  @Serializable
  data object AddWizard : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data object DeviceCatalog : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.menu_device_catalog)()
  }

  @Serializable
  data object NotificationsLog : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.menu_notifications)()
  }

  @Serializable
  data object About : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.about)()
  }

  @Serializable
  data object DeveloperInfo : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.developer_option)()
  }

  @Serializable
  data object LocationReorder : MainRoute {
    override val screenTakeoverAllowed: Boolean = false

    @Composable
    override fun title(): String = localizedString(R.string.location_ordering)()
  }

  @Serializable
  data object AndroidAutoItems : MainRoute {
    override val screenTakeoverAllowed: Boolean = false

    @Composable
    override fun title(): String = localizedString(R.string.settings_android_auto_label)()
  }

  @Serializable
  data class AddAndroidAutoItem(val id: Long? = null) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false

    @Composable
    override fun title(): String = localizedString(R.string.settings_android_auto_label)()
  }

  @Serializable
  data object NfcTagList : MainRoute {
    override val screenTakeoverAllowed: Boolean = true

    @Composable
    override fun title(): String = localizedString(R.string.nfc_list_title)()
  }

  @Serializable
  data class NfcTagDetail(val id: Long) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data object AddNfcTag : MainRoute {
    override val screenTakeoverAllowed: Boolean = false

    @Composable
    override fun title(): String = localizedString(R.string.menu_nfc)()
  }

  @Serializable
  data class EditNfcTag(val id: Long? = null, val newItemData: NewItemData? = null) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data class LockNfcTag(val id: Long) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false

    @Composable
    override fun title(): String = localizedString(R.string.nfc_lock_tag_label)()
  }

  @Serializable
  data class PinSetup(val lockScreenScope: LockScreenScope) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false

    @Composable
    override fun title(): String = localizedString(R.string.pin_setup_title)()
  }

  @Serializable
  data class CounterPhoto(val remoteId: Int) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class LegacyDimmerSettings(val item: ItemBundle) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }
}

enum class ListTab {
  CHANNELS, GROUPS, SCENES
}
