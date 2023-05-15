package org.supla.android.cfg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.LiNewProfileBinding
import org.supla.android.databinding.LiProfileItemBinding
import org.supla.android.db.AuthProfileItem

class ProfilesAdapter(private val profilesVM: ProfilesViewModel) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var profiles: List<AuthProfileItem> = emptyList()

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val type = SuplaApp.getApp().typefaceOpenSansRegular
    if (viewType == R.layout.li_profile_item) {
      val binding = LiProfileItemBinding.inflate(
        inflater,
        parent,
        false
      )
      binding.profileLabel.setTypeface(type)
      binding.activeIndicator.setTypeface(type)
      return ItemViewHolder(binding)
    } else {
      val binding = LiNewProfileBinding.inflate(
        inflater,
        parent,
        false
      )
      binding.addAccountLabel.setTypeface(type)
      return ButtonViewHolder(binding)
    }
  }

  override fun onBindViewHolder(
    vh: RecyclerView.ViewHolder,
    pos: Int
  ) {
    if (vh is ItemViewHolder) {
      val itm = profiles.get(pos)
      val evm = EditableProfileItemViewModel(itm)
      evm.editActionHandler = profilesVM
      vh.binding.viewModel = evm
      vh.binding.root.setOnClickListener {
        profilesVM.onActivateProfile(itm.id)
      }
    } else if (vh is ButtonViewHolder) {
      vh.binding.viewModel = profilesVM
      vh.binding.root.setOnClickListener {
        profilesVM.onNewProfile()
      }
    }
  }

  override fun getItemCount(): Int {
    return profiles.size + 1
  }

  override fun getItemViewType(pos: Int): Int {
    if (pos < profiles.size) {
      return R.layout.li_profile_item
    } else {
      return R.layout.li_new_profile
    }
  }

  override fun getItemId(position: Int): Long {
    return if (position < profiles.size) profiles.get(position).id else -1
  }

  fun reloadData(newProfiles: List<AuthProfileItem>) {
    this.profiles = newProfiles
    notifyDataSetChanged()
  }


  inner class ItemViewHolder(val binding: LiProfileItemBinding) :
    RecyclerView.ViewHolder(binding.root)

  inner class ButtonViewHolder(val binding: LiNewProfileBinding) :
    RecyclerView.ViewHolder(binding.root)
}
