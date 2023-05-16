package org.supla.android.features.scenelist

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.Location
import org.supla.android.db.Scene
import org.supla.android.events.ListsEventsManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.scene.CreateProfileScenesListUseCase

@RunWith(MockitoJUnitRunner::class)
class SceneListViewModelTest : BaseViewModelTest<SceneListViewState, SceneListViewEvent>() {

  @Mock
  private lateinit var sceneRepository: SceneRepository

  @Mock
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @Mock
  private lateinit var createProfileScenesListUseCase: CreateProfileScenesListUseCase

  @Mock
  private lateinit var listsEventsManager: ListsEventsManager

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: SceneListViewModel by lazy {
    SceneListViewModel(
      sceneRepository,
      toggleLocationUseCase,
      createProfileScenesListUseCase,
      listsEventsManager,
      preferences,
      schedulers
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    whenever(listsEventsManager.observeSceneUpdates()).thenReturn(listsEventsSubject)
    super.setUp()
  }

  @Test
  fun `should load scenes`() {
    // given
    val items: List<ListItem.SceneItem> = listOf(mockk())
    whenever(createProfileScenesListUseCase()).thenReturn(Observable.just(items))

    // when
    viewModel.loadScenes()

    // then
    val state = SceneListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, scenes = items),
      state.copy()
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileScenesListUseCase)
  }

  @Test
  fun `should update scenes order`() {
    // given
    val scenes: List<Scene> = listOf(mockk(), mockk(), mockk())
    every { scenes[0].sortOrder = 0 } answers { }
    every { scenes[1].sortOrder = 1 } answers { }
    every { scenes[2].sortOrder = 2 } answers { }

    // when
    viewModel.onSceneOrderUpdate(scenes)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify(sceneRepository).updateScene(scenes[0])
    verify(sceneRepository).updateScene(scenes[1])
    verify(sceneRepository).updateScene(scenes[2])
    verifyNoMoreInteractions(sceneRepository)
    verifyZeroInteractionsExcept(sceneRepository)
  }

  @Test
  fun `should toggle location collapsed and reload scenes`() {
    // given
    val location = mockk<Location>()
    whenever(toggleLocationUseCase(location, CollapsedFlag.SCENE)).thenReturn(Completable.complete())
    val list = emptyList<ListItem.SceneItem>()
    whenever(createProfileScenesListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = SceneListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, scenes = list),
      state.copy()
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileScenesListUseCase, toggleLocationUseCase)
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = emptyList<ListItem.SceneItem>()
    whenever(createProfileScenesListUseCase()).thenReturn(Observable.just(list))

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = SceneListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(loading = true),
      state.copy(loading = true, scenes = list),
      state.copy()
    )
    Assertions.assertThat(events).isEmpty()
    verifyZeroInteractionsExcept(createProfileScenesListUseCase)
  }

  private fun verifyZeroInteractionsExcept(vararg except: Any) {
    val allDependencies = listOf(
      sceneRepository,
      toggleLocationUseCase,
      createProfileScenesListUseCase
    )
    for (dependency in allDependencies) {
      if (!except.contains(dependency)) {
        verifyZeroInteractions(dependency)
      }
    }
  }
}
