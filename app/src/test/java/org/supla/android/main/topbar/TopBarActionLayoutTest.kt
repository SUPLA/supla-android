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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TopBarActionLayoutTest {
  @Test
  fun `shows two actions as icons`() {
    val layout =
      layoutTopBarActions(listOf(TopBarAction.ProfileSelect, TopBarAction.DetailImpulseCounterSettings), searchIconVisible = false)

    assertThat(
      layout.icons.map {
        it::class
      }
    ).containsExactly(TopBarAction.ProfileSelect::class, TopBarAction.DetailImpulseCounterSettings::class)
    assertThat(layout.overflow).isEmpty()
  }

  @Test
  fun `shows first eligible action as icon when there are more than two actions`() {
    val actions = listOf(TopBarAction.HistoryReload, TopBarAction.ProfileSelect, TopBarAction.DetailImpulseCounterSettings)

    val layout = layoutTopBarActions(actions, searchIconVisible = false)

    assertThat(layout.icons.map { it::class }).containsExactly(TopBarAction.ProfileSelect::class)
    assertThat(
      layout.overflow.map {
        it::class
      }
    ).containsExactly(TopBarAction.HistoryReload::class, TopBarAction.DetailImpulseCounterSettings::class)
  }

  @Test
  fun `keeps overflow only action in menu when there are two actions`() {
    val layout = layoutTopBarActions(listOf(TopBarAction.ProfileSelect, TopBarAction.HistoryReload), searchIconVisible = false)

    assertThat(layout.icons.map { it::class }).containsExactly(TopBarAction.ProfileSelect::class)
    assertThat(layout.overflow.map { it::class }).containsExactly(TopBarAction.HistoryReload::class)
  }

  @Test
  fun `keeps search and overflow when more than one action is available`() {
    val layout =
      layoutTopBarActions(listOf(TopBarAction.ProfileSelect, TopBarAction.DetailImpulseCounterSettings), searchIconVisible = true)

    assertThat(layout.icons).isEmpty()
    assertThat(
      layout.overflow.map {
        it::class
      }
    ).containsExactly(TopBarAction.ProfileSelect::class, TopBarAction.DetailImpulseCounterSettings::class)
  }

  @Test
  fun `keeps search and icon when there is only one action`() {
    val layout = layoutTopBarActions(listOf(TopBarAction.ProfileSelect), searchIconVisible = true)

    assertThat(layout.icons.map { it::class }).containsExactly(TopBarAction.ProfileSelect::class)
    assertThat(layout.overflow).isEmpty()
  }
}
