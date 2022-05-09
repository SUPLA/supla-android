package org.supla.android.widget.onoff

import android.database.Cursor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.Preferences
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.db.DbHelper
import org.supla.android.db.SuplaContract
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import org.supla.android.testhelpers.getOrAwaitValue
import org.supla.android.testhelpers.toByteArray
import org.supla.android.widget.WidgetPreferences
import java.security.InvalidParameterException

@RunWith(MockitoJUnitRunner::class)
class OnOffWidgetConfigurationViewModelTest {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var preferences: Preferences

    @Mock
    private lateinit var dbHelper: DbHelper

    @Mock
    private lateinit var widgetPreferences: WidgetPreferences

    @Mock
    private lateinit var profileManager: ProfileManager

    @Test
    fun `should load only channels with switch function`() {
        // given
        val cursor: Cursor = mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)

        // when
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)
        val channels = viewModel.channelsList.getOrAwaitValue()

        // then
        assertThat(channels.size, `is`(1))
        verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `should load all channels with switch function`() {
        // given
        val cursor: Cursor = mockCursorChannels(
                SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
                SuplaConst.SUPLA_CHANNELFNC_DIMMER,
                SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
                SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING,
                SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH)
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)

        // when
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)
        val channels = viewModel.channelsList.getOrAwaitValue()

        // then
        assertThat(channels.size, `is`(5))
        verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `should provide empty list of channels if no channel available`() {
        // given
        val cursor: Cursor = mockCursorChannels()
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)

        // when
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)
        val channels = viewModel.channelsList.getOrAwaitValue()

        // then
        assertThat(channels.size, `is`(0))
        verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `shouldn't allow to save the selection when there is no widget id set`() {
        // given
        val cursor: Cursor = mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)

        // when
        viewModel.confirmSelection()
        val result = viewModel.confirmationResult.getOrAwaitValue()

        // then
        assertThat(result.isFailure, `is`(true))
        assertThat(result.exceptionOrNull() is InvalidParameterException, `is`(true))
        verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `shouldn't allow to save selection when there is no channel selected`() {
        // given
        val cursor: Cursor = mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)

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
    fun `shouldn't allow to save selection when there is no display name for channel provided`() {
        // given
        val cursor: Cursor = mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)

        // when
        viewModel.widgetId = 123
        viewModel.selectedChannel = mock { }
        viewModel.confirmSelection()
        val result = viewModel.confirmationResult.getOrAwaitValue()

        // then
        assertThat(result.isFailure, `is`(true))
        assertThat(result.exceptionOrNull() is EmptyDisplayNameException, `is`(true))
        verifyNoInteractions(widgetPreferences)
    }

    @Test
    fun `should be able to save selection when all necessary information provided`() {
        // given
        val cursor: Cursor = mockCursorChannels(SuplaConst.SUPLA_CHANNELFNC_DIMMER, SuplaConst.SUPLA_CHANNELFNC_ALARM)
        whenever(dbHelper.channelListCursor).thenReturn(cursor)
        whenever(preferences.configIsSet()).thenReturn(true)
        val profileId: Long = 123
        val profile = mock<AuthProfileItem>() {
            on { id } doReturn profileId
        }
        whenever(profileManager.getCurrentProfile()).thenReturn(profile)
        val viewModel = OnOffWidgetConfigurationViewModel(preferences, dbHelper, widgetPreferences, profileManager)

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
        viewModel.selectedChannel = channel
        viewModel.displayName = channelCaption
        viewModel.confirmSelection()
        val result = viewModel.confirmationResult.getOrAwaitValue()

        // then
        assertThat(result.isSuccess, `is`(true))
        verify(widgetPreferences).setWidgetConfiguration(eq(123), argThat {
            this.channelId == channelId &&
                    this.channelCaption == channelCaption &&
                    this.channelFunction == channelFunc &&
                    this.channelColor == channelColor &&
                    this.profileId == profileId
        })
        verifyNoMoreInteractions(widgetPreferences)
    }

    private fun mockCursorChannels(vararg mockedFunctions: Int): Cursor {
        val cursor: Cursor = mock {
            on { moveToFirst() } doReturn true
            on { getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC) } doReturn 123
        }
        if (mockedFunctions.size > 1) {
            val channelFncs = Array(mockedFunctions.size - 1) { 0 }
            for (i in 1 until mockedFunctions.size) {
                channelFncs[i - 1] = mockedFunctions[i]
            }
            whenever(cursor.getInt(123)).thenReturn(mockedFunctions[0], *channelFncs)

            val moveToNexts = Array(mockedFunctions.size - 1) { true }
            moveToNexts[moveToNexts.size - 1] = false
            whenever(cursor.moveToNext()).thenReturn(true, *moveToNexts)
        } else if (mockedFunctions.size == 1) {
            whenever(cursor.getInt(123)).thenReturn(mockedFunctions[0])
            whenever(cursor.moveToNext()).thenReturn(false)
        }

        whenever(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE)).thenReturn(123)
        whenever(cursor.getBlob(123)).thenReturn(SuplaChannelExtendedValue().toByteArray())

        return cursor
    }
}