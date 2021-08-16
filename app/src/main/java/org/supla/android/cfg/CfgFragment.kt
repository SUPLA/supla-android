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
import org.supla.android.databinding.FragmentCfgBinding

class CfgFragment: Fragment() {
        private val viewModel: CfgViewModel by activityViewModels()
    private lateinit var binding: FragmentCfgBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cfg,
					  container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.cfgAdvanced.viewModel = viewModel
        binding.cfgBasic.viewModel = viewModel
	var type = SuplaApp.getApp().typefaceOpenSansRegular
	val edbg = resources.getDrawable(R.drawable.rounded_edittext)
        arrayOf(binding.cfgAdvanced.edServerAddr, binding.cfgAdvanced.edAccessID,
		binding.cfgAdvanced.edAccessIDpwd, binding.cfgAdvanced.cfgEmail!!,
		binding.cfgAdvanced.cfgProfileName!!, binding.cfgBasic.cfgEmail)
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

        return binding.root
    }

    fun hideKeyboard(v: View) {
        val service = SuplaApp.getApp().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        service?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
    }
}
