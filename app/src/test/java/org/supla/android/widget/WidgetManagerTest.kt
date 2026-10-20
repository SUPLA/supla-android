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
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.supla.android.extensions.getAllWidgetIds
import org.supla.android.extensions.getOnOffWidgetIds
import org.supla.android.extensions.getSingleWidgetIds
import org.supla.android.lib.actions.SubjectType
import org.supla.core.shared.data.model.general.SuplaFunction

class WidgetManagerTest {
  @MockK
  private lateinit var context: Context

  @MockK
  private lateinit var widgetPreferences: WidgetPreferences

  private lateinit var appWidgetManager: AppWidgetManager

  private lateinit var manager: WidgetManager

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
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
    every { widgetPreferences.getWidgetConfiguration(widgetId) } returns widgetConfiguration
    every { widgetPreferences.setWidgetConfiguration(widgetId, match { it.profileId == INVALID_LONG }) } just Runs

    every { context.sendBroadcast(any()) } just Runs

    every { appWidgetManager.getAllWidgetIds(context) } returns intArrayOf(widgetId)
    every { appWidgetManager.getOnOffWidgetIds(context) } returns intArrayOf(widgetId)
    every { appWidgetManager.getSingleWidgetIds(context) } returns intArrayOf()
    manager = WidgetManager(context, appWidgetManager, widgetPreferences)

    // when
    manager.onProfileRemoved(profileId)

    // then
    verify {
      widgetPreferences.getWidgetConfiguration(widgetId)
      widgetPreferences.setWidgetConfiguration(widgetId, match { it.profileId == INVALID_LONG })
      context.sendBroadcast(any())
      appWidgetManager.getAllWidgetIds(context)
      appWidgetManager.getOnOffWidgetIds(context)
      appWidgetManager.getSingleWidgetIds(context)
    }
    confirmVerified(appWidgetManager, widgetPreferences, context)
  }
}
