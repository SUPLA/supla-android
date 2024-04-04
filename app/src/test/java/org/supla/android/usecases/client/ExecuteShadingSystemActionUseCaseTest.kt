package org.supla.android.usecases.client

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ShadingSystemActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper

@RunWith(MockitoJUnitRunner::class)
class ExecuteShadingSystemActionUseCaseTest {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var vibrationHelper: VibrationHelper

  @InjectMocks
  private lateinit var useCase: ExecuteShadingSystemActionUseCase

  @Test
  fun `should execute action and vibrate`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val percentage = 23f

    val suplaClient: SuplaClientApi = mock()
    whenever(suplaClient.executeAction(any())).thenReturn(true)

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val observer = useCase.invoke(actionId, type, remoteId, percentage).test()

    // then
    observer.assertComplete()

    verify(suplaClient).executeAction(
      argThat { parameters ->
        parameters.action == actionId &&
          parameters.subjectType == type &&
          parameters.subjectId == remoteId &&
          (parameters as ShadingSystemActionParameters).percentage.compareTo(23) == 0
      }
    )
    verify(suplaClientProvider).provide()
    verify(vibrationHelper).vibrate()
    verifyNoMoreInteractions(suplaClient, suplaClientProvider, vibrationHelper)
  }

  @Test
  fun `should not vibrate when action is not executed successfully`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val percentage = 23f

    val suplaClient: SuplaClientApi = mock()
    whenever(suplaClient.executeAction(any())).thenReturn(false)

    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val observer = useCase.invoke(actionId, type, remoteId, percentage).test()

    // then
    observer.assertComplete()

    verify(suplaClient).executeAction(
      argThat { parameters ->
        parameters.action == actionId &&
          parameters.subjectType == type &&
          parameters.subjectId == remoteId &&
          (parameters as ShadingSystemActionParameters).percentage.compareTo(23) == 0
      }
    )
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(suplaClient, suplaClientProvider)
    verifyZeroInteractions(vibrationHelper)
  }

  @Test
  fun `should not fail when no supla client is provided`() {
    // given
    val actionId = ActionId.TURN_ON
    val type = SubjectType.CHANNEL
    val remoteId = 123
    val percentage = 23f

    whenever(suplaClientProvider.provide()).thenReturn(null)

    // when
    val observer = useCase.invoke(actionId, type, remoteId, percentage).test()

    // then
    observer.assertComplete()

    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(suplaClientProvider)
    verifyZeroInteractions(vibrationHelper)
  }
}
