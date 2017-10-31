package org.supla.android.listview;

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


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.supla.android.ChannelDetailRGB;
import org.supla.android.ChannelDetailRS;
import org.supla.android.R;
import org.supla.android.db.Channel;
import org.supla.android.lib.SuplaConst;


public class ChannelListView extends ListView {

    private float LastXtouch = -1;
    private float LastYtouch = -1;
    private ChannelLayout channelLayout = null;
    private ChannelLayout lastCL = null;
    private boolean buttonSliding = false;
    private Cursor _newCursor;
    private OnChannelButtonTouchListener onChannelButtonTouchListener;
    private OnDetailListener onDetailListener;
    private boolean requestLayout_Locked = false;
    private boolean detailSliding;
    private boolean detailTouchDown;
    private SectionLayout Header;
    private boolean detailAnim;
    private boolean mDetailVisible;
    private DetailLayout mDetailLayout;

    public interface OnChannelButtonTouchListener {

        void onChannelButtonTouch(boolean left, boolean up, int channelId, int channelFunc);
    }

    public interface OnDetailListener {

        void onChannelDetailShow();
        void onChannelDetailHide();
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

        //setFastScrollEnabled(true);
        setVerticalScrollBarEnabled(false);

        Header = new SectionLayout(context);
        Header.setCaption("ABC");
        detailSliding = false;
        detailTouchDown = false;
        detailAnim = false;
        mDetailLayout = null;
        mDetailVisible = false;

    }


    private DetailLayout getDetailLayout(Channel channel) {

        if ( mDetailLayout != null ) {

            switch(channel.getFunc()) {
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:

                    if (!(mDetailLayout instanceof ChannelDetailRGB))
                        mDetailLayout = null;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:

                    if (!(mDetailLayout instanceof ChannelDetailRS))
                        mDetailLayout = null;

                    break;
            }

        }

        if ( mDetailLayout == null ) {


            switch(channel.getFunc()) {
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                    mDetailLayout = new ChannelDetailRGB(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                    mDetailLayout = new ChannelDetailRS(getContext(), this);
                    break;
            }

            if ( mDetailLayout != null ) {

                if ( getParent() instanceof ViewGroup ) {
                    ((ViewGroup)getParent()).addView(mDetailLayout);
                }

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getWidth(), getHeight());
                lp.setMargins(getWidth(),0,-getWidth(),0);
                mDetailLayout.setLayoutParams(lp);

            }


            return mDetailLayout;

        }

        return mDetailLayout;
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

        if (!requestLayout_Locked)
            super.requestLayout();

    }

    public int getMargin() {

        ViewGroup.LayoutParams lp = getLayoutParams();
        if ( lp instanceof  ViewGroup.MarginLayoutParams ) {
            return ((MarginLayoutParams)lp).leftMargin;
        }

        return 0;
    }

    public boolean setMargin(int margin) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        if ( lp instanceof  ViewGroup.MarginLayoutParams ) {
            ((ViewGroup.MarginLayoutParams)lp).setMargins(margin,0,-margin,0);
            setLayoutParams(lp);

            if ( mDetailLayout != null )
                mDetailLayout.setMargin(getWidth()+((MarginLayoutParams) lp).leftMargin);

            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        int action = ev.getAction();
        float X = ev.getX();
        float Y = ev.getY();

        float deltaY = Math.abs(Y - LastYtouch);
        float deltaX = Math.abs(X - LastXtouch);

        if ( ev.getAction() == MotionEvent.ACTION_DOWN
                || channelLayout != null ) {

            View view = getChildAt(pointToPosition((int) ev.getX(), (int) ev.getY()) - getFirstVisiblePosition());

            if (view instanceof ChannelLayout) {

                if ( action == MotionEvent.ACTION_DOWN ) {

                    LastXtouch = ev.getX();
                    LastYtouch = ev.getY();

                    if ( !isDetailVisible() ) {
                        channelLayout = (ChannelLayout)view;
                    }

                    buttonSliding = false;
                    detailSliding = false;
                    detailTouchDown = false;

                    if ( lastCL != null && lastCL != channelLayout ) {
                        lastCL.AnimateToRestingPosition(true);
                        lastCL = null;
                    }

                    if ( channelLayout != null
                         && channelLayout.getDetailSliderEnabled() ) {


                        Object obj = getItemAtPosition(pointToPosition((int) ev.getX(), (int) ev.getY()));

                        Channel channel = new Channel();
                        channel.AssignCursorData((Cursor)obj);


                        if ( obj instanceof Cursor && getDetailLayout(channel) != null ) {
                            detailTouchDown = true;
                            getDetailLayout(channel).setData(channel);
                        }
                    }

                } else if ( action == MotionEvent.ACTION_MOVE ) {

                    if ( channelLayout.getButtonsEnabled()
                            && !detailSliding ) {


                        if (!channelLayout.Sliding()
                                && deltaY >= deltaX ) {
                            return super.onTouchEvent(ev);
                        }

                        if (  X != LastXtouch ) {
                            channelLayout.Slide((int)(X-LastXtouch));
                            buttonSliding = true;
                        }

                        LastXtouch = X;
                        LastYtouch = Y;

                        if ( channelLayout.Sliding() ) {
                            return true;
                        }

                    }
                }


            }


        }

        if ( LastXtouch != -1
                && ( detailTouchDown || isDetailVisible() )
                && action == MotionEvent.ACTION_MOVE) {


                        int delta = (int)(X-LastXtouch);
                        int margin = getMargin();

                        if ( isDetailVisible() ) {
                            if ( margin + delta < -getWidth() )
                                delta = -(margin+getWidth());
                        } else {
                            if ( margin + delta > -1 )
                                delta-=(margin + delta) + 1;
                        }


                        if ( ( ( (!isDetailVisible() && X <= LastXtouch)
                                || (isDetailVisible() && X >= LastXtouch) )
                                && deltaY < deltaX ) || detailSliding ) {

                            setMargin(getMargin()+delta);

                            if (!detailSliding) {

                                int color = Color.WHITE;

                                if (mDetailLayout.getBackground() instanceof ColorDrawable) {
                                    color = ((ColorDrawable) mDetailLayout.getBackground().mutate()).getColor();
                                }

                                if ( channelLayout != null )
                                    channelLayout.setBackgroundColor(color);

                                setVisibility(View.VISIBLE);
                                mDetailLayout.setBackgroundColor(color);
                                mDetailLayout.setVisibility(View.VISIBLE);

                            }

                            detailSliding = true;


                            return true;

                        } else if ( detailSliding ){
                            return true;
                        }


        }



        if  ( action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL )  {

            AnimateDetailSliding(false);

            if ( channelLayout != null ) {
                channelLayout.AnimateToRestingPosition(!buttonSliding);
                lastCL = channelLayout;
                channelLayout = null;
            }

            LastXtouch = -1;
            buttonSliding = false;
            detailSliding = false;
        }


        return super.onTouchEvent(ev);

    }


    public void AnimateDetailSliding(boolean force) {

        final int margin = getMargin();

        if ( detailAnim
                || mDetailLayout == null )
            return;

        if ( ( margin == 0
                || ( isDetailVisible()
                     && margin == -getWidth() ) ) && !force )
            return;

        int offset = 0;
        int m = margin;

        if ( isDetailVisible() )
            m+=getWidth();

        if ( Math.abs(m) > getWidth()/3.5 || force ) {

            if ( isDetailVisible() ) {
                offset = -margin;
            } else {
                offset = -(margin+getWidth());
            }

        } else {

            if ( isDetailVisible() ) {
                offset = -(getWidth()+margin);
            } else {
                offset = -margin;
            }

        }


        ValueAnimator varl = ValueAnimator.ofInt(offset);
        varl.setDuration(200);

        varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                setMargin(margin+(Integer)animation.getAnimatedValue());
            }

        });

        varl.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                detailAnim = true;
                setVisibility(View.VISIBLE);
                mDetailLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                detailAnim = false;


                if ( getMargin() != 0 ) {
                    mDetailLayout.setVisibility(View.VISIBLE);
                    setVisibility(View.INVISIBLE);
                    mDetailVisible = true;

                    onDetailShow();

                } else {
                    mDetailLayout.setVisibility(View.INVISIBLE);
                    setVisibility(View.VISIBLE);
                    mDetailVisible = false;

                    onDetailHide();
                }

                setChannelBackgroundColor(getResources().getColor(R.color.channel_cell));
                detailAnim = false;
                detailSliding = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        varl.start();

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

    private void onDetailShow() {
        if ( onDetailListener != null )
            onDetailListener.onChannelDetailShow();
    }

    private void onDetailHide() {
        if ( onDetailListener != null )
            onDetailListener.onChannelDetailHide();
    }

    private void setChannelBackgroundColor(int color) {

        for(int i=0; i<=getCount();i++) {

            View v = getChildAt(i);

            if ( v != null
                    && v instanceof ChannelLayout ) {
                v.setBackgroundColor(color);
            }

        }

    }

    public boolean isDetailSliding() {
        return detailSliding || detailAnim;
    }

    public boolean Slided() {

        if ( detailSliding )
            return true;

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


        if ( full
                || ( !isDetailSliding()
                     && !Slided()
                     && getFirstVisiblePosition() == 0 ) ) {
            newCursor.moveToFirst();
            ((ListViewCursorAdapter)getAdapter()).changeCursor(newCursor);
            return;
        }

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

    public void setOnDetailListener(OnDetailListener onDetailListener) {
        this.onDetailListener = onDetailListener;
    }

    public OnChannelButtonTouchListener getOnChannelButtonTouchListener() {
        return onChannelButtonTouchListener;
    }

    public boolean isDetailVisible() {
        return mDetailVisible && mDetailLayout != null;
    }

    public void hideDetail(boolean animated) {


        if ( isDetailVisible() )

            if ( animated ) {
                AnimateDetailSliding(true);
            } else {

                setMargin(0);

                mDetailLayout.setVisibility(View.INVISIBLE);
                mDetailVisible = false;
                setVisibility(View.VISIBLE);

                onDetailHide();
            }


    }

    public Channel detail_getChannel() {

        if ( isDetailVisible() ) {
            return mDetailLayout.getChannelFromDatabase();
        }

        return null;
    }

    public int detail_getChannelId() {

        if ( isDetailVisible() ) {
            return mDetailLayout.getChannelId();
        }

        return 0;
    }

    public void detail_OnChannelDataChanged() {

        if ( isDetailVisible() )
            mDetailLayout.OnChannelDataChanged();

    }
}
