package org.supla.android.cfg

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.findViewTreeLifecycleOwner
import org.supla.android.R
import org.supla.android.databinding.FragmentCfgBinding
import org.supla.android.databinding.FragmentProfilesBinding

class ProfilesFragment: Fragment() {
    private val viewModel: CfgViewModel by activityViewModels()
    private val profilesVM: ProfilesViewModel by viewModels()
    private lateinit var binding: FragmentProfilesBinding

    override fun onStart() {
        super.onStart()
        profilesVM.state.observe(viewLifecycleOwner) {
            when(it) {
                ProfilesViewModel.State.NEW_PROFILE_INPUT -> showNewProfileDialog()
            }
        }

    }

    private fun showNewProfileDialog() {
        val inputField = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("New profile")
            .setView(inputField)
            .setPositiveButton("Create") {
                dlg, what ->
                profilesVM.profileName = inputField.text.toString()
            }
            .setNegativeButton(android.R.string.no) { _,_ -> }
            .create()
            .show()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profiles,
            container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        profilesVM.activeProfile.value = "Default"
        binding.profilesVM = profilesVM
        binding.profilesList.adapter = ProfilesAdapter(profilesVM)
        return binding.root
    }
}
