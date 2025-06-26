package org.supla.android.ui.lists
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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val TESTING_TABLE: List<TestEntry> = listOf(
  TestEntry(ListOnlineState.ONLINE, ListOnlineState.ONLINE, ListOnlineState.ONLINE),
  TestEntry(ListOnlineState.ONLINE, ListOnlineState.OFFLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.ONLINE, ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.ONLINE, ListOnlineState.UNKNOWN, ListOnlineState.ONLINE),
  TestEntry(ListOnlineState.ONLINE, null, ListOnlineState.ONLINE),

  TestEntry(ListOnlineState.OFFLINE, ListOnlineState.ONLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.OFFLINE, ListOnlineState.OFFLINE, ListOnlineState.OFFLINE),
  TestEntry(ListOnlineState.OFFLINE, ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.OFFLINE, ListOnlineState.UNKNOWN, ListOnlineState.OFFLINE),
  TestEntry(ListOnlineState.OFFLINE, null, ListOnlineState.OFFLINE),

  TestEntry(ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.ONLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.OFFLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.UNKNOWN, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.PARTIALLY_ONLINE, null, ListOnlineState.PARTIALLY_ONLINE),

  TestEntry(ListOnlineState.UNKNOWN, ListOnlineState.ONLINE, ListOnlineState.ONLINE),
  TestEntry(ListOnlineState.UNKNOWN, ListOnlineState.OFFLINE, ListOnlineState.OFFLINE),
  TestEntry(ListOnlineState.UNKNOWN, ListOnlineState.PARTIALLY_ONLINE, ListOnlineState.PARTIALLY_ONLINE),
  TestEntry(ListOnlineState.UNKNOWN, ListOnlineState.UNKNOWN, ListOnlineState.UNKNOWN),
  TestEntry(ListOnlineState.UNKNOWN, null, ListOnlineState.OFFLINE),
)

class ListOnlineEspConfigurationStateTest {
  @Test
  fun `verify merge is working correctly`() {
    TESTING_TABLE.forEach { testingEntry ->
      val result = testingEntry.first mergeWith testingEntry.second
      assertThat(result).isEqualTo(testingEntry.result)
    }
  }
}

private data class TestEntry(
  val first: ListOnlineState,
  val second: ListOnlineState?,
  val result: ListOnlineState
)
