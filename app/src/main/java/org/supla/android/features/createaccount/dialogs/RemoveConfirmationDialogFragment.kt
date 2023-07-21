package org.supla.android.features.createaccount.dialogs

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
        it.arguments = bundleOf(
          ARG_PROFILE_ID to profileId
        )
      }
  }
}
