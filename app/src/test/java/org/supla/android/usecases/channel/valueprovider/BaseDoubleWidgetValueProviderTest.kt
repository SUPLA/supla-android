package org.supla.android.usecases.channel.valueprovider
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
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.ValueType

abstract class BaseDoubleWidgetValueProviderTest<T : DefaultDoubleValueProvider> {

  abstract val unknownValue: Double

  abstract var valueProvider: T

  @Test
  fun `should get unknown value when no data`() {
    // given
    val channelData: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { channelValueEntity } returns mockk {
          every { getValueAsByteArray() } returns ByteArray(0)
        }
      }
    }

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    if (unknownValue.isNaN()) {
      assertThat(value).isNaN()
    } else {
      assertThat(value).isEqualTo(unknownValue)
    }
  }

  @Test
  fun `should get value`() {
    // given
    val valueBytes = listOf(
      (-31).toByte(),
      (122).toByte(),
      (20).toByte(),
      (-82).toByte(),
      (71).toByte(),
      (-31).toByte(),
      (-54).toByte(),
      (63).toByte()
    )
    val channelData: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { channelValueEntity } returns mockk {
          every { getValueAsByteArray() } returns ByteArray(8) { position -> valueBytes[position] }
        }
      }
    }

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    assertThat(value).isEqualTo(0.21)
  }

  @Test
  fun `should get unknown value if there are wrong data`() {
    // given
    val valueBytes = listOf(
      (-31).toByte(),
      (122).toByte(),
      (20).toByte(),
      (-82).toByte(),
      (71).toByte(),
      (-31).toByte(),
      (-54).toByte(),
    )
    val channelData: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { channelValueEntity } returns mockk {
          every { getValueAsByteArray() } returns ByteArray(7) { position -> valueBytes[position] }
        }
      }
    }

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    if (unknownValue.isNaN()) {
      assertThat(value).isNaN()
    } else {
      assertThat(value).isEqualTo(unknownValue)
    }
  }
}
