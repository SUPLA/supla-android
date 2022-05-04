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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.databinding.FragmentProfilesBinding
import org.supla.android.profile.ProfileIdNew
import org.supla.android.profile.ProfileManager
import javax.inject.Inject

@AndroidEntryPoint
class ProfilesFragment: Fragment() {
    @Inject internal lateinit var profileManager: ProfileManager
    private val navCoordinator: NavCoordinator by activityViewModels()
    private val viewModel: CfgViewModel by activityViewModels()
    private val profilesVM: ProfilesViewModel by viewModels()
    private lateinit var binding: FragmentProfilesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreate(sis: Bundle?) {
        super.onCreate(sis)
        profilesVM.uiState.observe(requireActivity()) 
        { 
          uiState ->
              when(uiState) {
                  is ProfilesUiState.EditProfile -> 
                      openEditProfileView(uiState.profileId)
                  is ProfilesUiState.ListProfiles ->
                      profilesVM.profilesAdapter.reloadData(uiState.profiles)
                  is ProfilesUiState.ProfileActivation ->
                      requireActivity().finish()
              }
        }

    }

    private fun openEditProfileView(profileId: Long) {
        val navId = if(profileId == ProfileIdNew) R.id.newProfile else R.id.editProfile
        val args = AuthFragmentArgs(profileId, true, false)
        navCoordinator.wantsBack = true
        findNavController().navigate(navId, args.toBundle())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profiles,
            container, false)
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel
        binding.profilesVM = profilesVM

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        profilesVM.reload()
    }
}
