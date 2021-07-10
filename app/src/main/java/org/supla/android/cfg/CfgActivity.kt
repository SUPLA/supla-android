package org.supla.android.cfg

import android.os.Build
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.supla.android.R
import org.supla.android.NavigationActivity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import org.supla.android.databinding.ActivityCfgBinding
import org.supla.android.SuplaApp


class CfgActivity: NavigationActivity() {

    private lateinit var viewModel: CfgViewModel
    private lateinit var binding: ActivityCfgBinding

    override fun onCreate(sis: Bundle?) {
        super.onCreate(sis)
        viewModel = CfgViewModel(PrefsCfgRepositoryImpl(this))
        setContentView(R.layout.activity_cfg)

        val type = SuplaApp.getApp().typefaceOpenSansRegular
        val edbg = resources.getDrawable(R.drawable.rounded_edittext)
        arrayOf(binding.cfgAdvanced.edServerAddr, binding.cfgAdvanced.edAccessID,
            binding.cfgAdvanced.edAccessIDpwd, binding.cfgBasic.cfgEmail)
            .forEach {
                // TODO: setOnFocusChange
                it.setTypeface(type)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    it.setBackground(edbg)
                } else {
                    it.setBackgroundDrawable(edbg)
                }
            }
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
            return binding.root
        } else {
            return super.Inflate(resID, root)
        }
    }

    override fun onBackPressed() {
        showMain(this)
        finish()
    }
}
