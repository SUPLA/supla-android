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
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import org.supla.android.NavigationActivity
import org.supla.android.R
import org.supla.android.cfg.CfgActivity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.iterateVisibleFragments
import org.supla.android.features.details.legacydetail.LegacyDetailFragment
import org.supla.android.usecases.details.LegacyDetailType
import javax.inject.Inject

@ActivityScoped
class MainNavigator @Inject constructor(@ActivityContext private val activityContext: Context) {

  private val navController: NavController
    get() = (activityContext as Activity).findNavController(R.id.nav_host_fragment)

  fun navigateTo(@IdRes destinationId: Int, bundle: Bundle? = null) {
    navController.navigate(destinationId, bundle, animationOptions)
  }

  fun navigateToLegacyDetails(remoteId: Int, legacyDetailType: LegacyDetailType, itemType: ItemType) {
    navController.navigate(
      R.id.legacy_detail_fragment,
      LegacyDetailFragment.bundle(remoteId, legacyDetailType, itemType),
      animationOptions
    )
  }

  fun navigateToAddWizard() {
    navigateTo(R.id.add_wizard_fragment)
  }

  fun navigateToProfiles() {
    val intent = Intent(activityContext, CfgActivity::class.java).also {
      it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      it.putExtra(NavigationActivity.INTENT_SENDER, NavigationActivity.INTENT_SENDER_MAIN)
    }
    activityContext.startActivity(intent)
    (activityContext as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
  }

  fun navigateToNewProfile() {
    val intent = Intent(activityContext, CfgActivity::class.java).also {
      it.action = CfgActivity.ACTION_AUTH
      it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      it.putExtra(NavigationActivity.INTENT_SENDER, NavigationActivity.INTENT_SENDER_MAIN)
    }
    activityContext.startActivity(intent)
    (activityContext as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
  }

  fun navigateToCloudExternal() {
    activityContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activityContext.resources.getString(R.string.cloud_url))))
  }

  fun navigateToBetaCloudExternal() {
    activityContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activityContext.resources.getString(R.string.beta_cloud_url))))
  }

  fun navigateToWeb(url: Uri) {
    activityContext.startActivity(Intent(Intent.ACTION_VIEW, url))
  }

  fun navigateToSuplaOrgExternal() {
    navigateToWeb(Uri.parse(activityContext.getString(R.string.homepage_url)))
  }

  fun back(): Boolean =
    navController.currentDestination?.id != R.id.main_fragment &&
      navController.currentDestination?.id != R.id.status_fragment &&
      navController.popBackStack()

  fun navigateToStatus() {
    if (navigationToStatusAllowed()) {
      if (navController.popBackStack(R.id.status_fragment, false).not()) {
        navController.navigate(R.id.status_fragment)
      }
    }
  }

  fun navigateToMain() {
    if (navController.popBackStack(R.id.main_fragment, false).not()) {
      navController.navigate(R.id.main_fragment)
    }
  }

  private val animationOptions = NavOptions.Builder()
    .setEnterAnim(R.anim.slide_left_in)
    .setExitAnim(R.anim.slide_left_out)
    .setPopEnterAnim(R.anim.slide_right_in)
    .setPopExitAnim(R.anim.slide_right_out)
    .build()

  private fun navigationToStatusAllowed(): Boolean {
    val (fragmentActivity) = guardLet(activityContext as? FragmentActivity) { return true }
    var result = true
    fragmentActivity.supportFragmentManager.iterateVisibleFragments {
      if (it is NavigationSubcontroller && !it.screenTakeoverAllowed()) {
        result = false
        true
      } else {
        false
      }
    }

    return result
  }
}
