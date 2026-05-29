package org.supla.android.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.schedulers.Schedulers
import org.supla.android.R
import org.supla.android.cfg.ProfileItemViewModel
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.databinding.LiProfileChooserBinding
import org.supla.android.databinding.ProfileChooserBinding
import org.supla.android.usecases.profile.ActivateProfileUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import timber.log.Timber

class ProfileChooser(
  private val context: Context,
  private val activateProfileUseCase: ActivateProfileUseCase,
  readAllProfilesUseCase: ReadAllProfilesUseCase
) {

  private val profiles: List<ProfileEntity> = readAllProfilesUseCase().blockingFirst()
  private var dialog: AlertDialog? = null

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

  fun selectProfile(idx: Int) {
    try {
      activateProfileUseCase(profiles[idx].id, false)
        .subscribeOn(Schedulers.io())
        .blockingAwait()
    } catch (throwable: Throwable) {
      Timber.e(throwable, "Profile activation failed!")
    }

    dialog?.dismiss()
  }

  class Adapter(
    private val profiles: List<ProfileEntity>,
    private val host: ProfileChooser
  ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ItemViewHolder(val binding: LiProfileChooserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int
    ): RecyclerView.ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = LiProfileChooserBinding.inflate(
        inflater,
        parent,
        false
      )
      return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(
      vh: RecyclerView.ViewHolder,
      pos: Int
    ) {
      val itm = profiles.getOrNull(pos)
      if (vh is ItemViewHolder) {
        vh.binding.viewModel = ItemViewModel((itm?.name ?: ""), itm?.active == true)
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

  class ItemViewModel(name: String, isActive: Boolean) : ProfileItemViewModel(name, isActive)
}
