package org.supla.android.usecases.icon.producers
/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import org.assertj.core.api.Assertions
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.model.general.IconType
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer

abstract class BaseIconResourceProducerTest {

  abstract val producer: IconResourceProducer

  protected fun mockIconData(
    stateValue: ChannelState.Value = ChannelState.Value.NOT_USED,
    altIcon: Int = 0,
    type: IconType = IconType.SINGLE
  ) = mockIconData(ChannelState(stateValue), altIcon, type)

  protected fun mockIconData(
    state: ChannelState = ChannelState(ChannelState.Value.NOT_USED),
    altIcon: Int = 0,
    type: IconType = IconType.SINGLE
  ) =
    IconData(
      function = 0,
      altIcon = altIcon,
      state = state,
      type = type
    )

  protected fun test(state: ChannelState.Value, altIcon: Int, function: Int, expectedIcon: Int) =
    test(mockIconData(ChannelState(state), altIcon), function, expectedIcon)

  protected fun test(state: ChannelState.Value, function: Int, expectedIcon: Int) =
    test(mockIconData(ChannelState(state)), function, expectedIcon)

  protected fun test(data: IconData, function: Int, expectedIcon: Int?) {
    // when
    val accepts = producer.accepts(function)
    val icon = producer.produce(data)

    // then
    Assertions.assertThat(accepts).isTrue
    Assertions.assertThat(icon).isEqualTo(expectedIcon)
  }
}
