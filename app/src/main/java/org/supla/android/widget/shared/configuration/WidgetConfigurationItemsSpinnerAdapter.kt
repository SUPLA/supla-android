package org.supla.android.widget.shared.configuration
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import org.supla.android.R
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.db.ChannelBase
import org.supla.android.db.DbItem
import org.supla.android.db.Location
import org.supla.android.images.ImageCache

class WidgetConfigurationChannelsSpinnerAdapter(
  context: Context,
  objects: MutableList<SpinnerItem<DbItem>>
) : ArrayAdapter<SpinnerItem<DbItem>>(context, R.layout.li_widget_channel_spinner, objects) {

  override fun getItemId(position: Int): Long =
    if (position < count) getItem(position)?.value?.id ?: 0 else 0

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    return createDropDownItemView(position, convertView, R.layout.li_widget_spinner_display_item)
  }

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    return createItemView(position, convertView, R.layout.li_widget_channel_spinner)
  }

  fun postItems(channels: List<SpinnerItem<DbItem>>) {
    clear()
    addAll(channels)
    notifyDataSetChanged()
  }

  private fun createItemView(position: Int, convertView: View?, @LayoutRes layout: Int): View {
    val view = convertView ?: LayoutInflater.from(context).inflate(layout, null)
    val item = getItem(position) ?: return view
    val nightMode = context
      .resources
      .configuration
      .uiMode
      .and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    when {
      item.isLocation() -> {
        view.findViewById<LinearLayout>(R.id.spinner_item_content).isClickable = true
        view.findViewById<LinearLayout>(R.id.spinner_item_content)
          .setBackgroundResource(R.color.widget_spinner_location_background)
        view.findViewById<TextView>(R.id.spinner_item_text)?.text = (item.value as Location).caption
        view.findViewById<ImageView>(R.id.spinner_item_icon).visibility = View.GONE
      }
      item.isScene() -> {
        view.findViewById<LinearLayout>(R.id.spinner_item_content).isClickable = false
        val scene = (item.value as Scene)
        view.findViewById<LinearLayout>(R.id.spinner_item_content)
          .setBackgroundResource(R.color.widget_spinner_item_background)
        view.findViewById<TextView>(R.id.spinner_item_text)?.text = scene.caption

        val icon = view.findViewById<ImageView>(R.id.spinner_item_icon)
        icon.visibility = View.VISIBLE
        icon.setImageBitmap(ImageCache.getBitmap(context, scene.getImageId(nightMode)))
      }
      else -> {
        view.findViewById<LinearLayout>(R.id.spinner_item_content).isClickable = false
        val channel = (item.value as ChannelBase)
        view.findViewById<LinearLayout>(R.id.spinner_item_content)
          .setBackgroundResource(R.color.widget_spinner_item_background)
        view.findViewById<TextView>(R.id.spinner_item_text)?.text =
          channel.getNotEmptyCaption(context)

        val icon = view.findViewById<ImageView>(R.id.spinner_item_icon)
        icon.visibility = View.VISIBLE
        icon.setImageBitmap(
          ImageCache.getBitmap(
            context,
            channel.getImageIdx(nightMode, ChannelBase.WhichOne.First, 0)
          )
        )
      }
    }

    return view
  }

  private fun createDropDownItemView(
    position: Int,
    convertView: View?,
    @LayoutRes layout: Int
  ): View {
    val view = convertView ?: LayoutInflater.from(context).inflate(layout, null)
    val item = getItem(position) ?: return view

    when {
      item.isLocation() ->
        view.findViewById<TextView>(R.id.spinner_text)?.text = (item.value as Location).caption
      item.isScene() ->
        view.findViewById<TextView>(R.id.spinner_text)?.text = (item.value as Scene).caption
      else ->
        view.findViewById<TextView>(R.id.spinner_text)?.text =
          (item.value as ChannelBase).getNotEmptyCaption(context)
    }
    return view
  }
}

data class SpinnerItem<T : DbItem>(val value: T) {
  fun isLocation(): Boolean {
    return value is Location
  }

  fun isScene(): Boolean {
    return value is Scene
  }
}
