package org.supla.android.usecases.scene

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.Location
import org.supla.android.db.entity.Scene
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag

@RunWith(MockitoJUnitRunner::class)
class CreateProfileScenesListUseCaseTest {

  @Mock
  private lateinit var sceneRepository: SceneRepository

  @Mock
  private lateinit var channelRepository: ChannelRepository

  @InjectMocks
  private lateinit var useCase: CreateProfileScenesListUseCase

  @Test
  fun `should create list of scenes with locations`() {
    // given
    val firstLocationId = 2
    val collapsedLocationId = 4
    val thirdLocationId = 8

    val scenes = listOf(
      mockScene(firstLocationId),
      mockScene(firstLocationId),
      mockScene(collapsedLocationId),
      mockScene(thirdLocationId)
    )

    val firstLocation = mockLocation(firstLocationId)
    val collapsedLocation = mockLocation(collapsedLocationId, collapsed = true)
    val thirdLocation = mockLocation(thirdLocationId)

    whenever(sceneRepository.getAllProfileScenes()).thenReturn(Observable.just(scenes))
    whenever(channelRepository.getLocation(firstLocationId)).thenReturn(firstLocation)
    whenever(channelRepository.getLocation(collapsedLocationId)).thenReturn(collapsedLocation)
    whenever(channelRepository.getLocation(thirdLocationId)).thenReturn(thirdLocation)

    // when
    val testObserver = useCase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    Assertions.assertThat(list).hasSize(6)
    Assertions.assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[5]).isInstanceOf(ListItem.SceneItem::class.java)

    Assertions.assertThat((list[1] as ListItem.SceneItem).scene).isEqualTo(scenes[0])
    Assertions.assertThat((list[2] as ListItem.SceneItem).scene).isEqualTo(scenes[1])
    Assertions.assertThat((list[5] as ListItem.SceneItem).scene).isEqualTo(scenes[3])

    Assertions.assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    Assertions.assertThat((list[3] as ListItem.LocationItem).location).isEqualTo(collapsedLocation)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val firstLocationId = 2
    val secondLocationId = 4
    val thirdLocationId = 8

    val scenes = listOf(
      mockScene(firstLocationId),
      mockScene(firstLocationId),
      mockScene(secondLocationId),
      mockScene(thirdLocationId)
    )

    val firstLocation = mockLocation(firstLocationId, "Test")
    val secondLocation = mockLocation(secondLocationId, "Test")
    val thirdLocation = mockLocation(thirdLocationId)

    whenever(sceneRepository.getAllProfileScenes()).thenReturn(Observable.just(scenes))
    whenever(channelRepository.getLocation(firstLocationId)).thenReturn(firstLocation)
    whenever(channelRepository.getLocation(secondLocationId)).thenReturn(secondLocation)
    whenever(channelRepository.getLocation(thirdLocationId)).thenReturn(thirdLocation)

    // when
    val testObserver = useCase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    Assertions.assertThat(list).hasSize(6)
    Assertions.assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[3]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[5]).isInstanceOf(ListItem.SceneItem::class.java)

    Assertions.assertThat((list[1] as ListItem.SceneItem).scene).isEqualTo(scenes[0])
    Assertions.assertThat((list[2] as ListItem.SceneItem).scene).isEqualTo(scenes[1])
    Assertions.assertThat((list[3] as ListItem.SceneItem).scene).isEqualTo(scenes[2])
    Assertions.assertThat((list[5] as ListItem.SceneItem).scene).isEqualTo(scenes[3])

    Assertions.assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
  }

  private fun mockScene(locationId: Int): Scene {
    val scene: Scene = mockk()
    every { scene.locationId } returns locationId
    return scene
  }

  private fun mockLocation(locationId: Int, name: String = "Location $locationId", collapsed: Boolean = false): Location {
    val location: Location = mockk()
    every { location.locationId } returns locationId
    every { location.caption } returns name
    if (collapsed) {
      every { location.collapsed } returns (0 or CollapsedFlag.SCENE.value)
    } else {
      every { location.collapsed } returns 0
    }
    return location
  }
}
