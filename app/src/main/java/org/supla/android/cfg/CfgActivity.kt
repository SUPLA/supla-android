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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import org.supla.android.*
import org.supla.android.NavigationActivity.INTENTSENDER
import org.supla.android.NavigationActivity.INTENTSENDER_MAIN
import org.supla.android.NavigationActivity.showStatus
import org.supla.android.databinding.ActivityCfgBinding


class CfgActivity: AppCompatActivity() {

    private lateinit var viewModel: CfgViewModel
    private lateinit var binding: ActivityCfgBinding

    override fun onCreate(sis: Bundle?) {
        super.onCreate(sis)

	val factory = CfgViewModelFactory(PrefsCfgRepositoryImpl(this))
	viewModel = ViewModelProvider(this, factory).get(CfgViewModel::class.java)
	
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cfg)
            binding.viewModel = viewModel
            binding.lifecycleOwner = this

            viewModel.nextAction.observe(this) {
                it?.let { handleNavigationDirective(it) }
            }
            viewModel.didSaveConfig.observe(this) {
                it?.let { SuplaApp.getApp().SuplaClientInitIfNeed(this).reconnect() }
            }
    }

    override fun onBackPressed() {
        showMain()
        finish()
    }

    fun handleNavigationDirective(what: CfgViewModel.NavigationFlow) {
        /*
            At some point we shoud introduce navigation pattern from archiecture components.
            Before that happens, we use a bit awkward technique to drive navigation flow.
         */
        when(what) {
            CfgViewModel.NavigationFlow.CREATE_ACCOUNT -> showCreateAccount()
            CfgViewModel.NavigationFlow.STATUS -> {
                showStatus()
                finish()
            }
            CfgViewModel.NavigationFlow.MAIN -> {
                showMain()
                finish()
            }
            CfgViewModel.NavigationFlow.OPEN_PROFILES -> {
                findNavController( R.id.nav_host_fragment).navigate(R.id.openProfiles)
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
