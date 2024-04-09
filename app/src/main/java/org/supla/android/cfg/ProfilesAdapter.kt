package org.supla.android.cfg

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.R
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.databinding.LiNewProfileBinding
import org.supla.android.databinding.LiProfileItemBinding
import org.supla.android.extensions.visibleIf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfilesAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var onActivateClickListener: (Long) -> Unit = {}
  var onEditClickListener: (Long) -> Unit = {}
  var onAddClickListener: () -> Unit = {}

  private var profiles: List<ProfileEntity> = emptyList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return if (viewType == R.layout.li_profile_item) {
      ItemViewHolder(LiProfileItemBinding.inflate(inflater, parent, false))
    } else {
      ButtonViewHolder(LiNewProfileBinding.inflate(inflater, parent, false))
    }
  }

  override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {
    if (vh is ItemViewHolder) {
      val itm = profiles[pos]
      vh.binding.profileIcon.setImageResource(if (itm.active == true) R.drawable.profile_selected else R.drawable.profile_unselected)
      vh.binding.profileLabel.text = itm.name
      vh.binding.activeIndicator.visibleIf(itm.active == true)
      vh.binding.root.setOnClickListener { onActivateClickListener(itm.id!!) }
      vh.binding.accountDetailsEdit.setOnClickListener { onEditClickListener(itm.id!!) }
    } else if (vh is ButtonViewHolder) {
      vh.binding.root.setOnClickListener { onAddClickListener() }
    }
  }

  override fun getItemCount(): Int {
    return profiles.size + 1
  }

  override fun getItemViewType(pos: Int): Int {
    return if (pos < profiles.size) {
      R.layout.li_profile_item
    } else {
      R.layout.li_new_profile
    }
  }

  override fun getItemId(position: Int): Long {
    return if (position < profiles.size) profiles[position].id!! else -1
  }

  @SuppressLint("NotifyDataSetChanged")
  fun setData(profiles: List<ProfileEntity>) {
    this.profiles = profiles
    notifyDataSetChanged()
  }

  inner class ItemViewHolder(val binding: LiProfileItemBinding) :
    RecyclerView.ViewHolder(binding.root)

  inner class ButtonViewHolder(val binding: LiNewProfileBinding) :
    RecyclerView.ViewHolder(binding.root)
}
