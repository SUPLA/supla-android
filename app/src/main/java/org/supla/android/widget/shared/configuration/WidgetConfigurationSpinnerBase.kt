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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import org.supla.android.R

abstract class WidgetConfigurationSpinnerBase<T>(
        context: Context,
        objects: MutableList<T>
) : ArrayAdapter<T>(context, R.layout.spinner_item, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, R.layout.spinner_display_item)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, R.layout.spinner_item)
    }

    abstract fun getItemText(item: T): String

    fun postItems(channels: List<T>) {
        clear()
        addAll(channels)
        notifyDataSetChanged()
    }

    private fun createItemView(position: Int, convertView: View?, @LayoutRes layout: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(layout, null)
        val item = getItem(position) ?: return view

        view.findViewById<TextView>(R.id.spinner_text)?.text = getItemText(item)
        return view
    }
}
