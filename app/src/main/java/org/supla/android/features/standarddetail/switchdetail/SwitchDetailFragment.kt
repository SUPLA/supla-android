package org.supla.android.features.standarddetail.switchdetail

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentSwitchDetailBinding
import org.supla.android.db.Channel
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.model.ItemType

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"

@AndroidEntryPoint
class SwitchDetailFragment : BaseFragment<SwitchDetailViewState, SwitchDetailViewEvent>(R.layout.fragment_switch_detail) {

  private val viewModel: SwitchDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentSwitchDetailBinding::bind)

  private val itemType: ItemType by lazy { arguments!!.getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { arguments!!.getInt(ARG_REMOTE_ID) }

  override fun getViewModel(): BaseViewModel<SwitchDetailViewState, SwitchDetailViewEvent> = viewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.switchDetailButton.setOnClickListener { viewModel.toggle(remoteId) }
    viewModel.loadData(remoteId, itemType)
  }

  override fun handleEvents(event: SwitchDetailViewEvent) {
  }

  override fun handleViewState(state: SwitchDetailViewState) {
    if (state.channelBase != null) {
      setToolbarTitle(state.channelBase.getNotEmptyCaption(context))
      binding.switchDetailIcon.setImageResource(state.channelBase.imageIdx.id)

      val channel = state.channelBase as? Channel ?: return
      if (channel.value.hiValue()) {
        binding.switchDetailButton.setImageResource(R.drawable.rgbwpoweroff)
      } else {
        binding.switchDetailButton.setImageResource(R.drawable.rgbwpoweron)
      }
    }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (message.channelId == remoteId) {
          viewModel.loadData(remoteId, itemType)
        }
      }
    }
  }

  companion object {
    fun bundle(remoteId: Int, itemType: ItemType) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_ITEM_TYPE to itemType
    )
  }
}