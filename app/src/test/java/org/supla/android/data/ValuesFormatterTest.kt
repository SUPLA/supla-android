package org.supla.android.data
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
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.Preferences
import org.supla.android.data.source.local.calendar.Hour

@RunWith(MockitoJUnitRunner::class)
class ValuesFormatterTest {

  @Mock
  private lateinit var preferences: Preferences

  @InjectMocks
  private lateinit var formatter: ValuesFormatter

  @Test
  fun `should format hour and minute`() {
    // given
    val hour = 3
    val minute = 10

    // when
    val text = formatter.getTimeString(hour, minute)

    // then
    assertThat(text).isEqualTo("03:10")
  }

  @Test
  fun `should format hour, minute and second`() {
    // given
    val hour = 12
    val minute = 3
    val second = 5

    // when
    val text = formatter.getTimeString(hour, minute, second)

    // then
    assertThat(text).isEqualTo("12:03:05")
  }

  @Test
  fun `should format hour object`() {
    // given
    val hour = Hour(14, 15)

    // when
    val text = formatter.getHourString(hour)

    // then
    assertThat(text).isEqualTo("14:15")
  }
}
