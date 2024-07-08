package org.supla.android.data.source.local.entity.custom
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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity

class ChannelWithChildrenTest {

  @Test
  fun `should flatten the children tree`() {
    // given
    val child1: ChannelChildEntity = mockChildEntity()
    val child2: ChannelChildEntity = mockChildEntity()
    val child3: ChannelChildEntity = mockChildEntity(listOf(child1, child2))
    val child4: ChannelChildEntity = mockChildEntity()
    val child5: ChannelChildEntity = mockChildEntity(listOf(child3, child4))
    val child6: ChannelChildEntity = mockChildEntity()
    val child7: ChannelChildEntity = mockChildEntity(listOf(child6))
    val child8: ChannelChildEntity = mockChildEntity()
    val child9: ChannelChildEntity = mockChildEntity(listOf(child5, child7))

    val channel = ChannelWithChildren(
      channel = mockk(),
      children = listOf(child8, child9)
    )

    // when
    val result = channel.allDescendantFlat

    // then
    assertThat(result)
      .containsExactly(child8, child1, child2, child3, child4, child5, child6, child7, child9)
  }

  private fun mockChildEntity(children: List<ChannelChildEntity> = emptyList()): ChannelChildEntity =
    mockk {
      every { this@mockk.children } returns children
    }
}
