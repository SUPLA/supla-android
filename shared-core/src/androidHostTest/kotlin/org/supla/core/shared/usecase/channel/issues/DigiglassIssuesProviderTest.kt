package org.supla.core.shared.usecase.channel.issues

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedStringId

class DigiglassIssuesProviderTest {

  @InjectMockKs
  private lateinit var provider: DigiglassIssuesProvider

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get empty list if no flag set`() {
    // given
    val channel: Channel = mockk {
      every { function } returns SuplaFunction.DIGIGLASS_HORIZONTAL
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { value } returns byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).isEmpty()
  }

  @Test
  fun `should get issue for each flag set`() {
    // given
    val channel: Channel = mockk {
      every { function } returns SuplaFunction.DIGIGLASS_HORIZONTAL
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { value } returns byteArrayOf(7, 0, 0, 0, 0, 0, 0, 0)
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Warning(LocalizedStringId.DIGIGLASS_PLANNED_REGENERATION),
      ChannelIssueItem.Warning(LocalizedStringId.DIGIGLASS_REGENERATION_AFTER_20H),
      ChannelIssueItem.Error(LocalizedStringId.DIGIGLASS_TO_LONG_OPERATION)
    )
  }
}
