package org.supla.android.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import org.supla.android.MainActivity
import org.supla.android.NavigationActivity
import org.supla.android.R
import org.supla.android.cfg.CfgActivity
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment
import javax.inject.Inject

@ActivityScoped
class CfgActivityNavigator @Inject constructor(@ActivityContext private val activityContext: Context) {

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
