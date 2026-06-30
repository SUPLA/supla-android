package org.supla.android.ui.views
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

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import org.supla.android.main.scaffold.LocalScaffoldPadding

@Composable
fun LegacyFragmentScreen(
  fragmentClass: Class<out Fragment>,
  arguments: Bundle? = null
) {
  val context = LocalContext.current
  val fragmentManager = (context as FragmentActivity).supportFragmentManager
  val containerId = remember { View.generateViewId() }

  AndroidView(
    modifier = Modifier
      .fillMaxSize()
      .padding(LocalScaffoldPadding.current),
    factory = {
      FragmentContainerView(it).apply {
        id = containerId
      }
    },
    update = {
      if (fragmentManager.findFragmentById(containerId) == null) {
        fragmentManager.beginTransaction()
          .replace(containerId, fragmentClass, arguments)
          .commit()
      }
    }
  )

  DisposableEffect(containerId) {
    onDispose {
      fragmentManager.findFragmentById(containerId)?.let {
        fragmentManager.beginTransaction()
          .remove(it)
          .commitAllowingStateLoss()
      }
    }
  }
}
