package org.supla.android.cfg

import android.app.Activity
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.supla.android.R
import org.supla.android.NavigationActivity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.view.forEach
import org.supla.android.databinding.ActivityCfgBinding
import org.supla.android.SuplaApp


class CfgActivity: NavigationActivity() {

    private lateinit var viewModel: CfgViewModel
    private lateinit var binding: ActivityCfgBinding

    override fun onCreate(sis: Bundle?) {
        super.onCreate(sis)
        viewModel = CfgViewModel(PrefsCfgRepositoryImpl(this, dbHelper))
        setContentView(R.layout.activity_cfg)

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

    override fun Inflate(resID: Int, root: ViewGroup?): View {
        /*
         NavigationActivity overrides a lot on view creation code path and discards normal
         mvvm binding configuration on its way. While the ultimate solution is to refactor
         the whole navigation code to mvvm, currently the project changes besides cfg activity
         should be minimal so we need this hack for now.
         */

        if(resID == R.layout.activity_cfg) {
            binding = DataBindingUtil.inflate(layoutInflater, resID, root, true)
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

            return binding.root
        } else {
            return super.Inflate(resID, root)
        }
    }

    override fun onBackPressed() {
        showMain(this)
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
                showStatus(this)
                finish()
            }
            CfgViewModel.NavigationFlow.MAIN -> {
                showMain(this)
                finish()
            }
        }
    }
}
