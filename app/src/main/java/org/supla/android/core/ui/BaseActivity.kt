package org.supla.android.core.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import org.supla.android.extensions.visibleIf

abstract class BaseActivity: AppCompatActivity() {

  protected abstract fun getLoadingIndicator(): View

  fun showLoading(isLoading: Boolean) {
    getLoadingIndicator().visibleIf(isLoading)
  }

  protected fun isBackHandledInChildFragment(): Boolean {
    for (fragment in supportFragmentManager.fragments) {
      if (fragment.isVisible && fragment is BackHandler && fragment.onBackPressed()) {
        // handled by fragment
        return true
      }
      if (fragment is NavHostFragment) {
        for (childFragment in fragment.childFragmentManager.fragments) {
          if (childFragment.isVisible && childFragment is BackHandler && childFragment.onBackPressed()) {
            // handled by fragment
            return true
          }
        }
      }
    }

    return false
  }
}