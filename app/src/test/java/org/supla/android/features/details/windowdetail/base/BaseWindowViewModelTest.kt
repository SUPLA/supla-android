package org.supla.android.features.details.windowdetail.base

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.ui.dialogs.AuthorizationDialogState

class BaseWindowViewModelTest {

  private val viewModel = TestWindowViewModel()

  @Test
  fun `group values should be invalid for empty input`() {
    assertThat(viewModel.withMarkers(emptyList())).isEqualTo(WindowGroupedValue.Invalid)
    assertThat(viewModel.withoutMarkers(emptyList())).isEqualTo(WindowGroupedValue.Invalid)
  }

  @Test
  fun `group values should be invalid when the first value is invalid`() {
    assertThat(viewModel.withMarkers(listOf(-1f, 20f))).isEqualTo(WindowGroupedValue.Invalid)
    assertThat(viewModel.withoutMarkers(listOf(-1f, 20f))).isEqualTo(WindowGroupedValue.Invalid)
  }

  @Test
  fun `default threshold treats a spread equal to its limit as similar`() {
    assertThat(viewModel.withoutMarkers(listOf(20f, 25f)))
      .isEqualTo(WindowGroupedValue.Similar(20f))
    assertThat(viewModel.withMarkers(listOf(20f, 23f)))
      .isEqualTo(WindowGroupedValue.Similar(20f))
  }

  @Test
  fun `default threshold treats a spread above its limit as different`() {
    assertThat(viewModel.withoutMarkers(listOf(25f, 19f)))
      .isEqualTo(WindowGroupedValue.Different(min = 19f, max = 25f))
    assertThat(viewModel.withMarkers(listOf(20f, 24f)))
      .isEqualTo(WindowGroupedValue.Different(min = 20f, max = 24f))
  }

  @Test
  fun `slat threshold treats spreads at each limit as similar`() {
    assertThat(viewModel.withMarkers(listOf(20f, 70f), BaseWindowViewModel.GroupThreshold.Slat))
      .isEqualTo(WindowGroupedValue.Similar(20f))
    assertThat(viewModel.withoutMarkers(listOf(20f, 80f), BaseWindowViewModel.GroupThreshold.Slat))
      .isEqualTo(WindowGroupedValue.Similar(20f))
  }

  @Test
  fun `slat threshold treats spreads above each limit as different`() {
    assertThat(viewModel.withMarkers(listOf(20f, 71f), BaseWindowViewModel.GroupThreshold.Slat))
      .isEqualTo(WindowGroupedValue.Different(min = 20f, max = 71f))
    assertThat(viewModel.withoutMarkers(listOf(20f, 81f), BaseWindowViewModel.GroupThreshold.Slat))
      .isEqualTo(WindowGroupedValue.Different(min = 20f, max = 81f))
  }

  @Test
  fun `group values should extract values from other types`() {
    assertThat(viewModel.withoutMarkers(listOf(TestValue(22f), TestValue(28f))) { it.value })
      .isEqualTo(WindowGroupedValue.Different(min = 22f, max = 28f))
    assertThat(viewModel.withoutMarkers(listOf(TestValue(22f), TestValue(22f))) { it.value })
      .isEqualTo(WindowGroupedValue.Similar(22f))
  }

  @Test
  fun `zero threshold considers only identical values similar`() {
    assertThat(viewModel.withoutMarkers(listOf(20f, 20.1f, 19.8f), BaseWindowViewModel.GroupThreshold.None))
      .isEqualTo(WindowGroupedValue.Similar(20f))
    assertThat(viewModel.withoutMarkers(listOf(20f, 21.01f), BaseWindowViewModel.GroupThreshold.None))
      .isEqualTo(WindowGroupedValue.Different(min = 20f, max = 21.01f))
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
    fun withMarkers(
      values: List<Float>,
      threshold: GroupThreshold = GroupThreshold.Default
    ): WindowGroupedValue = getGroupValues(values, true, threshold)

    fun withoutMarkers(
      values: List<Float>,
      threshold: GroupThreshold = GroupThreshold.Default
    ): WindowGroupedValue = getGroupValues(values, false, threshold)

    fun <T> withoutMarkers(
      values: List<T>,
      threshold: GroupThreshold = GroupThreshold.Default,
      extractor: (T) -> Float
    ): WindowGroupedValue =
      getGroupValues(values, false, threshold = threshold, extractor = extractor)

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
