package org.supla.android.lib
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

import org.junit.Assert
import org.junit.Test

class SuplaSceneStateTest {
  @Test
  fun `The scene has not started`() {
    val state = SuplaSceneState(
      155,
      0,
      0,
      415,
      "iPhone Elon",
      true
    )
    Assert.assertEquals(155, state.sceneId)
    Assert.assertNull(state.startedAt)
    Assert.assertNull(state.estimatedEndDate)
    Assert.assertFalse(state.isDuringExecution)
    Assert.assertEquals(0, state.millisecondsLeft)
    Assert.assertEquals(415, state.initiatorId)
    Assert.assertEquals("iPhone Elon", state.initiatorName)
    Assert.assertTrue(state.isEol)
  }

  @Test
  fun `The scene is during execution`() {
    val state1 = SuplaSceneState(
      156,
      100,
      10,
      0,
      "",
      false
    )
    Assert.assertEquals(156, state1.sceneId)
    Assert.assertTrue(state1.isDuringExecution)
    Assert.assertFalse(state1.isEol)

    val state2 = SuplaSceneState(
      156,
      0,
      10,
      0,
      "",
      false
    )
    Assert.assertTrue(state2.isDuringExecution)
  }

  @Test
  fun `The scene is done`() {
    val state = SuplaSceneState(
      156,
      200,
      0,
      0,
      "",
      false
    )
    Assert.assertEquals(156, state.sceneId)
    Assert.assertNotNull(state.startedAt)
    Assert.assertNotNull(state.estimatedEndDate)
    Assert.assertFalse(state.isDuringExecution)
  }
}
