package org.supla.android.features.scenelist

import android.net.Uri
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.android.usecases.scene.CreateProfileScenesListUseCase
import org.supla.android.usecases.scene.ReorderScenesUseCase

@RunWith(MockitoJUnitRunner::class)
class SceneListViewModelTest : BaseViewModelTest<SceneListViewState, SceneListViewEvent, SceneListViewModel>() {

  @Mock
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @Mock
  private lateinit var createProfileScenesListUseCase: CreateProfileScenesListUseCase

  @Mock
  private lateinit var reorderScenesUseCase: ReorderScenesUseCase

  @Mock
  private lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  private lateinit var loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase

  @Mock
  private lateinit var preferences: ApplicationPreferences

  @Mock
  private lateinit var dateProvider: DateProvider

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: SceneListViewModel by lazy {
    SceneListViewModel(
      createProfileScenesListUseCase,
      reorderScenesUseCase,
      toggleLocationUseCase,
      loadActiveProfileUrlUseCase,
      updateEventsManager,
      schedulers,
      dateProvider,
      preferences
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    whenever(updateEventsManager.observeScenesUpdate()).thenReturn(listsEventsSubject)
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
      state.copy(scenes = items)
    )
    Assertions.assertThat(events).isEmpty()
    verifyNoInteractionsExcept(createProfileScenesListUseCase)
  }

  @Test
  fun `should update scenes order`() {
    // given
    val scenes: List<SceneDataEntity> = listOf(mockk(), mockk(), mockk())
    whenever(reorderScenesUseCase.invoke(scenes)).thenReturn(Completable.complete())

    // when
    viewModel.onSceneOrderUpdate(scenes)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify(reorderScenesUseCase).invoke(scenes)
    verifyNoMoreInteractions(reorderScenesUseCase)
    verifyNoInteractionsExcept(reorderScenesUseCase)
  }

  @Test
  fun `should toggle location collapsed and reload scenes`() {
    // given
    val location = mockk<LocationEntity>()
    whenever(toggleLocationUseCase(location, CollapsedFlag.SCENE)).thenReturn(Completable.complete())
    val list = listOf<ListItem.SceneItem>(mockk())
    whenever(createProfileScenesListUseCase()).thenReturn(Observable.just(list))

    // when
    viewModel.toggleLocationCollapsed(location)

    // then
    val state = SceneListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(scenes = list)
    )
    Assertions.assertThat(events).isEmpty()
    verifyNoInteractionsExcept(createProfileScenesListUseCase, toggleLocationUseCase)
  }

  @Test
  fun `should reload list on update`() {
    // given
    val list = listOf<ListItem.SceneItem>(mockk())
    whenever(createProfileScenesListUseCase()).thenReturn(Observable.just(list))

    // when
    listsEventsSubject.onNext(Any())

    // then
    val state = SceneListViewState()
    Assertions.assertThat(states).containsExactly(
      state.copy(scenes = list)
    )
    Assertions.assertThat(events).isEmpty()
    verifyNoInteractionsExcept(createProfileScenesListUseCase)
  }

  @Test
  fun `on add group click should open supla cloud`() {
    // given
    whenever(loadActiveProfileUrlUseCase.invoke()).thenReturn(Single.just(CloudUrl.DefaultCloud))

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(SceneListViewEvent.NavigateToSuplaCloud)
    verify(loadActiveProfileUrlUseCase).invoke()
    verifyNoMoreInteractions(loadActiveProfileUrlUseCase)
    verifyNoInteractionsExcept(loadActiveProfileUrlUseCase)
  }

  @Test
  fun `on add group click should open private cloud`() {
    // given
    val url: Uri = mockk()
    whenever(loadActiveProfileUrlUseCase.invoke()).thenReturn(Single.just(CloudUrl.ServerUri(url)))

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(SceneListViewEvent.NavigateToPrivateCloud(url))
    verify(loadActiveProfileUrlUseCase).invoke()
    verifyNoMoreInteractions(loadActiveProfileUrlUseCase)
    verifyNoInteractionsExcept(loadActiveProfileUrlUseCase)
  }

  private fun verifyNoInteractionsExcept(vararg except: Any) {
    val allDependencies = listOf(
      toggleLocationUseCase,
      createProfileScenesListUseCase,
      reorderScenesUseCase
    )
    for (dependency in allDependencies) {
      if (!except.contains(dependency)) {
        verifyNoInteractions(dependency)
      }
    }
  }
}
