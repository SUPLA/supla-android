package org.supla.android.features.thermostatdetail.scheduledetail

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.thermostatdetail.scheduledetail.ui.ScheduleDetail

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

@AndroidEntryPoint
class ScheduleDetailFragment : BaseFragment<ScheduleDetailViewState, ScheduleDetailViewEvent>(R.layout.fragment_compose) {

  override val viewModel: ScheduleDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      SuplaTheme {
        ScheduleDetail(viewModel)
      }
    }

    viewModel.observeConfig(remoteId = remoteId)
  }

  override fun handleEvents(event: ScheduleDetailViewEvent) {
  }

  override fun handleViewState(state: ScheduleDetailViewState) {
  }

  companion object {
    fun bundle(remoteId: Int) = bundleOf(ARG_REMOTE_ID to remoteId)
  }
}
