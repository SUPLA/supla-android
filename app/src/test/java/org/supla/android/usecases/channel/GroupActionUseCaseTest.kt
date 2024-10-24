package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.core.shared.data.SuplaChannelFunction

@RunWith(MockitoJUnitRunner::class)
class GroupActionUseCaseTest {
  @Mock
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMocks
  private lateinit var useCase: GroupActionUseCase

  @Test
  fun `should turn off RGB lighting`() {
    val channelId = 123

    testActionExecution(channelId, SuplaChannelFunction.RGB_LIGHTING, ButtonType.LEFT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn on dimmer lighting`() {
    val channelId = 234

    testActionExecution(channelId, SuplaChannelFunction.DIMMER, ButtonType.RIGHT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.TURN_ON)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn off dimmer with RGB lighting`() {
    val channelId = 123

    testActionExecution(channelId, SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING, ButtonType.LEFT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.TURN_OFF)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should open roller shutter`() {
    val channelId = 123

    testActionExecution(channelId, SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER, ButtonType.RIGHT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.REVEAL)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should close roller shutter`() {
    val channelId = 123

    testActionExecution(channelId, SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW, ButtonType.LEFT) {
      Assertions.assertThat(it.action).isEqualTo(ActionId.SHUT)
      Assertions.assertThat(it.subjectType).isEqualTo(SubjectType.GROUP)
      Assertions.assertThat(it.subjectId).isEqualTo(channelId)
    }
  }

  @Test
  fun `should turn on power switch`() {
    val channelId = 123

    testOpenClose(channelId, SuplaChannelFunction.POWER_SWITCH, ButtonType.RIGHT, 1)
  }

  @Test
  fun `should turn off light switch`() {
    val channelId = 234

    testOpenClose(channelId, SuplaChannelFunction.LIGHTSWITCH, ButtonType.LEFT, 0)
  }

  private fun testActionExecution(
    groupId: Int,
    channelFunc: SuplaChannelFunction,
    buttonType: ButtonType,
    actionAssertion: (ActionParameters) -> Unit
  ) {
    // given
    val group: ChannelGroupDataEntity = mockk()
    every { group.remoteId } returns groupId
    every { group.function } returns channelFunc
    every { group.flags } returns 0

    whenever(channelGroupRepository.findGroupDataEntity(groupId)).thenReturn(Maybe.just(group))

    val parametersSlot = slot<ActionParameters>()
    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.executeAction(capture(parametersSlot)) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(groupId, buttonType).test()

    // then
    testObserver.assertComplete()

    actionAssertion(parametersSlot.captured)

    verify(channelGroupRepository).findGroupDataEntity(groupId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelGroupRepository, suplaClientProvider)
  }

  private fun testOpenClose(groupId: Int, channelFunc: SuplaChannelFunction, buttonType: ButtonType, openValue: Int) {
    // given
    val group: ChannelGroupDataEntity = mockk()
    every { group.remoteId } returns groupId
    every { group.function } returns channelFunc

    whenever(channelGroupRepository.findGroupDataEntity(groupId)).thenReturn(Maybe.just(group))

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.open(groupId, true, openValue) } returns true

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(groupId, buttonType).test()

    // then
    testObserver.assertComplete()

    verify(channelGroupRepository).findGroupDataEntity(groupId)
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(channelGroupRepository, suplaClientProvider)
  }
}
