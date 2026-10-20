package org.supla.core.shared.data.model.function.digiglass
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
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus

class DigiglassValueTest {
  @Test
  fun `check if value is created when array has wrong length`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(1)

    // when
    val value = DigiglassValue.from(status, bytes)

    // then
    assertThat(value.flags).isEmpty()
    assertThat(value.mask).isEqualTo(0)
    assertThat(value.sectionCount).isEqualTo(0)
  }

  @Test
  fun `check value with to long operation flag`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(SuplaDigiglassFlag.TOO_LONG_OPERATION.value.toByte(), 7, 31, 0, 0, 0, 0, 0)

    // when
    val value = DigiglassValue.from(status, bytes)

    // then
    assertThat(value.flags).containsExactly(SuplaDigiglassFlag.TOO_LONG_OPERATION)
    assertThat(value.mask).isEqualTo(31)
    assertThat(value.sectionCount).isEqualTo(7)
    assertThat(value.isAnySectionTransparent).isTrue
  }

  @Test
  fun `check value with regeneration in progress flag`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(SuplaDigiglassFlag.PLANNED_REGENERATION_IN_PROGRESS.value.toByte(), 5, 0, 0, 0, 0, 0, 0)

    // when
    val value = DigiglassValue.from(status, bytes)

    // then
    assertThat(value.flags).containsExactly(SuplaDigiglassFlag.PLANNED_REGENERATION_IN_PROGRESS)
    assertThat(value.mask).isEqualTo(0)
    assertThat(value.sectionCount).isEqualTo(5)
    assertThat(value.isAnySectionTransparent).isFalse
  }

  @Test
  fun `check value with two flags`() {
    // given
    val flags = SuplaDigiglassFlag.PLANNED_REGENERATION_IN_PROGRESS.value or SuplaDigiglassFlag.TOO_LONG_OPERATION.value
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(flags.toByte(), 5, 0, 0, 0, 0, 0, 0)

    // when
    val value = DigiglassValue.from(status, bytes)

    // then
    assertThat(value.flags).containsExactly(SuplaDigiglassFlag.TOO_LONG_OPERATION, SuplaDigiglassFlag.PLANNED_REGENERATION_IN_PROGRESS)
    assertThat(value.mask).isEqualTo(0)
    assertThat(value.sectionCount).isEqualTo(5)
    assertThat(value.isAnySectionTransparent).isFalse
  }
}
