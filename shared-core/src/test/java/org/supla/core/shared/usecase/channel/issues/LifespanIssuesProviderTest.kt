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
    // given
    val lifespan = 3.0f
    val channelState: ChannelState = mockk {
      every { lightSourceLifespanLeft } returns lifespan
    }
    val channel: Channel = mockk {
      every { this@mockk.function } returns SuplaFunction.LIGHTSWITCH
      every { this@mockk.channelState } returns channelState
      every { this@mockk.altIcon } returns 2
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_REPLACE, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce schedule warning for UV lamp`() {
    // given
    val lifespan = 12.0f
    val channelState: ChannelState = mockk {
      every { lightSourceLifespanLeft } returns lifespan
    }
    val channel: Channel = mockk {
      every { this@mockk.function } returns SuplaFunction.LIGHTSWITCH
      every { this@mockk.channelState } returns channelState
      every { this@mockk.altIcon } returns 2
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_SCHEDULE, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce lifespan error for other light switch`() {
    // given
    val lifespan = 3.0f
    val channelState: ChannelState = mockk {
      every { lightSourceLifespanLeft } returns lifespan
    }
    val channel: Channel = mockk {
      every { this@mockk.function } returns SuplaFunction.LIGHTSWITCH
      every { this@mockk.channelState } returns channelState
      every { this@mockk.altIcon } returns 0
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespan)))
    )
  }

  @Test
  fun `should produce lifespan warning for other light switch`() {
    // given
    val lifespan = 13.0f
    val channelState: ChannelState = mockk {
      every { lightSourceLifespanLeft } returns lifespan
    }
    val channel: Channel = mockk {
      every { this@mockk.function } returns SuplaFunction.LIGHTSWITCH
      every { this@mockk.channelState } returns channelState
      every { this@mockk.altIcon } returns 0
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespan)))
    )
  }

  @Test
  fun `should not produce issues if lifespan bigger than 20`() {
    // given
    val lifespan = 22.0f
    val channelState: ChannelState = mockk {
      every { lightSourceLifespanLeft } returns lifespan
    }
    val channel: Channel = mockk {
      every { this@mockk.function } returns SuplaFunction.LIGHTSWITCH
      every { this@mockk.channelState } returns channelState
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).isEmpty()
  }
}
