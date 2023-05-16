package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions
import org.junit.Assert.*
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
import org.supla.android.db.ChannelGroup
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType

@RunWith(MockitoJUnitRunner::class)
class GroupActionUseCaseTest {
  @Mock
  private lateinit var channelRepository: ChannelRepository

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMocks
  private lateinit var useCase: GroupActionUseCase

  @Test
  fun `should not open valve channel when closed manually 1`() {
    testValveException(SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE)
  }

  @Test
  fun `should not open valve channel when closed manually 2`() {
    testValveException(SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE)
  }

  @Test
  fun `should turn off RGB lighting`() {
    val channelId = 123

    testActionExecution(channelId, SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING, ButtonType.LEFT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn on dimmer lighting`() {
    val channelId = 234

    testActionExecution(channelId, SuplaConst.SUPLA_CHANNELFNC_DIMMER, ButtonType.RIGHT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.TURN_ON)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn off dimmer with RGB lighting`() {
    val channelId = 123

    testActionExecution(channelId, SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, ButtonType.LEFT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should open roller shutter`() {
    val channelId = 123

    testActionExecution(channelId, SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, ButtonType.RIGHT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.REVEAL)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should close roller shutter`() {
    val channelId = 123

    testActionExecution(channelId, SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, ButtonType.LEFT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.SHUT)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn on power switch`() {
    val channelId = 123

    testOpenClose(channelId, SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH, ButtonType.RIGHT, 1)
  }

  @Test
  fun `should turn off light switch`() {
    val channelId = 234

    testOpenClose(channelId, SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH, ButtonType.LEFT, 0)
  }

  private fun testValveException(channelFunc: Int) {
    // given
    val groupId = 123
    val group: ChannelGroup = mockk()
    every { group.groupId } returns groupId
    every { group.remoteId } returns groupId
    every { group.func } returns channelFunc

    whenever(channelRepository.getChannelGroup(groupId)).thenReturn(group)

    // when
    val testObserver = useCase(groupId, ButtonType.LEFT).test()

    // then
    testObserver.assertError(ActionException.ChannelClosedManually(groupId))
    verifyZeroInteractions(suplaClientProvider)
  }

  private fun testActionExecution(groupId: Int, channelFunc: Int, buttonType: ButtonType, actionAssertion: (ActionParameters) -> Unit) {
    // given
    val group: ChannelGroup = mockk()
    every { group.groupId } returns groupId
    every { group.remoteId } returns groupId
    every { group.func } returns channelFunc

    whenever(channelRepository.getChannelGroup(groupId)).thenReturn(group)

    val parametersSlot = slot<ActionParameters>()
    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.executeAction(capture(parametersSlot)) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(groupId, buttonType).test()

    // then
    testObserver.assertComplete()

    actionAssertion(parametersSlot.captured)

    verify(channelRepository).getChannelGroup(groupId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelRepository, suplaClientProvider)
  }

  private fun testOpenClose(groupId: Int, channelFunc: Int, buttonType: ButtonType, openValue: Int) {
    // given
    val group: ChannelGroup = mockk()
    every { group.groupId } returns groupId
    every { group.remoteId } returns groupId
    every { group.func } returns channelFunc

    whenever(channelRepository.getChannelGroup(groupId)).thenReturn(group)

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.open(groupId, true, openValue) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(groupId, buttonType).test()

    // then
    testObserver.assertComplete()

    verify(channelRepository).getChannelGroup(groupId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelRepository, suplaClientProvider)
  }
}
