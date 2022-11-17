package org.supla.android.cfg
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
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.*
import org.supla.android.NavigationActivity.INTENTSENDER
import org.supla.android.NavigationActivity.INTENTSENDER_MAIN
import org.supla.android.data.TemperatureFormatter
import org.supla.android.databinding.ActivityCfgBinding
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.AppBar
import javax.inject.Inject

@AndroidEntryPoint
class CfgActivity : AppCompatActivity() {

  companion object {
    const val ACTION_PROFILE = "org.supla.android.CfgActivity.PROFILE"
    const val ACTION_CONFIG = "org.supla.android.CfgActivity.CONFIG"
    const val ACTION_AUTH = "org.supla.android.CfgActivity.AUTH"
  }

  private val viewModel: CfgViewModel by viewModels()

  @Inject
  lateinit var profileManager: ProfileManager

  @Inject
  lateinit var temperatureFormatter: TemperatureFormatter

  private lateinit var binding: ActivityCfgBinding
  private lateinit var navCoordinator: NavCoordinator
  private var shouldShowBack = false

  private val navToolbar: AppBar
    get() = binding.navToolbar

  override fun onCreate(sis: Bundle?) {
    super.onCreate(sis)

    SuplaApp.getApp().initTypefaceCollection(this)

    val factory =
      CfgViewModelFactory(profileManager)
    val provider = ViewModelProvider(this, factory)
    navCoordinator = provider[NavCoordinator::class.java]

    binding = DataBindingUtil.setContentView(this, R.layout.activity_cfg)
    binding.viewModel = viewModel
    binding.lifecycleOwner = this

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = ResourcesCompat.getColor(
        resources,
        R.color.splash_bg,
        null
      )
    }

    setSupportActionBar(navToolbar)

    navCoordinator.navAction.observe(this) {
      it?.let { handleNavigationDirective(it) }
    }

    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
      as NavHostFragment
    val navController = navHostFragment.navController
    val navInflater = navController.navInflater
    val graph = navInflater.inflate(R.navigation.nav_graph)

    val action = intent.action
    val startLoc = when (action) {
      ACTION_CONFIG -> R.id.cfgMain
      ACTION_AUTH -> R.id.cfgAuth
      ACTION_PROFILE -> R.id.cfgProfiles
      else -> null
    }

    if (startLoc != null) {
      /* Reconfigure navigation graph to dynamic start location */
      val args: Bundle? = if (action == ACTION_AUTH) {
        val profileId = profileManager.getCurrentProfile().id
        AuthFragmentArgs(profileId, asPopup = true).toBundle()
      } else {
        null
      }

      graph.setStartDestination(startLoc)
      navController.setGraph(graph, args)
    }

    val cfg = AppBarConfiguration(navController.graph)
    NavigationUI.setupWithNavController(
      navToolbar,
      navController,
      cfg
    )

    navController.addOnDestinationChangedListener { _, _, _ -> configureNavBar() }
  }

  override fun onResume() {
    super.onResume()
    val navController = findNavController(R.id.nav_host_fragment)
    val dest = navController.currentDestination
    if (dest != null) {
      // Temporary hack to match look and feel of the rest of the app
      // prior to moving everything into navigation graph.
      if (dest.id == R.id.cfgAuth) {
        supportActionBar?.setTitle(dest.label ?: "")
      } else {
        supportActionBar?.setSubtitle(dest.label ?: "")
      }
    }

    val sender = intent.getStringExtra(INTENTSENDER)
    if (sender != null && sender == INTENTSENDER_MAIN) {
      // show back button
      shouldShowBack = true
      binding.navToolbar.setNavigationIcon(R.drawable.navbar_back)
      binding.navToolbar.setNavigationOnClickListener {
        onBackPressed()
      }
    }
  }

  private fun configureNavBar() {
    if (shouldShowBack) {
      binding.navToolbar.setNavigationIcon(R.drawable.navbar_back)
    }
  }

  override fun onBackPressed() {
    val navController = findNavController(R.id.nav_host_fragment)
    navController.currentDestination

    if (!navController.navigateUp()) {
      if (Preferences(this).configIsSet()) {
        showMain()
      }
      finish()
    }
  }

  fun navigateToReordering() {
    navCoordinator.navigate(NavigationFlow.LOCATION_REORDERING)
  }

  private fun handleNavigationDirective(what: NavigationFlow) {
    /*
        At some point we should introduce navigation pattern from architecture components.
        Before that happens, we use a bit awkward technique to drive navigation flow.
     */
    when (what) {
      NavigationFlow.CREATE_ACCOUNT -> showCreateAccount()
      NavigationFlow.STATUS -> {
        SuplaApp.getApp().SuplaClientInitIfNeed(this).reconnect()
        showStatus()
        finish()
      }
      NavigationFlow.BASIC_MODE_ALERT -> {
        AlertDialog.Builder(this)
          .setTitle(R.string.basic_profile_warning)
          .setMessage(R.string.basic_config_unavailable)
          .setPositiveButton(android.R.string.ok) { dlg, _ ->
            dlg.cancel()
          }.create().show()
      }
      NavigationFlow.MAIN -> {
        showMain()
        finish()
      }
      NavigationFlow.OPEN_PROFILES -> {
        findNavController(R.id.nav_host_fragment).navigate(R.id.cfgProfiles)
      }

      NavigationFlow.LOCATION_REORDERING -> {
        findNavController(R.id.nav_host_fragment).navigate(R.id.cfgLocationOrdering)
      }

      NavigationFlow.BACK -> {
        findNavController(R.id.nav_host_fragment).navigateUp()
      }
    }
  }

  // Temporary navigation methods
  private fun showMain() {
    val client = SuplaApp.getApp().suplaClient

    if (client != null && client.registered()) {
      showActivity(this, MainActivity::class.java)
    } else {
      showStatus()
    }
  }

  private fun showCreateAccount() {
    showActivity(this, CreateAccountActivity::class.java)
  }

  private fun showStatus() {
    showActivity(this, StatusActivity::class.java)
  }

  private fun showActivity(sender: Activity, cls: Class<*>) {
    val i = Intent(sender.baseContext, cls)
    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    i.putExtra(INTENTSENDER, if (sender is MainActivity) INTENTSENDER_MAIN else "")
    sender.startActivity(i)
    sender.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
  }
}
