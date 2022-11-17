package org.supla.android.widget.onoff.configuration
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

import android.database.Cursor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.profile.ProfileManager
import org.supla.android.testhelpers.getOrAwaitValue
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.WidgetConfigurationViewModelTestBase
import org.supla.android.widget.shared.configuration.EmptyDisplayNameException
import org.supla.android.widget.shared.configuration.ItemType
import org.supla.android.widget.shared.configuration.NoItemSelectedException
import java.security.InvalidParameterException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class OnOffWidgetConfigurationViewModelTest : WidgetConfigurationViewModelTestBase() {
  @get:Rule
  val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

  private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

  @Mock
  private lateinit var dispatchers: CoroutineDispatchers

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var widgetPreferences: WidgetPreferences

  @Mock
  private lateinit var profileManager: ProfileManager

  @Mock
  private lateinit var channelRepository: ChannelRepository

  @Mock
  private lateinit var sceneRepository: SceneRepository

  @Mock
  private lateinit var singleCallProvider: SingleCall.Provider

  @Mock
  private lateinit var temperatureFormat: ValuesFormatter

  @Before
  fun setUp() {
    whenever(dispatchers.io()).thenReturn(testDispatcher)
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
  }

  @Test
  fun `should load only channels with switch function`() = runTest {
    // given
    val profileId = 123L
    val cursor: Cursor =
      mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(1))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `should load channel groups with switch function when type changed to group`() = runTest {
    // given
    val profileId = 321L
    val cursor =
      mockCursorChannelGroups(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
    whenever(channelRepository.getAllProfileChannelGroups(profileId)).thenReturn(cursor)
    val channelsCursor = mockCursorChannels()
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(channelsCursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    viewModel.changeType(ItemType.GROUP)
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(1))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(channelRepository).getAllProfileChannelGroups(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers, times(2)).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `should load all channels with switch function`() = runTest {
    // given
    val profileId = 234L
    val cursor: Cursor = mockCursorChannels(
      SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
      SuplaConst.SUPLA_CHANNELFNC_DIMMER,
      SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
      SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING,
      SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
    )
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(5))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `should load all channel groups with switch function`() = runTest {
    // given
    val profileId = 234L
    val cursor: Cursor = mockCursorChannels(
      SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
      SuplaConst.SUPLA_CHANNELFNC_DIMMER,
      SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
      SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING,
      SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
    )
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    viewModel.changeType(ItemType.GROUP)
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(5))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(channelRepository).getAllProfileChannelGroups(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers, times(2)).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `should load all roller shutter channels`() = runTest {
    // given
    val profileId = 234L
    val cursor: Cursor = mockCursorChannels(
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
    )
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(2))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `should load all roller shutter channel groups`() = runTest {
    // given
    val profileId = 234L
    val cursor: Cursor = mockCursorChannels(
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
    )
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    viewModel.changeType(ItemType.GROUP)
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(2))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(channelRepository).getAllProfileChannelGroups(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers, times(2)).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `should provide empty list of channels if no channel available`() = runTest {
    // given
    val profileId = 123L
    val cursor: Cursor = mockCursorChannels()
    whenever(channelRepository.getAllProfileChannels(profileId)).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)

    val profile = mock<AuthProfileItem>()
    whenever(profile.id).thenReturn(profileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(0))
    verify(channelRepository).getAllProfileChannels(profileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `shouldn't allow to save the selection when there is no widget id set`() = runTest {
    // given
    whenever(preferences.configIsSet()).thenReturn(true)
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )

    // when
    viewModel.confirmSelection()
    val result = viewModel.confirmationResult.getOrAwaitValue()

    // then
    assertThat(result.isFailure, `is`(true))
    assertThat(result.exceptionOrNull() is InvalidParameterException, `is`(true))
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `shouldn't allow to save selection when there is no channel selected`() = runTest {
    // given
    whenever(preferences.configIsSet()).thenReturn(true)
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()

    // when
    viewModel.widgetId = 123
    viewModel.confirmSelection()
    val result = viewModel.confirmationResult.getOrAwaitValue()

    // then
    assertThat(result.isFailure, `is`(true))
    assertThat(result.exceptionOrNull() is NoItemSelectedException, `is`(true))
    verifyNoInteractions(widgetPreferences)
  }

  @Test
  fun `shouldn't allow to save selection when there is no display name for channel provided`() =
    runTest {
      // given
      whenever(preferences.configIsSet()).thenReturn(true)
      val viewModel = OnOffWidgetConfigurationViewModel(
        preferences,
        widgetPreferences,
        profileManager,
        channelRepository,
        sceneRepository,
        dispatchers,
        singleCallProvider,
        temperatureFormat
      )
      advanceUntilIdle()

      // when
      viewModel.widgetId = 123
      viewModel.selectedItem = mock { }
      viewModel.confirmSelection()
      val result = viewModel.confirmationResult.getOrAwaitValue()

      // then
      assertThat(result.isFailure, `is`(true))
      assertThat(result.exceptionOrNull() is EmptyDisplayNameException, `is`(true))
      verifyNoInteractions(widgetPreferences)
    }

  @Test
  fun `should be able to save selection when all necessary information provided`() = runTest {
    // given
    val cursor: Cursor =
      mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
    whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
    whenever(preferences.configIsSet()).thenReturn(true)
    val profileId: Long = 123
    val profile = mock<AuthProfileItem> {
      on { id } doReturn profileId
    }
    whenever(profileManager.getCurrentProfile()).thenReturn(profile)
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()

    val channelId = 234
    val channelFunc = SuplaConst.SUPLA_CHANNELFNC_DIMMER
    val channelColor = 345
    val channelCaption = "a name"
    val channel = mock<Channel> {
      on { it.channelId } doReturn channelId
      on { func } doReturn channelFunc
      on { color } doReturn channelColor
    }

    // when
    viewModel.widgetId = 123
    viewModel.selectedItem = channel
    viewModel.displayName = channelCaption
    viewModel.confirmSelection()
    advanceUntilIdle()
    val result = viewModel.confirmationResult.getOrAwaitValue()

    // then
    assertThat(result.isSuccess, `is`(true))
    verify(widgetPreferences).setWidgetConfiguration(
      eq(123),
      argThat {
        this.itemId == channelId &&
          this.itemCaption == channelCaption &&
          this.itemFunction == channelFunc &&
          this.value == "$channelColor" &&
          this.profileId == profileId
      }
    )
    verifyNoMoreInteractions(widgetPreferences)
  }

  @Test
  fun `should reload channels when profile changed`() = runTest {
    // given
    val firstProfileId = 234L
    val secondProfileId = 432L
    val firstProfileCursor: Cursor = mockCursorChannels(
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
    )
    whenever(channelRepository.getAllProfileChannels(firstProfileId)).thenReturn(firstProfileCursor)
    val secondProfileCursor: Cursor = mockCursorChannels()
    whenever(channelRepository.getAllProfileChannels(secondProfileId)).thenReturn(
      secondProfileCursor
    )
    whenever(preferences.configIsSet()).thenReturn(true)

    val firstProfile = mock<AuthProfileItem>()
    whenever(firstProfile.id).thenReturn(firstProfileId)
    whenever(profileManager.getCurrentProfile()).thenReturn(firstProfile)
    val secondProfile = mock<AuthProfileItem>()
    whenever(secondProfile.id).thenReturn(secondProfileId)

    // when
    val viewModel = OnOffWidgetConfigurationViewModel(
      preferences,
      widgetPreferences,
      profileManager,
      channelRepository,
      sceneRepository,
      dispatchers,
      singleCallProvider,
      temperatureFormat
    )
    advanceUntilIdle()
    viewModel.changeProfile(secondProfile)
    advanceUntilIdle()
    val channels = viewModel.itemsList.getOrAwaitValue()

    // then
    assertThat(channels.size, `is`(0))
    verify(channelRepository).getAllProfileChannels(firstProfileId)
    verify(channelRepository).getAllProfileChannels(secondProfileId)
    verify(profileManager).getCurrentProfile()
    verify(profileManager).getAllProfiles()
    verify(dispatchers, times(2)).io()
    verify(preferences).configIsSet()
    verifyNoMoreInteractions(channelRepository, profileManager, dispatchers, preferences)
    verifyNoInteractions(widgetPreferences)
  }
}
