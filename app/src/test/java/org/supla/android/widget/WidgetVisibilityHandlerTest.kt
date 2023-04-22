package org.supla.android.widget
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
import org.supla.android.widget.shared.configuration.ItemType

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

    val widgetConfiguration = WidgetConfiguration(
      channelId,
      ItemType.CHANNEL,
      null,
      0,
      "0",
      profileId,
      true,
      -1,
      1,
      2
    )
    whenever(widgetPreferences.getWidgetConfiguration(widgetId)).thenReturn(widgetConfiguration)

    every { appWidgetManager.getAllWidgetIds(context) } returns intArrayOf(widgetId)
    handler = WidgetVisibilityHandler(context, appWidgetManager, widgetPreferences)

    // when
    handler.onProfileRemoved(profileId)

    // then
    verify(widgetPreferences).getWidgetConfiguration(widgetId)
    verify(widgetPreferences).setWidgetConfiguration(
      eq(widgetId),
      argThat { conf -> conf.profileId == INVALID_PROFILE_ID }
    )
    verify(context).sendBroadcast(any())
    verifyNoMoreInteractions(widgetPreferences, context)
    verifyZeroInteractions(appWidgetManager)
  }
}
