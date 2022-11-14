package org.supla.android.profile

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.DbHelper
import org.supla.android.scenes.SceneEventsManager
import org.supla.android.widget.WidgetVisibilityHandler

private const val INITIAL_PROFILE_ID = -1L
private const val DEVICE_ID = "device id"

@RunWith(MockitoJUnitRunner::class)
class MultiAccountProfileManagerTest {

    @Mock
    private lateinit var dbHelper: DbHelper

    @Mock
    private lateinit var profileRepository: ProfileRepository

    @Mock
    private lateinit var widgetVisibilityHandler: WidgetVisibilityHandler

    @Mock
    private lateinit var sceneEventsManager: SceneEventsManager

    private val profileIdHolder = ProfileIdHolder(INITIAL_PROFILE_ID)

    private lateinit var profileManager: MultiAccountProfileManager

    @Before
    fun setUp() {
        profileManager = MultiAccountProfileManager(dbHelper, DEVICE_ID, profileRepository, profileIdHolder, widgetVisibilityHandler, sceneEventsManager)
    }

    @Test
    fun `should remove profile`() {
        // given
        val profileId = 123L

        // when
        profileManager.removeProfile(profileId)

        // then
        assertEquals(INITIAL_PROFILE_ID, profileIdHolder.profileId)

        verify(profileRepository).deleteProfile(profileId)
        verify(widgetVisibilityHandler).onProfileRemoved(profileId)
        verifyNoMoreInteractions(profileRepository, widgetVisibilityHandler)
        verifyZeroInteractions(dbHelper)
    }

    @Test
    fun `should change profile id in holder when profile activated`() {
        // given
        val oldId = 123L
        val newId = 234L
        val profile: AuthProfileItem = mock()
        whenever(profile.id).thenReturn(oldId)
        whenever(profile.isActive).thenReturn(true)
        whenever(profileRepository.allProfiles).thenReturn(listOf(profile))
        whenever(profileRepository.setProfileActive(newId)).thenReturn(true)

        val suplaApp: SuplaApp = mock()
        mockkStatic(SuplaApp::class)
        every { SuplaApp.getApp() } returns suplaApp

        // when
        val result = profileManager.activateProfile(newId)

        // then
        assertTrue(result)
        assertEquals(newId, profileIdHolder.profileId)

        verify(profileRepository).allProfiles
        verify(profileRepository).setProfileActive(newId)
        verify(dbHelper).loadUserIconsIntoCache()
        verifyNoMoreInteractions(profileRepository, dbHelper)
        verifyZeroInteractions(widgetVisibilityHandler)
    }
}