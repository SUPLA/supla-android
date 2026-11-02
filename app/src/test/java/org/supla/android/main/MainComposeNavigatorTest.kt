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

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.features.lockscreen.UnlockAction

class MainComposeNavigatorTest {

  private val navigator = MainComposeNavigator()

  @Test
  fun `should not add another unlock app destination`() {
    val backStack = NavBackStack<NavKey>(MainRoute.Status)
    navigator.bind(backStack)

    navigator.navigateToUnlockApp(UnlockAction.AuthorizeApplication)
    navigator.navigateToUnlockApp(UnlockAction.AuthorizeApplication)

    assertThat(backStack).containsExactly(
      MainRoute.Status,
      MainRoute.UnlockApp(UnlockAction.AuthorizeApplication)
    )
  }

  @Test
  fun `should move existing unlock app destination to the top`() {
    val backStack = NavBackStack<NavKey>(
      MainRoute.Status,
      MainRoute.UnlockApp(UnlockAction.AuthorizeApplication),
      MainRoute.Settings
    )
    navigator.bind(backStack)

    navigator.navigateToUnlockApp(UnlockAction.AuthorizeApplication)

    assertThat(backStack).containsExactly(
      MainRoute.Status,
      MainRoute.Settings,
      MainRoute.UnlockApp(UnlockAction.AuthorizeApplication)
    )
  }
}
