package org.supla.android.usecases.icon
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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ValueStateWrapper
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.general.suplaFunction

@Suppress("SameParameterValue")
@RunWith(MockitoJUnitRunner::class)
class GetChannelIconUseCaseTest {

  @Mock
  private lateinit var getChannelStateUseCase: GetChannelStateUseCase

  @Mock
  private lateinit var getDefaultIconResourceUseCase: GetDefaultIconResourceUseCase

  @Mock
  private lateinit var imageCacheProxy: ImageCacheProxy

  @InjectMocks
  private lateinit var useCase: GetChannelIconUseCase

  @Test
  fun `should return null when wrong icon type asked`() {
    // given
    val channelBase: Channel = mockk {
      every { func } returns SUPLA_CHANNELFNC_LIGHTSWITCH
    }
    val type: IconType = IconType.SECOND

    // when
    val imageId = useCase.invoke(channelBase, type)

    // then
    assertThat(imageId).isNull()
  }

  @Test
  fun `should return null when wrong icon type asked (entity)`() {
    // given
    val channelBase: ChannelDataEntity = mockk {
      every { function } returns SuplaFunction.LIGHTSWITCH
    }
    val type: IconType = IconType.SECOND

    // when
    assertThatThrownBy {
      useCase.invoke(channelBase, type)
    }
      .hasMessage("Wrong icon type (iconType: '$type', function: '${SuplaFunction.LIGHTSWITCH}')!")
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `should get default icon when user icon not defined`() {
    // given
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH
    val userIconId = 0
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.ON)
    val resourceId = 123

    val channelBase = mockChannelBase(function, userIconId, altIcon, 0)

    val iconData = IconData(function.suplaFunction(), altIcon, channelState)
    whenever(getDefaultIconResourceUseCase(iconData)).thenReturn(resourceId)
    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(resourceId)
    assertThat(imageId.profileId).isEqualTo(0)
    assertThat(imageId.subId).isEqualTo(0)
  }

  @Test
  fun `should get default icon when user icon not defined (entity)`() {
    // given
    val function = SuplaFunction.LIGHTSWITCH
    val userIconId = 0
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.ON)
    val resourceId = 123

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, 0)

    val iconData = IconData(function, altIcon, channelState)
    whenever(getDefaultIconResourceUseCase(iconData)).thenReturn(resourceId)
    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)

    // when
    val imageId = useCase.invoke(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(resourceId)
    assertThat(imageId.profileId).isEqualTo(0)
    assertThat(imageId.subId).isEqualTo(0)
  }

  @Test
  fun `should get user icon`() {
    // given
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.ON)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (entity)`() {
    // given
    val function = SuplaFunction.LIGHTSWITCH
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.ON)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (inactive)`() {
    // given
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (inactive) (entity)`() {
    // given
    val function = SuplaFunction.LIGHTSWITCH
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (humidity and temperature)`() {
    // given
    val function = SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val stateWrapper: ValueStateWrapper = mockk()
    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (humidity and temperature) (entity)`() {
    // given
    val function = SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (humidity and temperature, second)`() {
    // given
    val function = SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase, type = IconType.SECOND)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (humidity and temperature, second) (entity)`() {
    // given
    val function = SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity, type = IconType.SECOND)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (thermometer)`() {
    // given
    val function = SUPLA_CHANNELFNC_THERMOMETER
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (thermometer) (entity)`() {
    // given
    val function = SuplaFunction.THERMOMETER
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OFF)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (garage door closed)`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.CLOSED)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (garage door closed) (entity)`() {
    // given
    val function = SuplaFunction.CONTROLLING_THE_GARAGE_DOOR
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.CLOSED)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (garage door open)`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OPEN)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (garage door open) (entity)`() {
    // given
    val function = SuplaFunction.CONTROLLING_THE_GARAGE_DOOR
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.OPEN)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (garage door partially open)`() {
    // given
    val function = SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.PARTIALLY_OPENED)
    val profileId = 212L

    val channelBase = mockChannelBase(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelBase)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 3, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase.invoke(channelBase)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId!!.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(3)
  }

  @Test
  fun `should get user icon (garage door partially open) (entity)`() {
    // given
    val function = SuplaFunction.CONTROLLING_THE_GARAGE_DOOR
    val userIconId = 5
    val altIcon = 1
    val channelState = ChannelState(ChannelState.Value.PARTIALLY_OPENED)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, altIcon, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 3, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(3)
  }

  @Test
  fun `should get user icon (dimmer and rgb - off off)`() {
    // given
    val function = SuplaFunction.DIMMER_AND_RGB_LIGHTING
    val userIconId = 5
    val channelState = ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.OFF, ChannelState.Value.OFF))
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, 0, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 1, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(1)
  }

  @Test
  fun `should get user icon (dimmer and rgb - on off)`() {
    // given
    val function = SuplaFunction.DIMMER_AND_RGB_LIGHTING
    val userIconId = 5
    val channelState = ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.ON, ChannelState.Value.OFF))
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, 0, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 2, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(2)
  }

  @Test
  fun `should get user icon (dimmer and rgb - off on)`() {
    // given
    val function = SuplaFunction.DIMMER_AND_RGB_LIGHTING
    val userIconId = 5
    val channelState = ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.OFF, ChannelState.Value.ON))
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, 0, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 3, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(3)
  }

  @Test
  fun `should get user icon (dimmer and rgb - on on)`() {
    // given
    val function = SuplaFunction.DIMMER_AND_RGB_LIGHTING
    val userIconId = 5
    val channelState = ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.ON, ChannelState.Value.ON))
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, 0, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 4, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(4)
  }

  @Test
  fun `should get user icon (dimmer and rgb - unclear)`() {
    // given
    val function = SuplaFunction.DIMMER_AND_RGB_LIGHTING
    val userIconId = 5
    val channelState = ChannelState(ChannelState.Value.ON)
    val profileId = 212L

    val channelDataEntity = mockChannelDataEntity(function, userIconId, 0, profileId)

    whenever(getChannelStateUseCase(channelDataEntity)).thenReturn(channelState)
    val expectedImageId = ImageId(userIconId, 4, profileId)
    whenever(imageCacheProxy.bitmapExists(expectedImageId)).thenReturn(true)

    // when
    val imageId = useCase(channelDataEntity)

    // then
    assertThat(imageId).isNotNull
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.profileId).isEqualTo(profileId)
    assertThat(imageId.subId).isEqualTo(4)
  }

  private fun mockChannelBase(function: Int, userIconId: Int, altIcon: Int, profileId: Long): ChannelBase {
    val channelBase: ChannelBase = mockk()
    every { channelBase.func } returns function
    every { channelBase.userIconId } returns userIconId
    every { channelBase.altIcon } returns altIcon
    every { channelBase.profileId } returns profileId

    return channelBase
  }

  private fun mockChannelDataEntity(
    function: SuplaFunction,
    userIconId: Int,
    altIcon: Int,
    profileId: Long
  ): ChannelDataEntity {
    return mockk {
      every { this@mockk.function } returns function
      every { this@mockk.userIcon } returns userIconId
      every { this@mockk.altIcon } returns altIcon
      every { this@mockk.profileId } returns profileId
    }
  }
}
