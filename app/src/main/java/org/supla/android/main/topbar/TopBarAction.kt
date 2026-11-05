package org.supla.android.main.topbar
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

import androidx.annotation.StringRes
import org.supla.android.R

sealed interface TopBarAction {
  @get:StringRes val titleRes: Int
  val icon: TopBarIcon?
  val event: TopBarEvent
  val displayMode: TopBarDisplayMode get() = TopBarDisplayMode.AUTO
  val clearTime: TopBarClearTime get() = TopBarClearTime.TAB_CHANGE

  data object ProfileSelect : TopBarAction {
    override val titleRes = R.string.profile_plural
    override val icon = TopBarIcon.PROFILES
    override val event = TopBarEvent.OpenProfileSelector
  }

  data object NotificationsDeleteAll : TopBarAction {
    override val titleRes = R.string.toolbar_delete_all
    override val icon: TopBarIcon? = null
    override val displayMode = TopBarDisplayMode.OVERFLOW_ONLY
    override val event = TopBarEvent.DeleteAll
  }

  data object NotificationsDeleteOlderThanMonth : TopBarAction {
    override val titleRes = R.string.toolbar_delete_older_than_month
    override val icon: TopBarIcon? = null
    override val displayMode = TopBarDisplayMode.OVERFLOW_ONLY
    override val event = TopBarEvent.DeleteLastMonth
  }

  data object HistoryReload : TopBarAction {
    override val titleRes = R.string.toolbar_delete_chart_history
    override val icon: TopBarIcon? = null
    override val displayMode = TopBarDisplayMode.OVERFLOW_ONLY
    override val event = TopBarEvent.ReloadChartHistory
  }

  data object ImpulseCounterOpenOcr : TopBarAction {
    override val titleRes = R.string.toolbar_ocr
    override val icon = TopBarIcon.OPEN_OCR
    override val clearTime = TopBarClearTime.TAB_CHANGE
    override val event = TopBarEvent.OpenOcr
  }

  data object DetailDimmerSettings : TopBarAction {
    override val titleRes = R.string.details_settings
    override val icon = TopBarIcon.OPEN_SETTINGS
    override val clearTime = TopBarClearTime.DETAIL_CHANGE
    override val event = TopBarEvent.DetailDimmerSettings
  }

  data object DetailElectricityMeterSettings : TopBarAction {
    override val titleRes = R.string.details_settings
    override val icon = TopBarIcon.OPEN_SETTINGS
    override val clearTime = TopBarClearTime.DETAIL_CHANGE
    override val event = TopBarEvent.DetailElectricityMeterSettings
  }

  data object DetailImpulseCounterSettings : TopBarAction {
    override val titleRes = R.string.details_settings
    override val icon = TopBarIcon.OPEN_SETTINGS
    override val clearTime = TopBarClearTime.DETAIL_CHANGE
    override val event = TopBarEvent.DetailImpulseCounterSettings
  }
}

enum class TopBarDisplayMode { AUTO, OVERFLOW_ONLY }

enum class TopBarClearTime { TAB_CHANGE, DETAIL_CHANGE }
