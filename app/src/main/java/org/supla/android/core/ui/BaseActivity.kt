package org.supla.android.core.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import org.supla.android.ui.LoadableContent

abstract class BaseActivity : AppCompatActivity(), LoadableContent {

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
