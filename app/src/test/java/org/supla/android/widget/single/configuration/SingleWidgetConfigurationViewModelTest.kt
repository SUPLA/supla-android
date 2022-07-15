package org.supla.android.widget.single.configuration
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
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.lib.SuplaConst.*
import org.supla.android.profile.ProfileManager
import org.supla.android.testhelpers.getOrAwaitValue
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.WidgetConfigurationViewModelTestBase
import org.supla.android.widget.shared.configuration.WidgetAction

@RunWith(MockitoJUnitRunner::class)
class SingleWidgetConfigurationViewModelTest : WidgetConfigurationViewModelTestBase() {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var preferences: Preferences

    @Mock
    private lateinit var widgetPreferences: WidgetPreferences

    @Mock
    private lateinit var profileManager: ProfileManager

    @Mock
    private lateinit var channelRepository: ChannelRepository

    @Test
    fun `should load only channels with gate function`() = runBlocking {
        // given
        val cursor: Cursor = mockCursorChannels(SUPLA_CHANNELFNC_CONTROLLINGTHEGATE, SUPLA_CHANNELFNC_ALARM)
        whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)

        val profile = mock<AuthProfileItem>()
        whenever(profile.id).thenReturn(1)
        whenever(profileManager.getCurrentProfile()).thenReturn(profile)

        // when
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        val channels = viewModel.channelsList.getOrAwaitValue()

        // then
        assertThat(channels.size, `is`(1))
        Mockito.verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `should load all channels with switch function`() = runBlocking {
        // given
        val cursor: Cursor = mockCursorChannels(
                SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
                SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK,
                SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR,
                SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK)
        whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)

        val profile = mock<AuthProfileItem>()
        whenever(profile.id).thenReturn(1)
        whenever(profileManager.getCurrentProfile()).thenReturn(profile)

        // when
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        val channels = viewModel.channelsList.getOrAwaitValue()

        // then
        assertThat(channels.size, `is`(4))
        Mockito.verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `should provide empty list of channels if no channel available`() = runBlocking {
        // given
        val cursor: Cursor = mockCursorChannels()
        whenever(channelRepository.getAllProfileChannels(any())).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)

        val profile = mock<AuthProfileItem>()
        whenever(profile.id).thenReturn(1)
        whenever(profileManager.getCurrentProfile()).thenReturn(profile)

        // when
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        val channels = viewModel.channelsList.getOrAwaitValue()

        // then
        assertThat(channels.size, `is`(0))
        Mockito.verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `should update actions when channel changed to switch`() {
        // given
        val channel = Channel()
        channel.func = SUPLA_CHANNELFNC_LIGHTSWITCH

        // when
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        viewModel.changeChannel(channel)

        // then
        val actionsList = viewModel.actionsList.getOrAwaitValue()
        assertThat(actionsList.size, `is`(2))
        assertThat(actionsList[0], `is`(WidgetAction.TURN_ON))
        assertThat(actionsList[1], `is`(WidgetAction.TURN_OFF))
    }

    @Test
    fun `should update actions when channel changed to roller shutter`() {
        // given
        val channel = Channel()
        channel.func = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER

        // when
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        viewModel.changeChannel(channel)

        // then
        val actionsList = viewModel.actionsList.getOrAwaitValue()
        assertThat(actionsList.size, `is`(2))
        assertThat(actionsList[0], `is`(WidgetAction.MOVE_UP))
        assertThat(actionsList[1], `is`(WidgetAction.MOVE_DOWN))
    }

    @Test
    fun `should remove actions when channel changed to gate controller`() {
        // given
        val channel = Channel()
        channel.func = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        viewModel.changeChannel(channel)

        // when
        channel.func = SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
        viewModel.changeChannel(channel)

        // then
        val actionsList = viewModel.actionsList.getOrAwaitValue()
        assertThat(actionsList.size, `is`(0))
    }

    @Test
    fun `should remove actions when no channel selected`() {
        // given
        val channel = Channel()
        channel.func = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
        val viewModel = SingleWidgetConfigurationViewModel(preferences, widgetPreferences, profileManager, channelRepository)
        viewModel.changeChannel(channel)

        // when
        viewModel.changeChannel(null)

        // then
        val actionsList = viewModel.actionsList.getOrAwaitValue()
        assertThat(actionsList.size, `is`(0))
    }
}