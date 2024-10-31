package org.supla.core.shared.usecase.channel
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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.lists.IssueIcon

class GetChannelBatteryIconUseCaseTest {

  @InjectMockKs
  private lateinit var useCase: GetChannelBatteryIconUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get battery not used`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns false
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.BatteryNotUsed)
  }

  @Test
  fun `should get battery level 100 percent`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns true
          every { level } returns 87
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.Battery100)
  }

  @Test
  fun `should get battery level 75 percent`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns true
          every { level } returns 72
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.Battery75)
  }

  @Test
  fun `should get battery level 50 percent`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns true
          every { level } returns 48
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.Battery50)
  }

  @Test
  fun `should get battery level 25 percent`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns true
          every { level } returns 18
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.Battery25)
  }

  @Test
  fun `should get battery low battery`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns true
          every { level } returns 5
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.Battery0)
  }

  @Test
  fun `should get battery powered icon`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { batteryInfo } returns mockk {
          every { batteryPowered } returns true
          every { level } returns null
        }
      }
    }

    // when
    val icon = useCase.invoke(channelWithChildren)

    // then
    assertThat(icon).isEqualTo(IssueIcon.Battery)
  }
}
