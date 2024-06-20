package org.supla.android.listview.draganddrop;

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

import android.view.DragEvent;
import android.view.View;
import android.widget.ListView;
import androidx.annotation.NonNull;

/** Drag listener to use inside a list view. */
public class ListViewDragListener implements View.OnDragListener {

  public static final int INVALID_POSITION = -1;

  private final ListView listView;
  private final OnDropListener onDropListener;
  private final OnPositionChangeListener onPositionChangeListener;

  public ListViewDragListener(
      @NonNull ListView listView,
      @NonNull OnDropListener onDropListener,
      @NonNull OnPositionChangeListener onPositionChangeListener) {
    this.listView = listView;
    this.onDropListener = onDropListener;
    this.onPositionChangeListener = onPositionChangeListener;
  }

  @Override
  public boolean onDrag(View v, DragEvent event) {
    final int action = event.getAction();
    switch (action) {
      case DragEvent.ACTION_DRAG_STARTED:
        return true;
      case DragEvent.ACTION_DRAG_LOCATION:
        onPositionChangeListener.onPositionChanged(
            listView.pointToPosition((int) event.getX(), (int) event.getY()));
        return false;
      case DragEvent.ACTION_DROP:
        onDropListener.onViewDropped(
            listView.pointToPosition((int) event.getX(), (int) event.getY()));
        return true;
      case DragEvent.ACTION_DRAG_ENDED:
        onDropListener.onViewDropped(INVALID_POSITION);
        return true;
      default:
        return false;
    }
  }

  /** View dropped listener. Provides position where view was dropped inside the list view. */
  @FunctionalInterface
  public interface OnDropListener {
    void onViewDropped(int droppedPosition);
  }

  @FunctionalInterface
  public interface OnPositionChangeListener {
    void onPositionChanged(int position);
  }
}
