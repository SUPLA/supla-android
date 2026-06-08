package org.supla.android.data.model.settings
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
import java.time.ZonedDateTime

class ListValueAggregationTest {

  @Test
  fun `should return null for no aggregation`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val result = ListValueAggregation.NO_AGGREGATION.aggregationStartDate(now)

    // then
    assertThat(result).isNull()
  }

  @Test
  fun `should subtract relative ranges from current timestamp`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val last24Hours = ListValueAggregation.LAST_24_HOURS.aggregationStartDate(now)
    val last7Days = ListValueAggregation.LAST_7_DAYS.aggregationStartDate(now)
    val last30Days = ListValueAggregation.LAST_30_DAYS.aggregationStartDate(now)
    val last90Days = ListValueAggregation.LAST_90_DAYS.aggregationStartDate(now)
    val last365Days = ListValueAggregation.LAST_365_DAYS.aggregationStartDate(now)

    // then
    assertThat(last24Hours).isEqualTo(ZonedDateTime.parse("2023-10-11T11:00Z"))
    assertThat(last7Days).isEqualTo(ZonedDateTime.parse("2023-10-06T00:00Z"))
    assertThat(last30Days).isEqualTo(ZonedDateTime.parse("2023-09-13T00:00Z"))
    assertThat(last90Days).isEqualTo(ZonedDateTime.parse("2023-07-15T00:00Z"))
    assertThat(last365Days).isEqualTo(ZonedDateTime.parse("2022-10-13T00:00Z"))
  }

  @Test
  fun `should round down to start of current hour`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val result = ListValueAggregation.CURRENT_HOUR.aggregationStartDate(now)

    // then
    assertThat(result).isEqualTo(ZonedDateTime.parse("2023-10-12T10:00:00Z"))
  }

  @Test
  fun `should round down to start of current day`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val result = ListValueAggregation.CURRENT_DAY.aggregationStartDate(now)

    // then
    assertThat(result).isEqualTo(ZonedDateTime.parse("2023-10-12T00:00:00Z"))
  }

  @Test
  fun `should round down to start of current week on monday`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val result = ListValueAggregation.CURRENT_WEEK.aggregationStartDate(now)

    // then
    assertThat(result).isEqualTo(ZonedDateTime.parse("2023-10-09T00:00:00Z"))
  }

  @Test
  fun `should keep same day when current week already starts on monday`() {
    // given
    val now = ZonedDateTime.parse("2023-10-09T10:30:45.123Z")

    // when
    val result = ListValueAggregation.CURRENT_WEEK.aggregationStartDate(now)

    // then
    assertThat(result).isEqualTo(ZonedDateTime.parse("2023-10-09T00:00:00Z"))
  }

  @Test
  fun `should round down to start of current month`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val result = ListValueAggregation.CURRENT_MONTH.aggregationStartDate(now)

    // then
    assertThat(result).isEqualTo(ZonedDateTime.parse("2023-10-01T00:00:00Z"))
  }

  @Test
  fun `should round down to start of current year`() {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:45.123Z")

    // when
    val result = ListValueAggregation.CURRENT_YEAR.aggregationStartDate(now)

    // then
    assertThat(result).isEqualTo(ZonedDateTime.parse("2023-01-01T00:00:00Z"))
  }
}
