package org.supla.android.features.details.actiontrigger.list

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase

class ActionTriggerListViewModelTest :
  BaseViewModelTest<ActionTriggerListViewState, ActionTriggerListViewEvent, ActionTriggerListViewModel>() {

  @RelaxedMockK
  private lateinit var observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase

  @RelaxedMockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @RelaxedMockK
  private lateinit var getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase

  @RelaxedMockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @RelaxedMockK
  private lateinit var preferences: ApplicationPreferences

  @RelaxedMockK
  private lateinit var provideChannelDetailTypeUseCase: ProvideChannelDetailTypeUseCase

  @RelaxedMockK
  override lateinit var threading: SuplaThreading

  @InjectMockKs
  override lateinit var viewModel: ActionTriggerListViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should emit caption change event when item caption is long pressed`() {
    val data = mockk<RelatedChannelData>()

    viewModel.onItemCaptionLongPress(data)

    assertThat(events).containsExactly(ActionTriggerListViewEvent.ChangeCaption(data))
  }

  @Test
  fun `should emit info event when item info is clicked`() {
    val data = mockk<RelatedChannelData>()

    viewModel.onItemInfoClick(data)

    assertThat(events).containsExactly(ActionTriggerListViewEvent.ShowInfo(data))
  }

  @Test
  fun `should ignore item click when channel details have not been loaded`() {
    viewModel.onItemClick(mockk { every { channelId } returns 42 })

    assertThat(events).isEmpty()
  }
}
