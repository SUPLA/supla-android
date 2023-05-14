package org.supla.android.ui.dialogs

import android.content.Context
import org.supla.android.CaptionEditor
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.db.DbHelper

class ChannelGroupCaptionEditor(context: Context?) : CaptionEditor(context) {

  var captionChangedListener: () -> Unit = { }

  override fun getTitle(): Int {
    return R.string.channel_name
  }

  override fun getCaption(): String {
    val dbH = DbHelper.getInstance(context)
    val channel = dbH.getChannelGroup(id)
    return if (channel != null && channel.caption != null) {
      channel.caption
    } else ""
  }

  override fun applyChanged(newCaption: String) {
    val dbH = DbHelper.getInstance(context)
    val channelGroup = dbH.getChannelGroup(id)
    if (channelGroup != null) {
      channelGroup.caption = newCaption
      dbH.updateChannelGroup(channelGroup)
      val client = SuplaApp.getApp().getSuplaClient()
      client?.setChannelGroupCaption(id, newCaption)
    }
    captionChangedListener()
  }

  override fun getHint(): Int {
    return R.string.str_default
  }
}
