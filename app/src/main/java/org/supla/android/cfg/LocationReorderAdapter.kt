package org.supla.android.cfg

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

import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.TextView
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import org.supla.android.SuplaApp
import org.supla.android.db.Location
import org.supla.android.R
import org.supla.android.listview.draganddrop.ListViewDragListener


class LocationReorderAdapter(private val ctx: Context,
                             private var locations: Array<Location>): 
    BaseAdapter(), ListAdapter {

    val orderedLocations: Array<Location> = locations

    private var destPosition: Int? = null
    private var srcPosition: Int? = null

    override fun getItem(pos: Int): Location {
        return locations[pos]
    }

    override fun getCount(): Int {
        return locations.size
    }

    override fun getItemId(pos: Int): Long {
        return locations[pos].id
    }

    override fun getView(pos: Int,
                         convertView: View?,
                         container: ViewGroup): View? {
        val rv: View?
        var obj: Location? = null
        val dest = destPosition
        val src = srcPosition
        if(dest == null) {
            obj = getItem(pos)
        } else {
            if(dest < src!!) {
                if(pos < dest || pos > src) {
                    obj = getItem(pos)
                } else if(pos > dest) {
                    obj = getItem(pos - 1)
                }
            } else if(dest > src) {
                if(pos < src ||  pos > dest) {
                    obj = getItem(pos)
                } else if(pos >= src && pos < dest) {
                    obj = getItem(pos + 1)
                }
            } else {
                if(pos != dest) {
                    obj = getItem(pos)
                }
            }
        }


        if(convertView == null) {
            val inflater = LayoutInflater.from(ctx)
            rv = inflater.inflate(R.layout.listview_section,
                                  container, false)
        } else {
            rv = convertView
       }
        val caption: TextView? = rv?.findViewById(R.id.tvSectionCaption)
        caption?.text = obj?.getCaption() ?: ""
        caption?.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        val collapse: View? = rv?.findViewById(R.id.ivSectionCollapsed)
        collapse?.visibility = View.INVISIBLE

        val dragHolder: View? = rv?.findViewById(R.id.ivDragHolder)
        dragHolder?.visibility = View.VISIBLE

        return rv
    }

    fun enableDrag(pos: Int) {
        srcPosition = pos
    }

    fun endDrag(pos: Int): Boolean {
        var rv = false
        var src = srcPosition
        if(src != null && pos != ListViewDragListener.INVALID_POSITION && pos != src) {
            val dir = if(src < pos) 1 else -1
            val t = locations[src]
            do {
                locations[src] = locations[src + dir]
                src += dir
            } while(src != pos)
            locations[src] = t
            rv = true
        }

        srcPosition = null
        destPosition = null
        notifyDataSetChanged()
        return rv
     }

    fun updateDrag(source: Int, dest: Int) {
        if(dest == ListViewDragListener.INVALID_POSITION ||
           srcPosition == null) { return }
        if(dest != destPosition) {
            destPosition = dest
            notifyDataSetChanged()
        }
        
    }
}
