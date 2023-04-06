package org.supla.android.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import org.supla.android.*
import org.supla.android.cfg.CfgActivity
import org.supla.android.features.webcontent.WebContentFragment
import javax.inject.Inject

@ActivityScoped
class CfgActivityNavigator @Inject constructor(@ActivityContext private val activityContext: Context) {

  private val navController: NavController
    get() = (activityContext as AppCompatActivity).findNavController(R.id.nav_host_fragment)

  fun navigateTo(@IdRes destinationId: Int, bundle: Bundle? = null) {
    navController.navigate(destinationId, bundle)
  }

  fun navigateToMain() {
    showActivity((activityContext as CfgActivity), MainActivity::class.java)
  }

  fun navigateToCreateAccount() {
    val arguments = WebContentFragment.bundle(activityContext.getString(R.string.create_url))
    navigateTo(R.id.createAccount, arguments)
  }

  fun navigateToStatus() {
    showActivity((activityContext as CfgActivity), StatusActivity::class.java)
  }

  fun restartAppStack() {
    Intent(activityContext, StartActivity::class.java).also {
      it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
      activityContext.startActivity(it)
    }
  }

  fun back(): Boolean = navController.navigateUp()

  private fun showActivity(sender: Activity, cls: Class<*>) {
    val i = Intent(sender.baseContext, cls)
    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    i.putExtra(NavigationActivity.INTENTSENDER, if (sender is MainActivity) NavigationActivity.INTENTSENDER_MAIN else "")
    sender.startActivity(i)
    sender.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
  }
}