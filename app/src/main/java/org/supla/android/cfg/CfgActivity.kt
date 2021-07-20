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
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.supla.android.*
import org.supla.android.NavigationActivity.INTENTSENDER
import org.supla.android.NavigationActivity.INTENTSENDER_MAIN
import org.supla.android.NavigationActivity.showStatus
import org.supla.android.databinding.ActivityCfgBinding


//NavigationActivity
class CfgActivity: AppCompatActivity() {

    private lateinit var viewModel: CfgViewModel
    private lateinit var binding: ActivityCfgBinding

    override fun onCreate(sis: Bundle?) {
        super.onCreate(sis)

	viewModel = CfgViewModel(PrefsCfgRepositoryImpl(this))
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cfg)
            binding.viewModel = viewModel
            binding.lifecycleOwner = this
            binding.cfgAdvanced.viewModel = viewModel
            binding.cfgBasic.viewModel = viewModel

            viewModel.nextAction.observe(this) {
                it?.let { handleNavigationDirective(it) }
            }
            viewModel.didSaveConfig.observe(this) {
                it?.let { SuplaApp.getApp().SuplaClientInitIfNeed(this).reconnect() }
            }


        var type = SuplaApp.getApp().typefaceOpenSansRegular
        val edbg = resources.getDrawable(R.drawable.rounded_edittext)
        arrayOf(binding.cfgAdvanced.edServerAddr, binding.cfgAdvanced.edAccessID,
            binding.cfgAdvanced.edAccessIDpwd, binding.cfgBasic.cfgEmail)
            .forEach {
                it.setOnFocusChangeListener { v, hasFocus ->
                    if(!hasFocus) { hideKeyboard(v) }
                }
                it.setTypeface(type)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    it.setBackground(edbg)
                } else {
                    it.setBackgroundDrawable(edbg)
                }
            }
        arrayOf(binding.cfgBasic.cfgLabelEmail).forEach {
            it.setTypeface(type)
        }
        type = SuplaApp.getApp().typefaceQuicksandRegular
        arrayOf(binding.cfgBasic.cfgLabelTitleBasic,
        binding.cfgAdvanced.cfgLabelTitleAdv).forEach {
            it.setTypeface(type)
        }

        binding.cfgBasic.cfgCreateAccount.setTypeface(type, Typeface.BOLD)
        binding.cfgCbAdvanced.setTypeface(type)
    }

    override fun onBackPressed() {
        showMain()
        finish()
    }

    fun hideKeyboard(v: View) {
        val service = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        service?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
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
