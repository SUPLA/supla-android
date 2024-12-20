package org.supla.android.testhelpers.extensions

import io.mockk.every
import io.mockk.mockk
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.core.shared.data.model.general.SuplaFunction

fun mock(remoteId: Int, profileId: Long, function: SuplaFunction): ChannelWithChildren = mockk {
  every { this@mockk.remoteId } returns remoteId
  every { this@mockk.profileId } returns profileId
  every { this@mockk.function } returns function
}
