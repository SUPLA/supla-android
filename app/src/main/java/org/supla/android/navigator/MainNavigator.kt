package org.supla.android.navigator
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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import org.supla.android.R
import javax.inject.Inject

@ActivityScoped
class MainNavigator @Inject constructor(@param:ActivityContext private val activityContext: Context) {

  private val navController: NavController
    get() = (activityContext as Activity).findNavController(R.id.nav_host_fragment)

  fun navigateTo(@IdRes destinationId: Int, bundle: Bundle? = null) {
    navController.navigate(destinationId, bundle, defaultAnimationOptions)
  }

  fun navigateToWeb(url: Uri) {
    activityContext.startActivity(Intent(Intent.ACTION_VIEW, url))
  }

  private val defaultAnimationOptions = NavOptions.Builder()
    .setEnterAnim(R.anim.slide_left_in)
    .setExitAnim(R.anim.slide_left_out)
    .setPopEnterAnim(R.anim.slide_right_in)
    .setPopExitAnim(R.anim.slide_right_out)
    .build()
}
