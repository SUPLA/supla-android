package org.supla.android.cfg

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.db.AuthProfileItem
import org.supla.android.databinding.ProfileListItemBinding
import org.supla.android.databinding.ProfileListNewBinding

class ProfilesAdapter(private val profilesVM: ProfilesViewModel) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var profiles: List<AuthProfileItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val type = SuplaApp.getApp().typefaceOpenSansRegular
        if(viewType == R.layout.profile_list_item) {
            val binding = ProfileListItemBinding.inflate(inflater,
                                                         parent,
                                                         false)
            binding.profileLabel.setTypeface(type)
            binding.activeIndicator.setTypeface(type)
            return ItemViewHolder(binding)
        } else {
            val binding = ProfileListNewBinding.inflate(inflater,
                                                        parent,
                                                        false)
            binding.addAccountLabel.setTypeface(type)
            return ButtonViewHolder(binding)
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder,
                                  pos: Int) {
        if(vh is ItemViewHolder) {
            val itm = profiles.get(pos)
            vh.binding.viewModel = ProfileItemViewModel(itm.name, itm.isActive)
            vh.binding.root.setOnClickListener {
                profilesVM.onEditProfile(itm.id)
            }
        } else if(vh is ButtonViewHolder) {
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
        if(pos < profiles.size) {
            return R.layout.profile_list_item
        } else {
            return R.layout.profile_list_new
        }
    }

    override fun getItemId(position: Int): Long {
        return if(position < profiles.size) profiles.get(position).id else -1
    }

    fun reloadData(newProfiles: List<AuthProfileItem>) {
        this.profiles = newProfiles
        notifyDataSetChanged()
    }


    inner class ItemViewHolder(val binding: ProfileListItemBinding) : 
        RecyclerView.ViewHolder(binding.root)

    inner class ButtonViewHolder(val binding: ProfileListNewBinding) :
        RecyclerView.ViewHolder(binding.root)
}
