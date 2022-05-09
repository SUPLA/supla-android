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
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.fragment.NavHostFragment
import android.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.databinding.ActivityCfgBinding
import org.supla.android.*
import org.supla.android.NavigationActivity.INTENTSENDER
import org.supla.android.NavigationActivity.INTENTSENDER_MAIN
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.AppBar
import javax.inject.Inject


@AndroidEntryPoint
class CfgActivity: AppCompatActivity() {

    companion object {
        const val ACTION_PROFILE = "org.supla.android.CfgActivity.PROFILE"
        const val ACTION_CONFIG = "org.supla.android.CfgActivity.CONFIG"
        const val ACTION_AUTH = "org.supla.android.CfgActivity.AUTH"
    }

    @Inject lateinit var profileManager: ProfileManager
    private lateinit var binding: ActivityCfgBinding
    private var shouldShowBack = false

    override fun onCreate(sis: Bundle?) {
        super.onCreate(sis)

        SuplaApp.getApp().initTypefaceCollection(this)

        val factory = CfgViewModelFactory(PrefsCfgRepositoryImpl(this), profileManager)
        val provider = ViewModelProvider(this, factory)
        val navCoordinator = provider.get(NavCoordinator::class.java)

        

	      val viewModel = provider.get(CfgViewModel::class.java)

        val navToolbar: AppBar
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cfg)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        navToolbar = binding.navToolbar


        navCoordinator.navAction.observe(this) {
            it?.let { handleNavigationDirective(it) }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
           window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
           window.setStatusBarColor(ResourcesCompat.getColor(getResources(),
               R.color.splash_bg, null));
        }
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)

        setSupportActionBar(navToolbar)
        /* FIXME: this workaround is to be removed when navigation controller
           is implemented in entire app. */
        val action = getIntent().getAction()
        val startLoc = when(action) {
            ACTION_CONFIG -> R.id.cfgMain
            ACTION_AUTH -> R.id.cfgAuth
            ACTION_PROFILE -> R.id.cfgProfiles
            else -> null
        }

        if(startLoc != null) {
            val args: Bundle?
            /* Reconfigure navigation graph to dynamic
               start location */
              if(action == ACTION_AUTH) {
                  val profileId = profileManager.getCurrentProfile().id
                  args = AuthFragmentArgs(profileId, true, true).toBundle()
              } else {
                  args = null
              }
              
            graph.setStartDestination(startLoc)
            navController.setGraph(graph, args)
        }

        val cfg = AppBarConfiguration(navController.graph)
        NavigationUI.setupWithNavController(navToolbar,
                                            navController,
                                            cfg)

        navController.addOnDestinationChangedListener() { ctrl, dest, arg ->
                                                              configureNavBar()
        }
    }

    override fun onResume() {
        super.onResume()
        val navController = findNavController(R.id.nav_host_fragment)
        val dest = navController?.currentDestination
        if(dest != null) {
            // Temporary hack to match look and feel of the rest of the app
            // prior to moving everything into navigation graph.
            if(dest.id == R.id.cfgAuth) {
                getSupportActionBar()?.setTitle(dest.label ?: "")
            } else {
                getSupportActionBar()?.setSubtitle(dest.label ?: "")
            }
        }

        val sender = getIntent().getStringExtra(INTENTSENDER)
        if(sender != null && sender.equals(INTENTSENDER_MAIN)) {
            // show back button
            shouldShowBack = true
            binding.navToolbar.setNavigationIcon(R.drawable.navbar_back)
            binding.navToolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

    }

    private fun configureNavBar() {
        if(shouldShowBack) {
            binding.navToolbar.setNavigationIcon(R.drawable.navbar_back)
        }
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        val dest = navController?.currentDestination

        if(!navController.navigateUp()) {
            if(Preferences(this).configIsSet()) {
                showMain()
            }
            finish()
        }

    }

    fun handleNavigationDirective(what: NavigationFlow) {
        /*
            At some point we shoud introduce navigation pattern from archiecture components.
            Before that happens, we use a bit awkward technique to drive navigation flow.
         */
        when(what) {
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
                    .setPositiveButton(android.R.string.ok) {
                        dlg, what ->
                            dlg.cancel()
                    }.create().show()
                

            }
            NavigationFlow.MAIN -> {
                showMain()
                finish()
            }
            NavigationFlow.OPEN_PROFILES -> {
                findNavController( R.id.nav_host_fragment).navigate(R.id.cfgProfiles)
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

        if (client != null
            && client.registered()
        ) {
            showActivity(this, MainActivity::class.java, 0)
        } else {
            showStatus()
        }


    }

    private fun showCreateAccount() {
        showActivity(this, CreateAccountActivity::class.java, 0)

    }

    private fun showStatus() {
        showActivity(this, StatusActivity::class.java, 0)

    }

    private fun showActivity(sender: Activity, cls: Class<*>, flags: Int) {
        val i = Intent(sender.baseContext, cls)
        i.flags = if (flags == 0) Intent.FLAG_ACTIVITY_REORDER_TO_FRONT else flags
        i.putExtra(INTENTSENDER, if (sender is MainActivity) INTENTSENDER_MAIN else "")
        sender.startActivity(i)
        sender.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
