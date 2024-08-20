package org.supla.android.data.model.chart
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
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.extensions.date
import java.util.Calendar

@RunWith(MockitoJUnitRunner::class)
class DateRangeTest {

  @Test
  fun `should count days (different months)`() {
    // given
    val range = DateRange(
      start = date(2023, 10, 16),
      end = date(2023, 11, 8)
    )

    // when
    val daysCount = range.daysCount

    // then
    assertThat(daysCount).isEqualTo(22)
    assertThat(range.minAggregation).isEqualTo(ChartDataAggregation.MINUTES)
    assertThat(range.maxAggregation(ChartRange.MONTH)).isEqualTo(ChartDataAggregation.DAYS)
  }

  @Test
  fun `should count days (same months)`() {
    // given
    val range = DateRange(
      start = date(2023, 10, 16),
      end = date(2023, 10, 15)
    )

    // when
    val daysCount = range.daysCount

    // then
    assertThat(daysCount).isEqualTo(1)
    assertThat(range.minAggregation).isEqualTo(ChartDataAggregation.MINUTES)
    assertThat(range.maxAggregation(ChartRange.MONTH)).isEqualTo(ChartDataAggregation.HOURS)
  }

  @Test
  fun `should count days (same days)`() {
    // given
    val range = DateRange(
      start = date(2023, 10, 16),
      end = date(2023, 10, 16)
    )

    // when
    val daysCount = range.daysCount

    // then
    assertThat(daysCount).isEqualTo(0)
    assertThat(range.minAggregation).isEqualTo(ChartDataAggregation.MINUTES)
    assertThat(range.maxAggregation(ChartRange.DAY)).isEqualTo(ChartDataAggregation.HOURS)
  }

  @Test
  fun `should count days (more than month)`() {
    // given
    val range = DateRange(
      start = date(2023, 10, 16),
      end = date(2023, 11, 26)
    )

    // when
    val daysCount = range.daysCount

    // then
    assertThat(daysCount).isEqualTo(40)
    assertThat(range.minAggregation).isEqualTo(ChartDataAggregation.HOURS)
    assertThat(range.maxAggregation(ChartRange.QUARTER)).isEqualTo(ChartDataAggregation.MONTHS)
  }

  @Test
  fun `should count days (more than quarter)`() {
    // given
    val range = DateRange(
      start = date(2023, 8, 16),
      end = date(2023, 11, 26)
    )

    // when
    val daysCount = range.daysCount

    // then
    assertThat(daysCount).isEqualTo(101)
    assertThat(range.minAggregation).isEqualTo(ChartDataAggregation.DAYS)
    assertThat(range.maxAggregation(ChartRange.QUARTER)).isEqualTo(ChartDataAggregation.MONTHS)
  }

  @Test
  fun `should count days (more than year)`() {
    // given
    val range = DateRange(
      start = date(2023, 4, 16),
      end = date(2024, 11, 26)
    )

    // when
    val daysCount = range.daysCount

    // then
    assertThat(daysCount).isEqualTo(590)
    assertThat(range.minAggregation).isEqualTo(ChartDataAggregation.DAYS)
    assertThat(range.maxAggregation(ChartRange.YEAR)).isEqualTo(ChartDataAggregation.YEARS)
  }

  @Test
  fun `should shift by range (day)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.OCTOBER, 15),
      end = date(2023, Calendar.OCTOBER, 15, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.DAY, true)

    // then
    assertThat(newRange.start).isEqualTo("2023-10-16 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2023-10-16 23:59:59.999")
  }

  @Test
  fun `should shift by range (week)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.OCTOBER, 9),
      end = date(2023, Calendar.OCTOBER, 15, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.WEEK, false)

    // then
    assertThat(newRange.start).isEqualTo("2023-10-02 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2023-10-08 23:59:59.999")
  }

  @Test
  fun `should shift by range (month forward)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.OCTOBER, 1),
      end = date(2023, Calendar.OCTOBER, 31, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.MONTH, true)

    // then
    assertThat(newRange.start).isEqualTo("2023-11-01 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2023-11-30 23:59:59.999")
  }

  @Test
  fun `should shift by range (month backward)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.OCTOBER, 1),
      end = date(2023, Calendar.OCTOBER, 31, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.MONTH, false)

    // then
    assertThat(newRange.start).isEqualTo("2023-09-01 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2023-09-30 23:59:59.999")
  }

  @Test
  fun `should shift by range (quarter forward)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.OCTOBER, 1),
      end = date(2023, Calendar.DECEMBER, 31, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.QUARTER, true)

    // then
    assertThat(newRange.start).isEqualTo("2024-01-01 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2024-03-31 23:59:59.999")
  }

  @Test
  fun `should shift by range (quarter backward)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.OCTOBER, 1),
      end = date(2023, Calendar.DECEMBER, 31, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.QUARTER, false)

    // then
    assertThat(newRange.start).isEqualTo("2023-07-01 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2023-09-30 23:59:59.999")
  }

  @Test
  fun `should shift by range (year forward)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.JANUARY, 1),
      end = date(2023, Calendar.DECEMBER, 31, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.YEAR, true)

    // then
    assertThat(newRange.start).isEqualTo("2024-01-01 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2024-12-31 23:59:59.999")
  }

  @Test
  fun `should shift by range (year backward)`() {
    // given
    val range = DateRange(
      start = date(2023, Calendar.JANUARY, 1),
      end = date(2023, Calendar.DECEMBER, 31, 23, 59, 59)
    )

    // when
    val newRange = range.shift(ChartRange.YEAR, false)

    // then
    assertThat(newRange.start).isEqualTo("2022-01-01 00:00:00.000")
    assertThat(newRange.end).isEqualTo("2022-12-31 23:59:59.999")
  }
}
