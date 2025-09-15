package org.supla.android.usecases.channel

import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

class CreateProfileChannelsListUseCaseTest {

  @MockK
  private lateinit var channelRelationRepository: ChannelRelationRepository

  @MockK
  private lateinit var channelRepository: RoomChannelRepository

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @MockK
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @MockK
  private lateinit var valueFormatter: ValueFormatter

  @MockK
  private lateinit var getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase

  @MockK
  private lateinit var getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase

  @MockK
  private lateinit var gson: Gson

  @InjectMockKs
  private lateinit var usecase: CreateProfileChannelsListUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create list of channels and locations`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12, channelFunction = SuplaFunction.HVAC_THERMOSTAT)
    val third = mockListEntity(31, 32, locationCollapsed = true)
    val fourth = mockListEntity(41, 42, channelFunction = SuplaFunction.DEPTH_SENSOR)
    val fifth = mockListEntity(51, 42, channelFunction = SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER)
    val sixth = mockListEntity(61, 42, channelFunction = SuplaFunction.PROJECTOR_SCREEN)

    every { channelRepository.findList() } returns Single.just(listOf(first, second, third, fourth, fifth, sixth))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(emptyMap())
    every { getChannelIssuesForListUseCase.invoke(any()) } returns ListItemIssues.empty

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(8)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.HvacThermostatItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[6]).isInstanceOf(ListItem.IconWithButtonsItem::class.java)
    assertThat(list[7]).isInstanceOf(ListItem.IconWithButtonsItem::class.java)

    assertThat((list[1] as ListItem.IconValueItem).captionProvider).isEqualTo(LocalizedString.Constant("caption 11"))
    assertThat((list[2] as ListItem.HvacThermostatItem).captionProvider).isEqualTo(LocalizedString.Constant("caption 21"))
    assertThat((list[5] as ListItem.IconValueItem).captionProvider).isEqualTo(LocalizedString.Constant("caption 41"))
    assertThat((list[6] as ListItem.IconWithButtonsItem).captionProvider).isEqualTo(LocalizedString.Constant("caption 51"))
    assertThat((list[7] as ListItem.IconWithButtonsItem).captionProvider).isEqualTo(LocalizedString.Constant("caption 61"))

    assertThat((list[0] as ListItem.LocationItem).location.caption).isEqualTo("12")
    assertThat((list[3] as ListItem.LocationItem).location.caption).isEqualTo("32")
    assertThat((list[4] as ListItem.LocationItem).location.caption).isEqualTo("42")
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val first = mockListEntity(11, 12, channelFunction = SuplaFunction.GENERAL_PURPOSE_METER)
    val second = mockListEntity(21, 12, channelFunction = SuplaFunction.GENERAL_PURPOSE_MEASUREMENT)
    val third = mockListEntity(31, 32, locationName = "12")
    val fourth = mockListEntity(41, 42)

    every { channelRepository.findList() } returns Single.just(listOf(first, second, third, fourth))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(emptyMap())
    every { getChannelIssuesForListUseCase.invoke(any()) } returns ListItemIssues.empty

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.IconValueItem::class.java)

    assertThat((list[1] as ListItem.IconValueItem).value).isEqualTo("value 11")
    assertThat((list[2] as ListItem.IconValueItem).value).isEqualTo("value 21")
    assertThat((list[3] as ListItem.IconValueItem).value).isEqualTo("value 31")
    assertThat((list[5] as ListItem.IconValueItem).value).isEqualTo("value 41")

    assertThat((list[0] as ListItem.LocationItem).location.caption).isEqualTo("12")
    assertThat((list[4] as ListItem.LocationItem).location.caption).isEqualTo("42")
  }

  @Test
  fun `should load children`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12)
    val third = mockListEntity(31, 12)

    every { channelRepository.findList() } returns Single.just(listOf(first, second, third))
    val childrenRelation = mockk<ChannelRelationEntity> {
      every { channelId } returns 21
      every { parentId } returns 11
      every { relationType } returns ChannelRelationType.DEFAULT
    }
    val relationMap = mapOf(11 to listOf(childrenRelation))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(relationMap)
    val childEntity = ChannelChildEntity(childrenRelation, second)
    every {
      getChannelChildrenTreeUseCase.invoke(eq(11), eq(relationMap), any(), any())
    } returns listOf(childEntity)
    every {
      getChannelValueStringUseCase.valueOrNull(
        channel = eq(ChannelWithChildren(first, listOf(childEntity))),
        valueType = eq(ValueType.FIRST),
        withUnit = eq(true)
      )
    } returns "value 11"
    every { getChannelIssuesForListUseCase.invoke(any()) } returns ListItemIssues.empty

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(3)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.IconValueItem::class.java)
    assertThat((list[0] as ListItem.LocationItem).location.caption).isEqualTo("12")
    assertThat((list[1] as ListItem.IconValueItem).channelBase.remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.IconValueItem).channelBase.remoteId).isEqualTo(31)
  }

  private fun mockListEntity(
    channelRemoteId: Int,
    locationRemoteId: Int,
    locationName: String = "$locationRemoteId",
    locationCollapsed: Boolean = false,
    channelFunction: SuplaFunction = SuplaFunction.NONE
  ): ChannelDataEntity = mockk {
    every { remoteId } returns channelRemoteId
    every { function } returns channelFunction
    every { caption } returns ""
    every { altIcon } returns 0
    every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    every { stateEntity } returns null
    every { locationEntity } returns mockk {
      every { remoteId } returns locationRemoteId
      every { caption } returns locationName
      every { isCollapsed(CollapsedFlag.CHANNEL) } returns locationCollapsed
    }
    every { channelEntity } returns mockk {
      every { function } returns channelFunction
      every { remoteId } returns channelRemoteId
    }
    every { channelValueEntity } returns mockk {
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      if (channelFunction == SuplaFunction.HVAC_THERMOSTAT) {
        every { asThermostatValue() } returns mockThermostatValue()
      }
      val isRollerShutter = channelFunction == SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
      val isProjectorScreen = channelFunction == SuplaFunction.PROJECTOR_SCREEN
      if (isRollerShutter || isProjectorScreen) {
        every { asRollerShutterValue() } returns mockk {
          every { getChannelIssue() } returns null
        }
      }
      every { getValueAsByteArray() } returns byteArrayOf()
      every { configEntity } returns null
    }

    if (channelFunction == SuplaFunction.HVAC_THERMOSTAT) {
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns null
      }
    } else {
      every { channelExtendedValueEntity } returns null
    }

    every {
      getCaptionUseCase.invoke(match { it.remoteId == channelRemoteId })
    } returns LocalizedString.Constant("caption $channelRemoteId")
    every {
      getChannelValueStringUseCase.valueOrNull(
        channel = eq(ChannelWithChildren(this@mockk)),
        valueType = eq(ValueType.FIRST),
        withUnit = eq(true)
      )
    } returns "value $channelRemoteId"
    every { getChannelIconUseCase.invoke(this@mockk) } returns ImageId(channelRemoteId)
  }

  private fun mockThermostatValue(): ThermostatValue = mockk {
    val heatTemperature = 10.4f
    val coolTemperature = 18.3f
    every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    every { flags } returns emptyList()
    every { mode } returns SuplaHvacMode.OFF
    every { setpointTemperatureHeat } returns heatTemperature
    every { setpointTemperatureCool } returns coolTemperature

    every { valueFormatter.format(heatTemperature, ValueFormat.WithoutUnit) } returns "10.4"
    every { valueFormatter.format(coolTemperature, ValueFormat.WithoutUnit) } returns "18.3"
  }
}
