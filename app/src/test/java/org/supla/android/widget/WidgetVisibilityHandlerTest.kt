package org.supla.android.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.extensions.getAllWidgetIds
import org.supla.android.profile.INVALID_PROFILE_ID

@RunWith(MockitoJUnitRunner::class)
class WidgetVisibilityHandlerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var appWidgetManager: AppWidgetManager

    @Mock
    private lateinit var widgetPreferences: WidgetPreferences

    private lateinit var handler: WidgetVisibilityHandler

    @Before
    fun setUp() {
        mockkStatic("org.supla.android.extensions.WidgetExtensionsKt")
        every { appWidgetManager.getAllWidgetIds(context) } returns intArrayOf()
        handler = WidgetVisibilityHandler(context, appWidgetManager, widgetPreferences)
    }

    @Test
    fun `should not observe widget when owned profile removed`() {
        // given
        val widgetId = 123
        val channelId = 234
        val profileId = 345L

        val widgetConfiguration = WidgetConfiguration(channelId, null, 0, 0, profileId, true)
        whenever(widgetPreferences.getWidgetConfiguration(widgetId)).thenReturn(widgetConfiguration)

        every { appWidgetManager.getAllWidgetIds(context) } returns intArrayOf(widgetId)
        handler = WidgetVisibilityHandler(context, appWidgetManager, widgetPreferences)

        // when
        handler.onProfileRemoved(profileId)

        // then
        verify(widgetPreferences).getWidgetConfiguration(widgetId)
        verify(widgetPreferences).setWidgetConfiguration(eq(widgetId),
                argThat { conf -> conf.profileId == INVALID_PROFILE_ID })
        verify(context).sendBroadcast(any())
        verifyNoMoreInteractions(widgetPreferences, context)
        verifyZeroInteractions(appWidgetManager)
    }
}