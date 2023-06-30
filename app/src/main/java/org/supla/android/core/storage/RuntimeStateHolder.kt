package org.supla.android.core.storage
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

import javax.inject.Inject
import javax.inject.Singleton

private const val SHARED_PREFERENCES_FILE = "InternalPreferences"
private const val DETAIL_OPENED_PAGE = "$SHARED_PREFERENCES_FILE.DETAIL_OPENED_PAGE_"

@Singleton
class RuntimeStateHolder @Inject constructor() {

  private val detailOpenedPageMap: MutableMap<Int, Int> = mutableMapOf()
  private val lastTimerValueMap: MutableMap<Int, Int> = mutableMapOf()

  fun getDetailOpenedPage(channelId: Int) =
    if (detailOpenedPageMap.containsKey(channelId)) {
      detailOpenedPageMap[channelId]!!
    } else {
      0
    }

  fun setDetailOpenedPage(channelId: Int, openedPage: Int) {
    detailOpenedPageMap[channelId] = openedPage
  }

  fun getLastTimerValue(channelId: Int) =
    if (lastTimerValueMap.containsKey(channelId)) {
      lastTimerValueMap[channelId]!!
    } else {
      1
    }

  fun setLastTimerValue(channelId: Int, timerValue: Int) {
    lastTimerValueMap[channelId] = timerValue
  }
}
