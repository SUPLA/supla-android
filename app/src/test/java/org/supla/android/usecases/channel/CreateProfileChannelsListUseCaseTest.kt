package org.supla.android.usecases.channel

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.supla.android.core.ui.LocalizedString
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.thermostat.ThermostatIndicatorIcon
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.ListItemIssues
import org.supla.android.ui.lists.data.ChannelIssueItem
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.SuplaChannelFunction

@RunWith(MockitoJUnitRunner::class)
class CreateProfileChannelsListUseCaseTest {

  @Mock
  private lateinit var channelRelationRepository: ChannelRelationRepository

  @Mock
  private lateinit var channelRepository: RoomChannelRepository

  @Mock
  private lateinit var getChannelCaptionUseCase: GetChannelCaptionUseCase

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @Mock
  private lateinit var getSwitchValueStringUseCase: GetSwitchValueStringUseCase

  @Mock
  private lateinit var valuesFormatter: ValuesFormatter

  @Mock
  private lateinit var getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase

  @Mock
  private lateinit var getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase

  @InjectMocks
  private lateinit var usecase: CreateProfileChannelsListUseCase

  @Test
  fun `should create list of channels and locations`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12, channelFunction = SuplaChannelFunction.HVAC_THERMOSTAT)
    val third = mockListEntity(31, 32, locationCollapsed = true)
    val fourth = mockListEntity(41, 42, channelFunction = SuplaChannelFunction.DEPTH_SENSOR)
    val fifth = mockListEntity(51, 42, channelFunction = SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER)
    val sixth = mockListEntity(61, 42, channelFunction = SuplaChannelFunction.PROJECTOR_SCREEN)

    whenever(channelRepository.findList()).thenReturn(Single.just(listOf(first, second, third, fourth, fifth, sixth)))
    whenever(channelRelationRepository.findChildrenToParentsRelations()).thenReturn(Observable.just(emptyMap()))
    whenever(getChannelIssuesForListUseCase.invoke(any())).thenReturn(ListItemIssues.empty)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val context: Context = mockk()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(8)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.HvacThermostatItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[6]).isInstanceOf(ListItem.ShadingSystemItem::class.java)
    assertThat(list[7]).isInstanceOf(ListItem.ShadingSystemItem::class.java)

    assertThat((list[1] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.HvacThermostatItem).captionProvider(context)).isEqualTo("caption 21")
    assertThat((list[5] as ListItem.IconValueItem).captionProvider(context)).isEqualTo("caption 41")
    assertThat((list[6] as ListItem.ShadingSystemItem).captionProvider(context)).isEqualTo("caption 51")
    assertThat((list[7] as ListItem.ShadingSystemItem).captionProvider(context)).isEqualTo("caption 61")

    assertThat((list[0] as ListItem.LocationItem).location.caption).isEqualTo("12")
    assertThat((list[3] as ListItem.LocationItem).location.caption).isEqualTo("32")
    assertThat((list[4] as ListItem.LocationItem).location.caption).isEqualTo("42")
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val first = mockListEntity(11, 12, channelFunction = SuplaChannelFunction.GENERAL_PURPOSE_METER)
    val second = mockListEntity(21, 12, channelFunction = SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT)
    val third = mockListEntity(31, 32, locationName = "12")
    val fourth = mockListEntity(41, 42)

    whenever(channelRepository.findList()).thenReturn(Single.just(listOf(first, second, third, fourth)))
    whenever(channelRelationRepository.findChildrenToParentsRelations()).thenReturn(Observable.just(emptyMap()))
    whenever(getChannelIssuesForListUseCase.invoke(any())).thenReturn(ListItemIssues.empty)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.GeneralPurposeMeterItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.GeneralPurposeMeasurementItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.ChannelItem::class.java)

    assertThat((list[1] as ListItem.GeneralPurposeMeterItem).value).isEqualTo("value 11")
    assertThat((list[2] as ListItem.GeneralPurposeMeasurementItem).value).isEqualTo("value 21")
    assertThat((list[3] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(31)
    assertThat((list[5] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(41)

    assertThat((list[0] as ListItem.LocationItem).location.caption).isEqualTo("12")
    assertThat((list[4] as ListItem.LocationItem).location.caption).isEqualTo("42")
  }

  @Test
  fun `should load children`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12)
    val third = mockListEntity(31, 12)

    whenever(channelRepository.findList()).thenReturn(Single.just(listOf(first, second, third)))
    val childrenRelation = mockk<ChannelRelationEntity> { every { channelId } returns 21 }
    val relationMap = mapOf(11 to listOf(childrenRelation))
    whenever(channelRelationRepository.findChildrenToParentsRelations()).thenReturn(
      Observable.just(relationMap)
    )
    whenever(getChannelChildrenTreeUseCase.invoke(eq(11), eq(relationMap), any(), any()))
      .thenReturn(listOf(ChannelChildEntity(childrenRelation, second)))

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(3)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.ChannelItem::class.java)
    assertThat((list[1] as ListItem.ChannelItem).children).isEqualTo(listOf(ChannelChildEntity(childrenRelation, second)))

    assertThat((list[1] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.ChannelItem).channelBase.remoteId).isEqualTo(31)

    assertThat((list[0] as ListItem.LocationItem).location.caption).isEqualTo("12")
  }

  private fun mockListEntity(
    channelRemoteId: Int,
    locationRemoteId: Int,
    locationName: String = "$locationRemoteId",
    locationCollapsed: Boolean = false,
    channelFunction: SuplaChannelFunction = SuplaChannelFunction.HUMIDITY
  ): ChannelDataEntity = mockk {
    every { remoteId } returns channelRemoteId
    every { function } returns channelFunction
    every { locationEntity } returns mockk {
      every { remoteId } returns locationRemoteId
      every { caption } returns locationName
      every { isCollapsed(CollapsedFlag.CHANNEL) } returns locationCollapsed
    }
    every { channelEntity } returns mockk {
      every { function } returns channelFunction
      every { remoteId } returns channelRemoteId
    }
    every { getLegacyChannel() } returns mockk {
      every { remoteId } returns channelRemoteId
    }
    every { channelValueEntity } returns mockk {
      every { online } returns true
      if (channelFunction == SuplaChannelFunction.HVAC_THERMOSTAT) {
        every { asThermostatValue() } returns mockk {
          every { getSetpointText(valuesFormatter) } returns "setpoint text"
          every { getIndicatorIcon() } returns ThermostatIndicatorIcon.STANDBY
          every { getChannelIssues() } returns listOf(ChannelIssueItem.Warning())
        }
      }
      val isRollerShutter = channelFunction == SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER
      val isProjectorScreen = channelFunction == SuplaChannelFunction.PROJECTOR_SCREEN
      if (isRollerShutter || isProjectorScreen) {
        every { asRollerShutterValue() } returns mockk {
          every { getChannelIssue() } returns null
        }
      }
    }

    if (channelFunction == SuplaChannelFunction.HVAC_THERMOSTAT) {
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns null
      }
    }

    whenever(getChannelCaptionUseCase.invoke(this)).thenReturn(LocalizedString.Constant("caption $channelRemoteId"))
    whenever(getChannelValueStringUseCase.invoke(this)).thenReturn("value $channelRemoteId")
    whenever(getChannelIconUseCase.invoke(this)).thenReturn(ImageId(channelRemoteId))
  }
}
