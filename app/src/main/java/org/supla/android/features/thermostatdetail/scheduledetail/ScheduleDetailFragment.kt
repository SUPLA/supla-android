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
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentScheduleDetailBinding
import org.supla.android.features.thermostatdetail.scheduledetail.ui.ScheduleDetail

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"

@AndroidEntryPoint
class ScheduleDetailFragment : BaseFragment<ScheduleDetailViewState, ScheduleDetailViewEvent>(R.layout.fragment_schedule_detail) {

  override val viewModel: ScheduleDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentScheduleDetailBinding::bind)

  @Suppress("DEPRECATION") // Not deprecated method can't be accessed from API 21
  private val itemType: ItemType by lazy { requireArguments().getSerializable(ARG_ITEM_TYPE) as ItemType }
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
    fun bundle(remoteId: Int, itemType: ItemType) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_ITEM_TYPE to itemType
    )
  }
}
