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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.supla.android.core.storage.ApplicationPreferences

fun Activity.setStatusBarColor(statusBarColorId: Int, navigationBarColorId: Int, isLight: Boolean) {
  val window = window
  window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
  window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
  val color = ResourcesCompat.getColor(resources, statusBarColorId, null)
  window.statusBarColor = color
  val navigationBarColor = ResourcesCompat.getColor(resources, navigationBarColorId, null)
  window.navigationBarColor = navigationBarColor

  with(WindowInsetsControllerCompat(window, window.decorView)) {
    isAppearanceLightStatusBars = isLight
    isAppearanceLightNavigationBars = true
  }
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.setupOrientationLock(preferences: ApplicationPreferences) {
  requestedOrientation =
    if (preferences.rotationEnabled) {
      ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    } else {
      ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
