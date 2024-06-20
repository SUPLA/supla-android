package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.relay.SuplaRelayFlag
import org.supla.android.data.source.remote.valve.SuplaValveFlag
import org.supla.android.data.source.remote.valve.ValveValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType

@RunWith(MockitoJUnitRunner::class)
class ChannelActionUseCaseTest {
  @Mock
  private lateinit var channelRepository: RoomChannelRepository

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMocks
  private lateinit var useCase: ChannelActionUseCase

  @Test
  fun `should not perform action when power switch channel turned off because of high amperage`() {
    testHighAmperageException(SUPLA_CHANNELFNC_POWERSWITCH)
  }

  @Test
  fun `should not perform action when light switch channel turned off because of high amperage`() {
    testHighAmperageException(SUPLA_CHANNELFNC_LIGHTSWITCH)
  }

  @Test
  fun `should not perform action when stair case timer channel turned off because of high amperage`() {
    testHighAmperageException(SUPLA_CHANNELFNC_STAIRCASETIMER)
  }

  @Test
  fun `should not open valve channel when closed and flooding`() {
    testValveException(SUPLA_CHANNELFNC_VALVE_OPENCLOSE) {
      every { it.flags } returns listOf(SuplaValveFlag.FLOODING)
    }
  }

  @Test
  fun `should not open valve channel when closed and closed manually`() {
    testValveException(SUPLA_CHANNELFNC_VALVE_PERCENTAGE) {
      every { it.flags } returns listOf(SuplaValveFlag.FLOODING, SuplaValveFlag.MANUALLY_CLOSED)
    }
  }

  @Test
  fun `should turn off RGB lighting`() {
    val channelId = 123

    testActionExecution(channelId, SUPLA_CHANNELFNC_RGBLIGHTING, ButtonType.LEFT) {
      assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn on dimmer lighting`() {
    val channelId = 234

    testActionExecution(channelId, SUPLA_CHANNELFNC_DIMMER, ButtonType.RIGHT) {
      assertThat(it.action).isEqualTo(ActionId.TURN_ON)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn off dimmer with RGB lighting`() {
    val channelId = 123

    testActionExecution(channelId, SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, ButtonType.LEFT) {
      assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should open roller shutter`() {
    val channelId = 123

    testActionExecution(channelId, SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, ButtonType.RIGHT) {
      assertThat(it.action).isEqualTo(ActionId.REVEAL)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should close roller shutter`() {
    val channelId = 123

    testActionExecution(channelId, SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, ButtonType.LEFT) {
      assertThat(it.action).isEqualTo(ActionId.SHUT)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should open or stop roller shutter`() {
    val channelId = 123

    testActionExecution(
      channelId,
      SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      ButtonType.RIGHT,
      SuplaChannelFlag.RS_SBS_AND_STOP_ACTIONS
    ) {
      assertThat(it.action).isEqualTo(ActionId.UP_OR_STOP)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should close or stop roller shutter`() {
    val channelId = 123

    testActionExecution(
      channelId,
      SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
      ButtonType.LEFT,
      SuplaChannelFlag.RS_SBS_AND_STOP_ACTIONS
    ) {
      assertThat(it.action).isEqualTo(ActionId.DOWN_OR_STOP)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn on power switch`() {
    val channelId = 123

    testOpenClose(channelId, SUPLA_CHANNELFNC_POWERSWITCH, ButtonType.RIGHT, 1)
  }

  @Test
  fun `should turn off light switch`() {
    val channelId = 234

    testOpenClose(channelId, SUPLA_CHANNELFNC_LIGHTSWITCH, ButtonType.LEFT, 0)
  }

  @Test
  fun `should turn on home plus thermostat`() {
    val channelId = 234

    testActionExecution(
      channelId,
      SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS,
      ButtonType.RIGHT
    ) {
      assertThat(it.action).isEqualTo(ActionId.TURN_ON)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn off home plus thermostat`() {
    val channelId = 234

    testActionExecution(
      channelId,
      SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS,
      ButtonType.LEFT
    ) {
      assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      assertThat(it.subjectType).isEqualTo(SubjectType.CHANNEL)
      assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  private fun testHighAmperageException(channelFunc: Int) {
    // given
    val channelValue: ChannelValueEntity = mockk()
    every { channelValue.asRelayValue() } returns mockk {
      every { flags } returns listOf(SuplaRelayFlag.OVERCURRENT_RELAY_OFF)
      every { on } returns false
    }

    val channelId = 123
    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns channelId
    every { channel.channelValueEntity } returns channelValue
    every { channel.function } returns channelFunc

    whenever(channelRepository.findChannelDataEntity(channelId)).thenReturn(Maybe.just(channel))

    // when
    val testObserver = useCase(channelId, ButtonType.RIGHT).test()

    // then
    testObserver.assertError(ActionException.ChannelExceedAmperage(channelId))
    verifyZeroInteractions(suplaClientProvider)
  }

  private fun testValveException(channelFunc: Int, channelValueSetup: (ValveValue) -> Unit) {
    // given
    val channelId = 123
    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns channelId
    every { channel.function } returns channelFunc

    val valveValue: ValveValue = mockk()
    every { valveValue.isClosed() } returns true
    channelValueSetup(valveValue)

    val channelValue: ChannelValueEntity = mockk()
    every { channelValue.asValveValue() } returns valveValue
    every { channel.channelValueEntity } returns channelValue

    whenever(channelRepository.findChannelDataEntity(channelId)).thenReturn(Maybe.just(channel))

    // when
    val testObserver = useCase(channelId, ButtonType.LEFT).test()

    // then
    testObserver.assertError(ActionException.ChannelClosedManually(channelId))
    verifyZeroInteractions(suplaClientProvider)
  }

  private fun testActionExecution(
    channelId: Int,
    channelFunc: Int,
    buttonType: ButtonType,
    flag: SuplaChannelFlag? = null,
    actionAssertion: (ActionParameters) -> Unit
  ) {
    // given
    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns channelId
    every { channel.function } returns channelFunc
    every { channel.flags } returns (flag?.rawValue ?: 0L)

    whenever(channelRepository.findChannelDataEntity(channelId)).thenReturn(Maybe.just(channel))

    val parametersSlot = slot<ActionParameters>()
    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.executeAction(capture(parametersSlot)) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(channelId, buttonType).test()

    // then
    testObserver.assertComplete()

    actionAssertion(parametersSlot.captured)

    verify(channelRepository).findChannelDataEntity(channelId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelRepository, suplaClientProvider)
  }

  private fun testOpenClose(channelId: Int, channelFunc: Int, buttonType: ButtonType, openValue: Int) {
    // given
    val channelValue: ChannelValueEntity = mockk()
    every { channelValue.asRelayValue() } returns mockk {
      every { flags } returns emptyList()
      every { on } returns false
    }

    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns channelId
    every { channel.function } returns channelFunc
    every { channel.channelValueEntity } returns channelValue

    whenever(channelRepository.findChannelDataEntity(channelId)).thenReturn(Maybe.just(channel))

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.open(channelId, false, openValue) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(channelId, buttonType).test()

    // then
    testObserver.assertComplete()

    verify(channelRepository).findChannelDataEntity(channelId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelRepository, suplaClientProvider)
  }
}
