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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import org.supla.android.R
import org.supla.android.databinding.FragmentAuthBinding

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val viewModel: AuthItemViewModel by viewModels()
    private val navCoordinator: NavCoordinator by activityViewModels()
    private val args: AuthFragmentArgs by navArgs()

    private lateinit var binding: FragmentAuthBinding

    /*
     Flag to keep track if we should perform a deferred
     action after keyboard is hidden.
     */
    private var pendingKeyboardHiddenAction = false
    private var origHeight = 0

    /* Flag to indicate if scroll position should be reset */
    private var shouldResetScrollViewOffset = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreated(args.profileId)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth,
                container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.navCoordinator = navCoordinator
        binding.cfgAdvanced.viewModel = viewModel
        binding.cfgBasic.viewModel = viewModel

        val sv = binding.scrollView
        val vto = sv.viewTreeObserver
        vto.addOnGlobalLayoutListener {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val height = binding.root.height
                if (origHeight > 0) {
                    if (height == origHeight) {
                        // keyboard was hidden
                        resetScrollView()
                    }
                } else {
                    origHeight = height
                }
                if (shouldResetScrollViewOffset) {
                    shouldResetScrollViewOffset = false
                    sv.smoothScrollTo(0, 0)
                }
            }, 100)
        }

        arrayOf(binding.cfgAdvanced.edServerAddr,
                binding.cfgAdvanced.edServerAddrEmail,
                binding.cfgAdvanced.edAccessID,
                binding.cfgAdvanced.edAccessIDpwd,
                binding.cfgAdvanced.cfgEmail,
                binding.cfgAdvanced.cfgProfileName,
                binding.cfgBasic.cfgProfileName,
                binding.cfgBasic.cfgEmail)
                .forEach { editText ->
                    editText.setOnFocusChangeListener { v, hasFocus ->
                        val createAccountVisibility: Int
                        if (hasFocus) {
                            createAccountVisibility = View.GONE
                            // reset pending hide keyboard action as another
                            // view had received the focus.
                            pendingKeyboardHiddenAction = false
                        } else {
                            createAccountVisibility = if (viewModel.hasValidAccount) View.GONE else View.VISIBLE
                            // schedule a pending keyboard action to be performed
                            if (!pendingKeyboardHiddenAction) {
                                pendingKeyboardHiddenAction = true
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({
                                    if (pendingKeyboardHiddenAction) {
                                        resetScrollView()
                                        hideKeyboard(v)
                                        pendingKeyboardHiddenAction = false
                                    }
                                }, 100)
                            }
                        }
                        arrayOf(binding.dontHaveAccountText,
                                binding.cfgCreateAccount).forEach {
                            it.visibility = createAccountVisibility
                        }
                    }
                }

        if (viewModel.authByEmail.value == true) {
            binding.cfgAdvanced.authType.position = 0
        } else {
            binding.cfgAdvanced.authType.position = 1
        }

        binding.cfgAdvanced.authType.setOnPositionChangedListener { pos ->
            viewModel.selectEmailAuth(pos == 0)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.editAction.collect { state -> handleActions(state) }
        }
    }

    private fun handleActions(action: AuthItemEditAction?) {
        when (action) {
            is AuthItemEditAction.Alert -> showEditDialog(action)
            is AuthItemEditAction.ConfirmDelete -> showConfirmAction(action.hasWidgets)
            is AuthItemEditAction.NavigateToCreateAccount -> navCoordinator.navigate(NavigationFlow.CREATE_ACCOUNT)
            is AuthItemEditAction.ReturnFromAuth -> navCoordinator.returnFromAuth(action.authSettingChanged)
            null -> {} // nothing to do
        }
    }

    private fun showEditDialog(action: AuthItemEditAction.Alert) {
        AlertDialog.Builder(requireContext())
                .setTitle(action.titleResId)
                .setMessage(action.messageResId)
                .setPositiveButton(android.R.string.ok) { dlg, _ ->
                    dlg.cancel()
                }.create().show()
    }

    private fun showConfirmAction(hasWidgets: Boolean) {
        val message = if (hasWidgets) {
            R.string.delete_account_confirm_message_with_widgets
        } else {
            R.string.delete_account_confirm_message
        }

        AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_account_confirm_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.onDeleteProfile(true)
                }
                .setNegativeButton(android.R.string.cancel) { dlg, _ ->
                    dlg.cancel()
                }.create().show()
    }

    private fun hideKeyboard(v: View) {
        val service = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        service?.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun resetScrollView() {
        shouldResetScrollViewOffset = true
    }
}
