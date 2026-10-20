package org.supla.android.extensions
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
import androidx.work.Worker
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.core.infrastructure.suplaclient.SingleCallProvider
import org.supla.android.di.entrypoints.AppWidgetManagerEntryPoint
import org.supla.android.di.entrypoints.SingleCallProviderEntryPoint
import org.supla.android.di.entrypoints.WidgetPreferencesEntryPoint
import org.supla.android.widget.WidgetPreferences

fun Worker.getSingleCallProvider(): SingleCallProvider =
  EntryPointAccessors.fromApplication(
    applicationContext,
    SingleCallProviderEntryPoint::class.java
  ).provideSingleCallProvider()

fun Worker.getWidgetPreferences(): WidgetPreferences =
  EntryPointAccessors.fromApplication(
    applicationContext,
    WidgetPreferencesEntryPoint::class.java
  ).provideWidgetPreferences()

fun Worker.getAppWidgetManager(): AppWidgetManager =
  EntryPointAccessors.fromApplication(
    applicationContext,
    AppWidgetManagerEntryPoint::class.java
  ).provideAppWidgetManager()
