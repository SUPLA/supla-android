package org.supla.android

import android.content.Context
import org.supla.android.db.DbHelper

class LocationCaptionEditor(context: Context?) : CaptionEditor(context) {

  var captionChangedListener: () -> Unit = { }

  override fun getTitle(): Int {
    return R.string.location_name
  }

  override fun getCaption(): String {
    val dbH = DbHelper.getInstance(context)
    val location = dbH.getLocation(id)
    return if (location != null && location.caption != null) {
      location.caption
    } else ""
  }

  override fun applyChanged(newCaption: String) {
    val dbH = DbHelper.getInstance(context)
    val location = dbH.getLocation(id)
    if (location != null) {
      location.caption = newCaption
      dbH.updateLocation(location)
    }
    val client = SuplaApp.getApp().getSuplaClient()
    client?.setLocationCaption(id, newCaption)
    captionChangedListener()
  }
}