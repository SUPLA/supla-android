package org.supla.android.data.source.remote.rollershutter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

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
    assertThat(value.tilt).isEqualTo(45)
    assertThat(value.bottomPosition).isEqualTo(35)
    assertThat(value.flags).containsExactly(
      SuplaRollerShutterFlag.TILT_IS_SET,
      SuplaRollerShutterFlag.MOTOR_PROBLEM
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
    assertThat(value.tilt).isEqualTo(0)
    assertThat(value.bottomPosition).isEqualTo(0)
    assertThat(value.flags).isEmpty()
  }
}
