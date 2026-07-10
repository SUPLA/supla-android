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

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.features.nfc.edit.NewItemData
import org.supla.android.usecases.details.LegacyDetailType

@Serializable
sealed interface MainRoute : NavKey {
  val screenTakeoverAllowed: Boolean

  @Serializable
  data object Status : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object List : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class StandardDetail(
    val item: ItemBundle,
    val pages: kotlin.collections.List<DetailPage>
  ) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class UnlockApp(val unlockAction: UnlockAction) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class Unlock(val unlockAction: UnlockAction) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object Settings : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object AddWizard : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data object DeviceCatalog : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object NotificationsLog : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object About : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object DeveloperInfo : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data object LocationReorder : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data object AndroidAutoItems : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data class AddAndroidAutoItem(val id: Long? = null) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data object NfcTagList : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class NfcTagDetail(val id: Long) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data object AddNfcTag : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data class EditNfcTag(val id: Long? = null, val newItemData: NewItemData? = null) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data class LockNfcTag(val id: Long) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data class PinSetup(val lockScreenScope: LockScreenScope) : MainRoute {
    override val screenTakeoverAllowed: Boolean = false
  }

  @Serializable
  data class CounterPhoto(val remoteId: Int) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class LegacyDimmerSettings(val item: ItemBundle) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }

  @Serializable
  data class LegacyDetail(
    val remoteId: Int,
    val itemType: ItemType,
    val legacyDetailType: LegacyDetailType
  ) : MainRoute {
    override val screenTakeoverAllowed: Boolean = true
  }
}

enum class ListTab {
  CHANNELS, GROUPS, SCENES
}
