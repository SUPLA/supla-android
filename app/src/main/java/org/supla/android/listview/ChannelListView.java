package org.supla.android.listview;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */


import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.R;
import org.supla.android.Trace;
import org.supla.android.db.Channel;


public class ChannelListView extends ListView {

    private float LastXtouch = -1;
    private float LastYtouch = -1;
    private ChannelLayout channelLayout = null;
    private ChannelLayout lastCL = null;
    private boolean SlideStarted = false;
    private Cursor _newCursor;
    private OnChannelButtonTouchListener onChannelButtonTouchListener;
    private boolean requestLayout_Locked = false;
    private SectionLayout Header;

    public interface OnChannelButtonTouchListener {

        void onChannelButtonTouch(boolean left, boolean up, int channelId, int channelFunc);
    }

    public ChannelListView(Context context) {
        super(context);
        init(context);
    }

    public ChannelListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ChannelListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setHeaderDividersEnabled(false);
        setFooterDividersEnabled(false);
        setDivider(null);

        setFastScrollEnabled(true);

        Header = new SectionLayout(context);
        Header.setCaption("ABCD");
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void requestLayout() {

        if ( !requestLayout_Locked )
            super.requestLayout();

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        if (ev.getAction() == MotionEvent.ACTION_DOWN || channelLayout != null ) {

            View view = getChildAt(pointToPosition((int) ev.getX(), (int) ev.getY()) - getFirstVisiblePosition() );
            int action = ev.getAction();

            if (view instanceof ChannelLayout) {

                switch (action) {
                    case MotionEvent.ACTION_DOWN:

                        LastXtouch = ev.getX();
                        LastYtouch = ev.getY();
                        channelLayout = (ChannelLayout)view;
                        SlideStarted = false;

                        if ( lastCL != null && lastCL != channelLayout ) {
                            lastCL.AnimateToRestingPosition(true);
                            lastCL = null;
                        }

                        break;

                    case MotionEvent.ACTION_MOVE:

                        float X = ev.getX();
                        float Y = ev.getY();

                        float deltaY = Math.abs(Y - LastYtouch);
                        float deltaX = Math.abs(X - LastXtouch);

                        if ( channelLayout.Sliding() == false
                                && deltaY >= deltaX ) {
                            return super.onTouchEvent(ev);
                        }

                        if (  X != LastXtouch ) {
                            channelLayout.Slide((int)(X-LastXtouch));
                            SlideStarted = true;
                        }

                        LastXtouch = X;
                        LastYtouch = Y;

                        if ( channelLayout.Sliding() ) {
                            return true;
                        }

                }

            }

           if  ( action == MotionEvent.ACTION_UP )  {

               if ( channelLayout != null ) {
                   channelLayout.AnimateToRestingPosition(SlideStarted == false);
                   lastCL = channelLayout;
                   channelLayout = null;
               }

               LastXtouch = -1;
               SlideStarted = false;
           }


        }

        return super.onTouchEvent(ev);

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        canvas.save();

        ListViewCursorAdapter adapter = (ListViewCursorAdapter)getAdapter();
        if ( adapter != null ) {

            int idx = adapter.getSectionIndexAtPosition(pointToPosition(0, 0));

            ListViewCursorAdapter.SectionItem section = adapter.getSectionAtIndex(idx);
            if ( section != null
                    && section.getView() != null ) {

                idx++;
                ListViewCursorAdapter.SectionItem next = adapter.getSectionAtIndex(idx);

                if ( next != null
                        && next.getView() != null
                        && next.getView().getTop() > 0
                        && next.getView().getTop() <= section.getView().getHeight() ) {

                    canvas.translate(0, (next.getView().getHeight() - next.getView().getTop())*-1);
                }

                section.getView().draw(canvas);
            }
        }

        canvas.restore();

    }

    public void onSlideAnimationEnd() {
        
        if ( _newCursor != null && !Slided() ) {
            Cursor newCursor = _newCursor;
            _newCursor = null;
            Refresh(newCursor, true);
        }

    }

    public boolean Slided() {

        int start = getFirstVisiblePosition();

        for(int i=start; i<=getLastVisiblePosition();i++) {

            View v = getChildAt(i-start);


            if ( v != null
                    && v instanceof ChannelLayout
                    && ((ChannelLayout)v).Slided() > 0 ) {
                    return true;
            }

        }

        return false;
    }

    public void Refresh(Cursor newCursor, boolean full) {

        if ( _newCursor != null ) {
            _newCursor.close();
            _newCursor = null;
        }

        if ( getAdapter() == null )
            return;

        if ( full || !Slided() ) {
            newCursor.moveToFirst();
            ((ListViewCursorAdapter)getAdapter()).changeCursor(newCursor);
            return;
        };

        if ( newCursor == null || newCursor.isClosed() )
            return;

        _newCursor = newCursor;
        int start = getFirstVisiblePosition();

        for(int i=start;i<=getLastVisiblePosition();i++) {

            View v = getChildAt(i-start);
            Object obj = getItemAtPosition(i);

            if ( v instanceof ChannelLayout
                    && obj instanceof Cursor ) {

                Channel old_channel = new Channel();
                old_channel.AssignCursorData((Cursor) obj);


                if ( _newCursor.moveToFirst() )
                    do {
                        Channel new_channel = new Channel();
                        new_channel.AssignCursorData(_newCursor);

                        if ( new_channel.getId() == old_channel.getId() ) {
                            requestLayout_Locked = true;
                            ((ListViewCursorAdapter) getAdapter()).setData((ChannelLayout) v, new_channel);
                            requestLayout_Locked = false;
                        }

                    } while(_newCursor.moveToNext());
            }

        }


    }

    public void setOnChannelButtonTouchListener(OnChannelButtonTouchListener onChannelButtonTouchListener) {
        this.onChannelButtonTouchListener = onChannelButtonTouchListener;
    }

    public OnChannelButtonTouchListener getOnChannelButtonTouchListener() {
        return onChannelButtonTouchListener;
    }

}
