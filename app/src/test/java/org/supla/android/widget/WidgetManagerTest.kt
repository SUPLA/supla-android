package org.supla.android.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.extensions.getAllWidgetIds
import org.supla.android.extensions.getOnOffWidgetIds
import org.supla.android.extensions.getSingleWidgetIds
import org.supla.android.lib.actions.SubjectType
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class WidgetManagerTest {
  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var widgetPreferences: WidgetPreferences

  private lateinit var appWidgetManager: AppWidgetManager

  private lateinit var manager: WidgetManager

  @Before
  fun setUp() {
    appWidgetManager = mockk()
    mockkStatic("org.supla.android.extensions.WidgetExtensionsKt")
    manager = WidgetManager(context, appWidgetManager, widgetPreferences)
  }

  @Test
  fun `should not observe widget when owned profile removed`() {
    // given
    val widgetId = 123
    val channelId = 234
    val profileId = 345L

    val widgetConfiguration = WidgetConfiguration(
      channelId,
      SubjectType.CHANNEL,
      null,
      SuplaFunction.NONE,
      "0",
      profileId,
      true,
      null,
      1,
      2
    )
    whenever(widgetPreferences.getWidgetConfiguration(widgetId)).thenReturn(widgetConfiguration)

    every { appWidgetManager.getAllWidgetIds(context) } returns intArrayOf(widgetId)
    every { appWidgetManager.getOnOffWidgetIds(context) } returns intArrayOf(widgetId)
    every { appWidgetManager.getSingleWidgetIds(context) } returns intArrayOf()
    manager = WidgetManager(context, appWidgetManager, widgetPreferences)

    // when
    manager.onProfileRemoved(profileId)

    // then
    verify(widgetPreferences).getWidgetConfiguration(widgetId)
    verify(widgetPreferences).setWidgetConfiguration(
      eq(widgetId),
      argThat { conf -> conf.profileId == INVALID_LONG }
    )
    verify(context).sendBroadcast(any())
    io.mockk.verify {
      appWidgetManager.getAllWidgetIds(context)
      appWidgetManager.getOnOffWidgetIds(context)
      appWidgetManager.getSingleWidgetIds(context)
    }
    confirmVerified(appWidgetManager)
    verifyNoMoreInteractions(widgetPreferences, context)
  }
}
