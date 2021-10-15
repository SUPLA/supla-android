package org.supla.android.cfg

import android.content.Context
import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.FragmentAuthBinding

class AuthFragment: Fragment() {
        private val viewModel: CfgViewModel by activityViewModels()
    private lateinit var binding: FragmentAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth,
					  container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.cfgAdvanced.viewModel = viewModel
        binding.cfgBasic.viewModel = viewModel

	      var type = SuplaApp.getApp().typefaceOpenSansRegular

        arrayOf(binding.cfgAdvanced.edServerAddr,
                binding.cfgAdvanced.edServerAddrEmail,
                binding.cfgAdvanced.edAccessID,
		            binding.cfgAdvanced.edAccessIDpwd, 
                binding.cfgAdvanced.cfgEmail,
		            /*binding.cfgAdvanced.cfgProfileName!!,*/ 
                binding.cfgBasic.cfgEmail)
            .forEach {
                it.setOnFocusChangeListener { v, hasFocus ->
						                                      if(!hasFocus) { hideKeyboard(v) }
                }
                it.setTypeface(type)
            }
        arrayOf(binding.cfgBasic.cfgLabelEmail,
		            binding.cfgAdvanced.cfgLabelEmail,
		            binding.cfgAdvanced.cfgLabelSvrAddress,
                binding.cfgBasic.cfgCreateAccount,
                binding.cfgBasic.dontHaveAccountText,
                binding.cfgBasic.cfgCbAdvanced).forEach {
            it.setTypeface(type)
        }
        binding.cfgBasic.cfgCreateAccount.setTypeface(type, Typeface.BOLD)

        type = SuplaApp.getApp().typefaceQuicksandRegular
        arrayOf(binding.cfgBasic.cfgLabelTitleBasic).forEach {
            it.setTypeface(type)
        }


        if(viewModel.cfgData.authByEmail.value!!) {
            binding.cfgAdvanced.authType.setPosition(0, false)
        } else {
            binding.cfgAdvanced.authType.setPosition(1, false)
        }

        binding.cfgAdvanced.authType.setOnPositionChangedListener() { 
            pos -> viewModel.selectEmailAuth(pos == 0)
        }

        return binding.root
    }

    fun hideKeyboard(v: View) {
        val service = SuplaApp.getApp().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        service?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
    }
}
