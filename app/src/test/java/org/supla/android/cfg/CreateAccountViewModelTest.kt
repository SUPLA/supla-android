package org.supla.android.cfg

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.db.AuthProfileItem
import org.supla.android.features.createaccount.CreateAccountViewEvent
import org.supla.android.features.createaccount.CreateAccountViewModel
import org.supla.android.profile.AuthInfo
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetManager

@RunWith(MockitoJUnitRunner::class)
class CreateAccountViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var profileManager: ProfileManager
    @Mock
    private lateinit var widgetManager: WidgetManager

    @InjectMocks
    private lateinit var viewModel: CreateAccountViewModel

    @Test
    fun shouldInformWhenDeletingActiveProfile() = runBlocking {
        // given
        val profileId = 123L
        val profile: AuthProfileItem = mockProfile(profileId)
        whenever(profile.isActive).thenReturn(true)

        whenever(profileManager.getProfile(profileId)).thenReturn(profile)

        // when
        viewModel.onCreated(profileId)
        viewModel.onDeleteProfile()

        // then
        val action = viewModel.editAction.first()
        assertNotNull(action)
        assertTrue(action is CreateAccountViewEvent.Alert)
        verify(profileManager).getProfile(profileId)
        verifyNoMoreInteractions(profileManager)
        verifyZeroInteractions(widgetManager)
    }


    @Test
    fun shouldConfirmDeleteWithWidget() = runBlocking {
        // given
        val profileId = 123L
        val profile: AuthProfileItem = mockProfile(profileId)

        whenever(profileManager.getProfile(profileId)).thenReturn(profile)
        whenever(widgetManager.hasProfileWidgets(profileId)).thenReturn(true)

        // when
        viewModel.onCreated(profileId)
        viewModel.onDeleteProfile()

        // then
        val action = viewModel.editAction.first()
        assertNotNull(action)
        assertTrue(action is CreateAccountViewEvent.ConfirmDelete)
        assertTrue((action as CreateAccountViewEvent.ConfirmDelete).hasWidgets)
        verify(profileManager).getProfile(profileId)
        verify(widgetManager).hasProfileWidgets(profileId)
        verifyNoMoreInteractions(profileManager, widgetManager)
    }

    @Test
    fun shouldConfirmDeleteWithoutWidget() = runBlocking {
        // given
        val profileId = 234L
        val profile: AuthProfileItem = mockProfile(profileId)

        whenever(profileManager.getProfile(profileId)).thenReturn(profile)
        whenever(widgetManager.hasProfileWidgets(profileId)).thenReturn(false)

        // when
        viewModel.onCreated(profileId)
        viewModel.onDeleteProfile()

        // then
        val action = viewModel.editAction.first()
        assertNotNull(action)
        assertTrue(action is CreateAccountViewEvent.ConfirmDelete)
        assertFalse((action as CreateAccountViewEvent.ConfirmDelete).hasWidgets)
        verify(profileManager).getProfile(profileId)
        verify(widgetManager).hasProfileWidgets(profileId)
        verifyNoMoreInteractions(profileManager, widgetManager)
    }

    @Test
    fun shouldRemoveProfile() = runBlocking {
        // given
        val profileId = 234L
        val profile: AuthProfileItem = mockProfile(profileId)

        whenever(profileManager.getProfile(profileId)).thenReturn(profile)

        // when
        viewModel.onCreated(profileId)
        viewModel.onDeleteProfile()

        // then
        val action = viewModel.editAction.first()
        assertNotNull(action)
        assertTrue(action is CreateAccountViewEvent.ReturnFromCreateAccount)
        assertTrue((action as CreateAccountViewEvent.ReturnFromCreateAccount).authSettingChanged)
        verify(profileManager).getProfile(profileId)
        verify(profileManager).removeProfile(profileId)
        verifyNoMoreInteractions(profileManager)
        verifyZeroInteractions(widgetManager)
    }

    private fun mockProfile(profileId: Long): AuthProfileItem {
        val profile: AuthProfileItem = mock()
        whenever(profile.authInfo).thenReturn(
                AuthInfo(emailAuth = true, serverAutoDetect = true))
        whenever(profile.name).thenReturn("some name")
        whenever(profile.id).thenReturn(profileId)

        return profile
    }
}