package org.supla.android.profile

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.Bindable
import androidx.databinding.BaseObservable
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.MainActivity
import org.supla.android.db.AuthProfileItem
import org.supla.android.cfg.ProfileItemViewModel
import org.supla.android.databinding.ProfileChooserListItemBinding
import org.supla.android.databinding.ProfileChooserBinding


class ProfileChooser(private val context: Context,
                     private val profileManager: ProfileManager) {

    private val profiles: Array<AuthProfileItem>
    private var dialog: AlertDialog? = null


    interface Listener {
        fun onProfileChanged()
    }

    var listener: Listener? = null

    init {
        profiles = profileManager.getAllProfiles().blockingFirst().toTypedArray()
    }

    fun show() {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewBinding = ProfileChooserBinding.inflate(inflater, null, false)
        val chooserView = viewBinding.root

        viewBinding.profileChooser.adapter = Adapter(profiles, this)
        
        val builder = AlertDialog.Builder(context)

        with(builder) {

            setTitle(R.string.profile_select_active)
            setCancelable(true)
            setView(chooserView)
            dialog = show()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    fun selectProfile(idx: Int) {
        val activated = try {
            profileManager.activateProfile(profiles[idx].id, false).blockingAwait()
            true
        } catch (throwable: Throwable) {
            false
        }

        if(activated) {
            listener?.onProfileChanged()
        }
        dialog?.dismiss()
    }

    class Adapter(private val profiles: Array<AuthProfileItem>,
                  private val host: ProfileChooser): 
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class ItemViewHolder(val binding: ProfileChooserListItemBinding) 
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): RecyclerView.ViewHolder {
            val type = SuplaApp.getApp().typefaceOpenSansRegular
            val inflater = LayoutInflater.from(parent.context)
            val binding = ProfileChooserListItemBinding.inflate(inflater, parent,
                                                                false)
            binding.profileLabel.setTypeface(type)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(vh: RecyclerView.ViewHolder,
                                      pos: Int) {
            val itm = profiles.get(pos)
            if(vh is ItemViewHolder) {
                vh.binding.viewModel = ItemViewModel(itm.name, itm.isActive)
                vh.binding.root.setOnClickListener {
                    host.selectProfile(pos)
                }
            }
        }

        override fun getItemCount(): Int {
            return profiles.size
        }

        override fun getItemId(pos: Int): Long {
            return pos.toLong()
        }
    }


    class ItemViewModel(name: String, isActive: Boolean): 
        ProfileItemViewModel(name, isActive) {
    }
}
