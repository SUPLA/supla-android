package org.supla.android.features.notificationinfo

import android.Manifest
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.MainActivity
import org.supla.android.Preferences
import org.supla.android.databinding.DialogNotificationInfoBinding
import javax.inject.Inject

@AndroidEntryPoint
class NotificationInfoDialog : DialogFragment() {

  @Inject lateinit var preferences: Preferences

  private lateinit var binding: DialogNotificationInfoBinding

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return super.onCreateDialog(savedInstanceState).apply {
      setCanceledOnTouchOutside(false)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = DialogNotificationInfoBinding.inflate(inflater, container, false)
    return binding.root
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {
      notificationInfoOk.setOnClickListener {
        dismiss()
        (requireActivity() as? MainActivity)?.requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      notificationInfoSkip.setOnClickListener {
        dismiss()
        preferences.isNotificationsPopupDisplayed = true
      }
    }
  }

  companion object {
    fun create(): NotificationInfoDialog = NotificationInfoDialog()
  }
}
