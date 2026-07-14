package org.supla.android.main
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
import android.provider.Settings
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import org.supla.android.NavigationActivity
import org.supla.android.R
import org.supla.android.ZWaveConfigurationWizardActivity
import org.supla.android.cfg.CfgActivity
import org.supla.android.extensions.findActivity
import java.lang.ref.WeakReference
import javax.inject.Inject

@ActivityScoped
class MainComposeNavigator @Inject constructor() {
  private lateinit var activityContext: WeakReference<Context>
  private val backStack = MutableStateFlow<NavBackStack<NavKey>?>(null)

  fun current(): NavKey? = backStack.value?.lastOrNull()

  fun back() {
    if (backStack.value?.size == 1) {
      activityContext.get()?.findActivity()?.finish()
    } else {
      backStack.value?.removeLastOrNull()
    }
  }

  fun navigateTo(destination: NavKey) {
    backStack.value?.add(destination)
  }

  fun replaceTop(destination: NavKey) {
    backStack.value?.apply {
      clear()
      add(destination)
    }
  }

  fun navigateToStatus() {
    backStack.value?.let { backStack ->
      (backStack.lastOrNull() as? MainRoute)?.let {
        if (!it.screenTakeoverAllowed) {
          return // No takeover allowed
        }
      }

      backStack.clear()
      backStack.add(MainRoute.Status)
    }
  }

  fun replace(destination: NavKey) {
    backStack.value?.apply {
      removeLastOrNull()
      add(destination)
    }
  }

  fun navigateToNewProfile() {
    withContext {
      val intent = Intent(this, CfgActivity::class.java).also {
        it.action = CfgActivity.ACTION_AUTH
        it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        it.putExtra(NavigationActivity.INTENT_SENDER, NavigationActivity.INTENT_SENDER_MAIN)
      }
      startActivity(intent)
      (this as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
  }

  fun navigateToProfiles() {
    withContext {
      val intent = Intent(this, CfgActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        it.putExtra(NavigationActivity.INTENT_SENDER, NavigationActivity.INTENT_SENDER_MAIN)
      }
      startActivity(intent)
      (this as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
  }

  fun navigateToZWaveConfigurationWizard() {
    withContext {
      val intent = Intent(this, ZWaveConfigurationWizardActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        it.putExtra(NavigationActivity.INTENT_SENDER, NavigationActivity.INTENT_SENDER_MAIN)
      }
      startActivity(intent)
      (this as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
  }

  fun navigateToCloudExternal() {
    withContext {
      startActivity(Intent(Intent.ACTION_VIEW, resources.getString(R.string.cloud_url).toUri()))
    }
  }

  fun navigateToBetaCloudExternal() {
    withContext {
      startActivity(Intent(Intent.ACTION_VIEW, resources.getString(R.string.beta_cloud_url).toUri()))
    }
  }

  fun navigateToWeb(url: Uri) {
    withContext {
      startActivity(Intent(Intent.ACTION_VIEW, url))
    }
  }

  fun navigateToSuplaOrgExternal() {
    withContext {
      navigateToWeb(getString(R.string.homepage_url).toUri())
    }
  }

  fun navigateToNfcSettings() {
    withContext {
      startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    }
  }

  fun navigateToSystemSettings() {
    withContext {
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(this)
      }
    }
  }

  fun bind(activity: Activity, backStack: NavBackStack<NavKey>) {
    this.activityContext = WeakReference(activity)
    this.backStack.value = backStack
  }

  fun unbind(backStack: NavBackStack<NavKey>) {
    if (this.backStack.value == backStack) {
      this.backStack.value = null
    }
  }

  fun withContext(runnable: Context.() -> Unit) {
    activityContext.get()?.let { runnable.invoke(it) }
  }
}

val LocalNavigator = staticCompositionLocalOf<MainComposeNavigator?> { null }
