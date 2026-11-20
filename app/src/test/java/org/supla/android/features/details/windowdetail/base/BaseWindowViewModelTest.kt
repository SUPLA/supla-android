package org.supla.android.features.details.windowdetail.base

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ObserveChannelGroupByRemoteIdUseCase

class BaseWindowViewModelTest {

  private val viewModel = TestWindowViewModel()

  @Test
  fun `group values with markers should be invalid for empty values`() {
    assertThat(viewModel.withMarkers(emptyList(), hadMarkers = false)).isEqualTo(WindowGroupedValue.Invalid)
    assertThat(viewModel.withMarkers(emptyList(), hadMarkers = true)).isEqualTo(WindowGroupedValue.Invalid)
  }

  @Test
  fun `group values with markers should be invalid when first value is invalid`() {
    assertThat(viewModel.withMarkers(listOf(-1f, 20f), hadMarkers = false)).isEqualTo(WindowGroupedValue.Invalid)
    assertThat(viewModel.withMarkers(listOf(-1f, 20f), hadMarkers = true)).isEqualTo(WindowGroupedValue.Invalid)
  }

  @Test
  fun `group values with markers should use first value when values are within threshold`() {
    assertThat(viewModel.withMarkers(listOf(20f, 25f), hadMarkers = false))
      .isEqualTo(WindowGroupedValue.Similar(20f))
    assertThat(viewModel.withMarkers(listOf(20f, 23f), hadMarkers = true))
      .isEqualTo(WindowGroupedValue.Similar(20f))
  }

  @Test
  fun `group values with markers should accept a spread of five without markers`() {
    assertThat(viewModel.withMarkers(listOf(20f, 25f), hadMarkers = false))
      .isEqualTo(WindowGroupedValue.Similar(20f))
  }

  @Test
  fun `group values with markers should be different for a spread greater than five without markers`() {
    assertThat(viewModel.withMarkers(listOf(25f, 19f), hadMarkers = false))
      .isEqualTo(WindowGroupedValue.Different(min = 19f, max = 25f))
  }

  @Test
  fun `group values with markers should be different for a spread greater than three with markers`() {
    assertThat(viewModel.withMarkers(listOf(20f, 24f), hadMarkers = true))
      .isEqualTo(WindowGroupedValue.Different(min = 20f, max = 24f))
  }

  @Test
  fun `group values with markers should extract values from other types`() {
    assertThat(viewModel.withMarkers(listOf(TestValue(22f), TestValue(28f)), hadMarkers = false) { it.value })
      .isEqualTo(WindowGroupedValue.Different(min = 22f, max = 28f))
  }

  @Test
  fun `group values should be invalid for empty values`() {
    assertThat(viewModel.withoutMarkers(emptyList())).isEqualTo(WindowGroupedValue.Invalid)
  }

  @Test
  fun `group values should be invalid when first value is invalid`() {
    assertThat(viewModel.withoutMarkers(listOf(-1f, 20f))).isEqualTo(WindowGroupedValue.Invalid)
  }

  @Test
  fun `group values should be similar only when every value matches`() {
    assertThat(viewModel.withoutMarkers(listOf(20f, 20f, 20f)))
      .isEqualTo(WindowGroupedValue.Similar(20f))
    assertThat(viewModel.withoutMarkers(listOf(20f, 20.01f)))
      .isEqualTo(WindowGroupedValue.Different(min = 20f, max = 20.01f))
  }

  @Test
  fun `group values should extract values from other types`() {
    assertThat(viewModel.withoutMarkers(listOf(TestValue(22f), TestValue(22f))) { it.value })
      .isEqualTo(WindowGroupedValue.Similar(22f))
  }

  private data class TestValue(val value: Float)

  private class TestWindowViewModel : BaseWindowViewModel<TestWindowViewModelState>(
    executeShadingSystemActionUseCase = mockk(relaxed = true),
    executeSimpleActionUseCase = mockk(relaxed = true),
    callSuplaClientOperationUseCase = mockk(relaxed = true),
    observeChannelWithChildrenUseCase = mockk(relaxed = true),
    observeChannelGroupByRemoteIdUseCase = mockk(relaxed = true),
    getGroupOnlineSummaryUseCase = mockk(relaxed = true),
    preferences = mockk(relaxed = true),
    dateProvider = mockk(relaxed = true),
    suplaClientProvider = mockk(relaxed = true),
    profileRepository = mockk(relaxed = true),
    loginUseCase = mockk(relaxed = true),
    authorizeUseCase = mockk(relaxed = true),
    defaultState = TestWindowViewModelState(),
    threading = mockk(relaxed = true)
  ) {
    fun withMarkers(values: List<Float>, hadMarkers: Boolean): WindowGroupedValue =
      getGroupValues(values, hadMarkers)

    fun <T> withMarkers(values: List<T>, hadMarkers: Boolean, extractor: (T) -> Float): WindowGroupedValue =
      getGroupValues(values, hadMarkers, extractor)

    fun withoutMarkers(values: List<Float>): WindowGroupedValue = getGroupValues(values)

    fun <T> withoutMarkers(values: List<T>, extractor: (T) -> Float): WindowGroupedValue =
      getGroupValues(values, extractor = extractor)

    override fun updatePosition(state: TestWindowViewModelState, position: Float) = state

    override fun stateCopy(
      state: TestWindowViewModelState,
      remoteId: Int?,
      moveStartTime: Long?,
      manualMoving: Boolean,
      showCalibrationDialog: Boolean,
      authorizationDialogState: AuthorizationDialogState?,
      viewStateUpdater: (WindowViewState) -> WindowViewState
    ) = state
      .copy(
        remoteId = remoteId,
        moveStartTime = moveStartTime,
        manualMoving = manualMoving,
        showCalibrationDialog = showCalibrationDialog,
        authorizationDialogState = authorizationDialogState,
        viewState = viewStateUpdater(state.viewState)
      )

    override fun handleChannel(channel: ChannelDataEntity) = Unit

    override fun handleGroup(group: GroupData) = Unit
  }

  private data class TestWindowViewModelState(
    override val remoteId: Int? = null,
    override val windowState: RollerShutterWindowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
    override val viewState: WindowViewState = WindowViewState(),
    override val moveStartTime: Long? = null,
    override val manualMoving: Boolean = false,
    override val showCalibrationDialog: Boolean = false,
    override val authorizationDialogState: AuthorizationDialogState? = null
  ) : BaseWindowViewModelState()
}
