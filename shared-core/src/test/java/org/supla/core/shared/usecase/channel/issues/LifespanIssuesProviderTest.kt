package org.supla.core.shared.usecase.channel.issues

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.core.shared.data.model.channel.ChannelState
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

class LifespanIssuesProviderTest {
  @InjectMockKs
  private lateinit var provider: LifespanIssuesProvider

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should produce replace error for UV lamp`() {
    val lifespan = 3.0f
    performTest(
      lifespanLeft = lifespan,
      expectedIssue = ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_REPLACE, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce schedule warning for UV lamp`() {
    val lifespan = 12.0f
    performTest(
      lifespanLeft = lifespan,
      expectedIssue = ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_SCHEDULE, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce lifespan error for other light switch`() {
    val lifespan = 3.0f
    performTest(
      lifespanLeft = lifespan,
      altIcon = 0,
      expectedIssue = ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce lifespan warning for other light switch`() {
    val lifespan = 13.0f
    performTest(
      lifespanLeft = lifespan,
      altIcon = 0,
      expectedIssue = ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespan)))
    )
  }

  @Test
  fun `should not produce issues if lifespan bigger than 20 percent`() {
    val lifespan = 22.0f
    performTest(
      lifespanLeft = lifespan,
      altIcon = 0,
    )
  }

  @Test
  fun `should produce replace error for UV lamp - based on left percent`() {
    val lifespan = 3.0f
    performTest(
      operatingTimeLeft = lifespan,
      expectedIssue = ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_REPLACE, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce schedule warning for UV lamp - based on left percent`() {
    val lifespan = 12.0f
    performTest(
      operatingTimeLeft = lifespan,
      expectedIssue = ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_SCHEDULE, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce lifespan error for other light switch - based on left percent`() {
    val lifespan = 3.0f
    performTest(
      operatingTimeLeft = lifespan,
      altIcon = 0,
      expectedIssue = ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce lifespan warning for other light switch - based on left percent`() {
    val lifespan = 13.0f
    performTest(
      operatingTimeLeft = lifespan,
      altIcon = 0,
      expectedIssue = ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespan)))
    )
  }

  @Test
  fun `should not produce issues if lifespan bigger than 20 percent - based on left percent`() {
    val lifespan = 22.0f
    performTest(
      operatingTimeLeft = lifespan,
      altIcon = 0,
    )
  }

  private fun performTest(
    lifespanLeft: Float? = null,
    operatingTimeLeft: Float? = null,
    altIcon: Int = 2,
    expectedIssue: ChannelIssueItem? = null
  ) {
    // given
    val channelState: ChannelState = mockk {
      every { lightSourceLifespan } returns 100
      every { lightSourceLifespanLeft } returns lifespanLeft
      every { lightSourceOperatingTimePercentLeft } returns operatingTimeLeft
    }
    val channel: Channel = mockk {
      every { this@mockk.function } returns SuplaFunction.LIGHTSWITCH
      every { this@mockk.channelState } returns channelState
      every { this@mockk.altIcon } returns altIcon
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    if (expectedIssue == null) {
      assertThat(issues).isEmpty()
    } else {
      assertThat(issues).containsExactly(expectedIssue)
    }
  }
}
