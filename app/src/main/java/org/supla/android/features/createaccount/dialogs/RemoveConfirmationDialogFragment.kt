package org.supla.android.features.createaccount.dialogs
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
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import org.supla.android.databinding.DialogRemoveConfirmationDialogBinding
import org.supla.android.features.createaccount.CreateAccountViewModel

private const val ARG_PROFILE_ID = "arg_profile_id"

class RemoveConfirmationDialogFragment : DialogFragment() {

  private val viewModel: CreateAccountViewModel by viewModels(ownerProducer = { requireParentFragment() })
  private val profileId: Long by lazy { requireArguments().getLong(ARG_PROFILE_ID) }

  private lateinit var binding: DialogRemoveConfirmationDialogBinding
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = DialogRemoveConfirmationDialogBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {
      removeAccountCancelButton.setOnClickListener { dismiss() }
      removeAccountLocallyButton.setOnClickListener {
        dismiss()
        viewModel.deleteProfile(profileId)
      }
      removeAccountRemotelyButton.setOnClickListener {
        dismiss()
        viewModel.deleteProfileWithCloud(profileId)
      }
    }
  }

  companion object {
    fun create(profileId: Long): RemoveConfirmationDialogFragment =
      RemoveConfirmationDialogFragment().also {
        it.arguments = Bundle().apply { putLong(ARG_PROFILE_ID, profileId) }
      }
  }
}
