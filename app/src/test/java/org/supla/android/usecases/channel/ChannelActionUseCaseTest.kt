package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.lib.SuplaConst.*
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType

@RunWith(MockitoJUnitRunner::class)
class ChannelActionUseCaseTest {
  @Mock
  private lateinit var channelRepository: ChannelRepository

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
  fun `should not open valve channel when closed manually 1`() {
    testValveException(SUPLA_CHANNELFNC_VALVE_OPENCLOSE)
  }

  @Test
  fun `should not open valve channel when closed manually 2`() {
    testValveException(SUPLA_CHANNELFNC_VALVE_PERCENTAGE)
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
  fun `should turn on power switch`() {
    val channelId = 123

    testOpenClose(channelId, SUPLA_CHANNELFNC_POWERSWITCH, ButtonType.RIGHT, 1)
  }

  @Test
  fun `should turn off light switch`() {
    val channelId = 234

    testOpenClose(channelId, SUPLA_CHANNELFNC_LIGHTSWITCH, ButtonType.LEFT, 0)
  }

  private fun testHighAmperageException(channelFunc: Int) {
    // given
    val channelValue: ChannelValue = mockk()
    every { channelValue.hiValue() } returns false
    every { channelValue.overcurrentRelayOff() } returns true

    val channelId = 123
    val channel: Channel = mockk()
    every { channel.channelId } returns channelId
    every { channel.value } returns channelValue
    every { channel.func } returns channelFunc

    whenever(channelRepository.getChannel(channelId)).thenReturn(channel)

    // when
    val testObserver = useCase(channelId, ButtonType.RIGHT).test()

    // then
    testObserver.assertError(ActionException.ChannelExceedAmperage(channelId))
    verifyZeroInteractions(suplaClientProvider)
  }

  private fun testValveException(channelFunc: Int) {
    // given
    val channelId = 123
    val channel: Channel = mockk()
    every { channel.channelId } returns channelId
    every { channel.remoteId } returns channelId
    every { channel.func } returns channelFunc

    whenever(channelRepository.getChannel(channelId)).thenReturn(channel)

    // when
    val testObserver = useCase(channelId, ButtonType.LEFT).test()

    // then
    testObserver.assertError(ActionException.ChannelClosedManually(channelId))
    verifyZeroInteractions(suplaClientProvider)
  }

  private fun testActionExecution(channelId: Int, channelFunc: Int, buttonType: ButtonType, actionAssertion: (ActionParameters) -> Unit) {
    // given
    val channel: Channel = mockk()
    every { channel.channelId } returns channelId
    every { channel.remoteId } returns channelId
    every { channel.func } returns channelFunc

    whenever(channelRepository.getChannel(channelId)).thenReturn(channel)

    val parametersSlot = slot<ActionParameters>()
    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.executeAction(capture(parametersSlot)) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(channelId, buttonType).test()

    // then
    testObserver.assertComplete()

    actionAssertion(parametersSlot.captured)

    verify(channelRepository).getChannel(channelId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelRepository, suplaClientProvider)
  }

  private fun testOpenClose(channelId: Int, channelFunc: Int, buttonType: ButtonType, openValue: Int) {
    // given
    val channelValue: ChannelValue = mockk()
    every { channelValue.hiValue() } returns false
    every { channelValue.overcurrentRelayOff() } returns false

    val channel: Channel = mockk()
    every { channel.channelId } returns channelId
    every { channel.remoteId } returns channelId
    every { channel.func } returns channelFunc
    every { channel.value } returns channelValue

    whenever(channelRepository.getChannel(channelId)).thenReturn(channel)

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.open(channelId, false, openValue) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(channelId, buttonType).test()

    // then
    testObserver.assertComplete()

    verify(channelRepository).getChannel(channelId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelRepository, suplaClientProvider)
  }
}
