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
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import org.supla.android.NavigationActivity
import org.supla.android.R
import org.supla.android.cfg.CfgActivity
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment
import org.supla.android.main.MainActivity
import javax.inject.Inject

@ActivityScoped
class CfgActivityNavigator @Inject constructor(@param:ActivityContext private val activityContext: Context) {

  private val navController: NavController
    get() = (activityContext as Activity).findNavController(R.id.nav_host_fragment)

  fun navigateTo(@IdRes destinationId: Int, bundle: Bundle? = null) {
    navController.navigate(destinationId, bundle)
  }

  fun navigateToMain() {
    showActivity((activityContext as CfgActivity), MainActivity::class.java)
    activityContext.finish()
  }

  fun navigateToCreateAccount() {
    navigateTo(R.id.createAccount)
  }

  fun navigateToDeleteAccount(serverAddress: String?, destination: DeleteAccountWebFragment.EndDestination) {
    navController.popBackStack(R.id.cfgProfiles, false)
    navigateTo(R.id.webContentDeleteAccount, DeleteAccountWebFragment.bundle(serverAddress, destination))
  }

  fun restartAppStack() {
    Intent(activityContext, MainActivity::class.java).also {
      it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
      activityContext.startActivity(it)
    }
    (activityContext as CfgActivity).finish()
  }

  fun back(): Boolean = navController.navigateUp()

  fun backOrClose() {
    if (!navController.navigateUp()) {
      navigateToMain()
    }
  }

  private fun showActivity(sender: Activity, cls: Class<*>) {
    val i = Intent(sender.baseContext, cls)
    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    i.putExtra(NavigationActivity.INTENT_SENDER, if (sender is MainActivity) NavigationActivity.INTENT_SENDER_MAIN else "")
    sender.startActivity(i)
    sender.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
  }
}
