package org.supla.android.core.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.supla.android.extensions.visibleIf

abstract class BaseActivity: AppCompatActivity() {

  protected abstract fun getLoadingIndicator(): View

  fun showLoading(isLoading: Boolean) {
    getLoadingIndicator().visibleIf(isLoading)
  }
}