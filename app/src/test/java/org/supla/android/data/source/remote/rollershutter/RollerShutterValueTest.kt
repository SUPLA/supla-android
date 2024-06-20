package org.supla.android.data.source.remote.rollershutter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.remote.shadingsystem.SuplaShadingSystemFlag

@RunWith(MockitoJUnitRunner::class)
class RollerShutterValueTest {

  @Test
  fun shouldCreateFromByteArray() {
    // given
    val online = true
    val bytes = byteArrayOf(0x37, 0x2D, 0x23, 0x9, 0x0) // 55, 45, 35, 9, 0

    // when
    val value = RollerShutterValue.from(online, bytes)

    // then
    assertThat(value.online).isEqualTo(true)
    assertThat(value.position).isEqualTo(55)
    assertThat(value.bottomPosition).isEqualTo(35)
    assertThat(value.flags).containsExactly(
      SuplaShadingSystemFlag.TILT_IS_SET,
      SuplaShadingSystemFlag.MOTOR_PROBLEM
    )
  }

  @Test
  fun shouldCreateDefaultValueWhenByteArrayToShort() {
    // given
    val online = false
    val bytes = byteArrayOf(0x37, 0x2D)

    // when
    val value = RollerShutterValue.from(online, bytes)

    // then
    assertThat(value.online).isEqualTo(false)
    assertThat(value.position).isEqualTo(-1)
    assertThat(value.bottomPosition).isEqualTo(0)
    assertThat(value.flags).isEmpty()
  }

  @Test
  fun `should get invalid value when out of range`() {
    // given
    val online = false
    val bytes = byteArrayOf(-50, 0, 127, 2, 0)

    // when
    val value = RollerShutterValue.from(online, bytes)

    // when
    assertThat(value.online).isFalse()
    assertThat(value.position).isEqualTo(-1)
    assertThat(value.bottomPosition).isEqualTo(100)
    assertThat(value.flags).containsExactly(SuplaShadingSystemFlag.CALIBRATION_FAILED)

    assertThat(value.hasValidPosition()).isFalse()

    assertThat(value.alwaysValidPosition).isEqualTo(0)
  }
}
