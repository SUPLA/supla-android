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
import android.widget.ListView
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import org.supla.android.SuplaApp
import org.supla.android.db.Location
import org.supla.android.R
import org.supla.android.listview.draganddrop.ListViewDragListener


class LocationReorderAdapter(private val ctx: Context,
                             private var locations: Array<Location>,
                             private val listView: ListView,
                             private val viewModel: LocationReorderViewModel): 
    BaseAdapter(), ListAdapter {

    init {
        val dl = ListViewDragListener(listView,
                                      { pos -> viewDropped(pos) },
                                      { pos -> viewMoved(pos) })
        listView.setOnDragListener(dl)
    }

    val orderedLocations: Array<Location> = locations

    private var destPosition: Int? = null
    private var srcPosition: Int? = null

    private val isDragging: Boolean
        get() = srcPosition != null

    private class DragShadowBuilder(v: View): View.DragShadowBuilder(v) {
        
        override fun onDrawShadow(c: Canvas) {
            super.onDrawShadow(c)
            val p = Paint()
            p.setStyle(Paint.Style.STROKE)
            c.drawRect(c.getClipBounds(), p)
        }
    }

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

        val dragHolder: View?

        if(convertView == null) {
            val inflater = LayoutInflater.from(ctx)
            rv = inflater.inflate(R.layout.listview_section,
                                  container, false)
            val collapse: View? = rv?.findViewById(R.id.ivSectionCollapsed)
            collapse?.visibility = View.INVISIBLE

            dragHolder = rv?.findViewById(R.id.ivDragHolder)
            dragHolder?.visibility = View.VISIBLE
            dragHolder?.setOnTouchListener {
                v, event ->
                    val pos = v.tag as? Int
                    if(event.getAction() == MotionEvent.ACTION_DOWN && !isDragging && pos != null) {
                        val itmView = listView.getChildAt(pos - listView.getFirstVisiblePosition())
                        enableDrag(itmView, pos)
                         true
                    } else {
                         false
                    }
            }
        } else {
            rv = convertView
            dragHolder = rv?.findViewById(R.id.ivDragHolder)
       }
        val caption: TextView? = rv?.findViewById(R.id.tvSectionCaption)
        caption?.text = obj?.getCaption() ?: ""
        caption?.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());
        
        dragHolder?.tag = pos

        return rv
    }

    private fun enableDrag(view: View, pos: Int) {
        srcPosition = pos
        val shadowBuilder = DragShadowBuilder(view)
        view.startDrag(null, shadowBuilder, listView.getItemAtPosition(pos), 0)
    }

    private fun endDrag(pos: Int): Boolean {
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

    private fun updateDrag(source: Int, dest: Int) {
        val lastVisiblePosition = listView.getLastVisiblePosition()
        val firstVisiblePosition = listView.getFirstVisiblePosition()
        if(dest == ListViewDragListener.INVALID_POSITION ||
           srcPosition == null) { return }
        if(dest != destPosition) {
            destPosition = dest
            notifyDataSetChanged()
        } else if(dest == lastVisiblePosition && lastVisiblePosition < locations.size - 1) {
           listView.smoothScrollToPosition(dest + 1)
        } else if(dest == firstVisiblePosition && firstVisiblePosition > 0) {
            listView.smoothScrollToPosition(dest - 1)
        }
    }
   
    private fun viewDropped(pos: Int) {
        if(endDrag(pos)) {
            viewModel.onLocationsUpdate(orderedLocations)
        }
    }

    private fun viewMoved(pos: Int) {
        val start = srcPosition
        if(start != null) {
            updateDrag(start, pos)
        }
    }


}
