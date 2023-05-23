package org.supla.android.core.storage

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val SHARED_PREFERENCES_FILE = "InternalPreferences"
private const val DETAIL_OPENED_PAGE = "$SHARED_PREFERENCES_FILE.DETAIL_OPENED_PAGE_"

@Singleton
class InternalPreferences @Inject constructor(@ApplicationContext context: Context) {

  private val preferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

  fun getDetailOpenedPage(channelId: Int) =
    preferences.getInt("$DETAIL_OPENED_PAGE$channelId", 0)
  fun setDetailOpenedPage(channelId: Int, openedPage: Int) =
    preferences.edit().putInt("$DETAIL_OPENED_PAGE$channelId", openedPage).apply()
}