package org.supla.android.features.standarddetail.scheduledetail

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentScheduleDetailBinding
import org.supla.android.model.ItemType

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"

@AndroidEntryPoint
class ScheduleDetailFragment : BaseFragment<ScheduleDetailViewState, ScheduleDetailViewEvent>(R.layout.fragment_schedule_detail) {

  private val viewModel: ScheduleDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentScheduleDetailBinding::bind)

  private val itemType: ItemType by lazy { arguments!!.getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { arguments!!.getInt(ARG_REMOTE_ID) }

  override fun getViewModel(): BaseViewModel<ScheduleDetailViewState, ScheduleDetailViewEvent> = viewModel

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
