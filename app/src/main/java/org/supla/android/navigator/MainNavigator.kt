package org.supla.android.navigator

import android.app.Activity
import android.content.Context
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
class MainNavigator @Inject constructor(@ActivityContext private val activityContext: Context) {

  private val navController: NavController
    get() = (activityContext as Activity).findNavController(R.id.nav_host_fragment)

  fun navigateTo(@IdRes destinationId: Int, bundle: Bundle? = null) {
    navController.navigate(destinationId, bundle)
  }

  fun replaceTo(@IdRes destinationId: Int) {
    navController.navigate(destinationId, null, NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(R.id.channel_list_fragment, true).build())
  }

  fun back(): Boolean = navController.navigateUp()
}