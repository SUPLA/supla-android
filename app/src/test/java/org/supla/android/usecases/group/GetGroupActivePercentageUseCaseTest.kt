package org.supla.android.usecases.group

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING

@RunWith(MockitoJUnitRunner::class)
class GetGroupActivePercentageUseCaseTest {

  @InjectMocks
  private lateinit var useCase: GetGroupActivePercentageUseCase

  @Test
  fun `should get active percentage for power switch group`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_POWERSWITCH
      every { totalValue } returns "1|0|1|1"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for roller shutter`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
      every { totalValue } returns "80:1|50:0|100:0|100:0"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for facade blind`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
      every { totalValue } returns "80:50|50:10|100:0|100:0"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(50)
  }

  @Test
  fun `should get active percentage for projector screen`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_PROJECTOR_SCREEN
      every { totalValue } returns "100|50|100|100"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for dimmer`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_DIMMER
      every { totalValue } returns "100|0|100|100"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for rgb`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_RGBLIGHTING
      every { totalValue } returns "20:100|20:0|40:100|10:100"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for dimmer and rgb`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
      every { totalValue } returns "20:100:40|20:0:0|40:100:20|10:100:10"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }
}
