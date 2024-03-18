package org.supla.android.usecases.scene

import io.reactivex.rxjava3.core.Completable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.db.Location

@RunWith(MockitoJUnitRunner::class)
class UpdateSceneOrderUseCaseTest {

  @Mock
  private lateinit var sceneRepository: RoomSceneRepository

  @InjectMocks
  private lateinit var useCase: UpdateSceneOrderUseCase

  @Test
  fun `should update order`() {
    // given
    val firstScene = createSceneEntity(1L)
    val secondScene = createSceneEntity(2L)
    val thirdScene = createSceneEntity(3L)

    whenever(sceneRepository.update(any())).thenReturn(Completable.complete())

    // when
    val testObserver = useCase.invoke(listOf(firstScene, secondScene, thirdScene)).test()

    // then
    testObserver.assertComplete()

    val argumentCaptor = argumentCaptor<List<SceneEntity>>()
    verify(sceneRepository).update(argumentCaptor.capture())

    assertThat(argumentCaptor.firstValue)
      .extracting({ it.id }, { it.sortOrder })
      .containsExactly(
        tuple(1L, 0),
        tuple(2L, 1),
        tuple(3L, 2)
      )
  }

  private fun createSceneEntity(id: Long) =
    SceneDataEntity(
      sceneEntity = SceneEntity(
        id = id,
        remoteId = 0,
        locationId = 0,
        altIcon = 0,
        userIcon = 0,
        caption = "",
        startedAt = null,
        estimatedEndDate = null,
        initiatorId = null,
        initiatorName = null,
        sortOrder = 0,
        visible = 0,
        profileId = null
      ),
      locationEntity = LocationEntity(
        id = null,
        remoteId = 0,
        caption = "",
        visible = 0,
        collapsed = 0,
        sorting = Location.SortingType.DEFAULT,
        sortOrder = 0,
        profileId = 0
      )
    )
}
