package org.supla.android.cfg

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.ProfileListItemBinding
import org.supla.android.databinding.ProfileListNewBinding

class ProfilesAdapter(private val profilesVM: ProfilesViewModel) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var profileCount: Int = 3

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
        //val itm = profiles.get(pos)
        if(vh is ItemViewHolder) {
            vh.binding.viewModel = ProfileItemViewModel("ala")
        }
    }

    override fun getItemCount(): Int {
        return profileCount + 1
    }

    override fun getItemViewType(pos: Int): Int {
        if(pos < profileCount) {
            return R.layout.profile_list_item
        } else {
            return R.layout.profile_list_new
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    inner class ItemViewHolder(val binding: ProfileListItemBinding) : 
        RecyclerView.ViewHolder(binding.root)

    inner class ButtonViewHolder(val binding: ProfileListNewBinding) :
        RecyclerView.ViewHolder(binding.root)
}
