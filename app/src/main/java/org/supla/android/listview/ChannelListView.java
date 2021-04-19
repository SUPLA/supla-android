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
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.supla.android.ChannelDetailDigiglass;
import org.supla.android.ChannelDetailEM;
import org.supla.android.ChannelDetailIC;
import org.supla.android.ChannelDetailRGBW;
import org.supla.android.ChannelDetailRS;
import org.supla.android.ChannelDetailTempHumidity;
import org.supla.android.ChannelDetailTemperature;
import org.supla.android.ChannelDetailThermostat;
import org.supla.android.ChannelDetailThermostatHP;
import org.supla.android.R;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaConst;


public class ChannelListView extends ListView {

    private float LastXtouch = -1;
    private float LastYtouch = -1;
    private ChannelLayout channelLayout = null;
    private ChannelLayout lastCL = null;
    private boolean buttonSliding = false;
    private Cursor _newCursor;
    private OnChannelButtonClickListener onChannelButtonClickListener;
    private OnChannelButtonTouchListener onChannelButtonTouchListener;
    private OnDetailListener onDetailListener;
    private OnCaptionLongClickListener onCaptionLongClickListener;
    private OnSectionLayoutTouchListener onSectionLayoutTouchListener;
    private boolean requestLayout_Locked = false;
    private boolean detailSliding;
    private boolean detailTouchDown;
    private boolean detailAnim;
    private boolean mDetailVisible;
    private DetailLayout mDetailLayout;
    private Point mChannelStateIconTouchPoint;
    private Point mChannelWarningIconTouchPoint;

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
        detailSliding = false;
        detailTouchDown = false;
        detailAnim = false;
        mDetailLayout = null;
        mDetailVisible = false;
    }

    private DetailLayout getDetailLayout(ChannelBase cbase) {

        
        if (mDetailLayout != null) {
            switch (cbase.getFunc()) {
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:

                    if (!(mDetailLayout instanceof ChannelDetailRGBW))
                        mDetailLayout = null;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:

                    if (!(mDetailLayout instanceof ChannelDetailRS))
                        mDetailLayout = null;

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:

                    // TODO: Remove channel type checking in future versions. Check function instead of type. # 140-issue
                    if (cbase.getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {
                        if (!(mDetailLayout instanceof ChannelDetailIC))
                            mDetailLayout = null;
                    } else {
                        if (!(mDetailLayout instanceof ChannelDetailEM))
                            mDetailLayout = null;
                    }
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                    if (!(mDetailLayout instanceof ChannelDetailTemperature)
                            || mDetailLayout instanceof ChannelDetailTempHumidity)
                        mDetailLayout = null;

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                    if (!(mDetailLayout instanceof ChannelDetailTempHumidity))
                        mDetailLayout = null;

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:

                    if (!(mDetailLayout instanceof ChannelDetailThermostat))
                        mDetailLayout = null;

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:

                    if (!(mDetailLayout instanceof ChannelDetailThermostatHP))
                        mDetailLayout = null;

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
                case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
                    if (!(mDetailLayout instanceof ChannelDetailDigiglass))
                        mDetailLayout = null;
                    break;
            }

        }

        if (mDetailLayout == null) {


            switch (cbase.getFunc()) {
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                    mDetailLayout = new ChannelDetailRGBW(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
                    mDetailLayout = new ChannelDetailRS(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:

                    // TODO: Remove channel type checking in future versions. Check function instead of type. # 140-issue
                    if (cbase.getType() == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {
                        mDetailLayout = new ChannelDetailIC(getContext(), this);
                    } else {
                        mDetailLayout = new ChannelDetailEM(getContext(), this);
                    }
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                    mDetailLayout = new ChannelDetailTemperature(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                    mDetailLayout = new ChannelDetailTempHumidity(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
                    mDetailLayout = new ChannelDetailThermostat(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
                    mDetailLayout = new ChannelDetailThermostatHP(getContext(), this);
                    break;
                case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
                case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
                    mDetailLayout = new ChannelDetailDigiglass(getContext(), this);
                    break;
            }

            if (mDetailLayout != null) {

                if (getParent() instanceof ViewGroup) {
                    ((ViewGroup) getParent()).addView(mDetailLayout);
                }

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getWidth(), getHeight());
                lp.setMargins(getWidth(), 0, -getWidth(), 0);
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
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return ((MarginLayoutParams) lp).leftMargin;
        }

        return 0;
    }

    public boolean setMargin(int margin) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) lp).setMargins(margin, 0, -margin, 0);
            setLayoutParams(lp);

            if (mDetailLayout != null)
                mDetailLayout.setMargin(getWidth() + ((MarginLayoutParams) lp).leftMargin);

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

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mChannelStateIconTouchPoint = null;
            mChannelWarningIconTouchPoint = null;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN
                || channelLayout != null) {

            View view = null;

            if (isDetailVisible()) {
                LastXtouch = ev.getX();
                LastYtouch = ev.getY();
            } else {
                view = getChildAt(pointToPosition((int) ev.getX(), (int) ev.getY()) - getFirstVisiblePosition());
            }

            if (view instanceof ChannelLayout) {

                if (action == MotionEvent.ACTION_DOWN) {

                    LastXtouch = ev.getX();
                    LastYtouch = ev.getY();

                    if (!isDetailVisible()) {
                        channelLayout = (ChannelLayout) view;
                    }

                    buttonSliding = false;
                    detailSliding = false;
                    detailTouchDown = false;

                    if (lastCL != null && lastCL != channelLayout) {
                        lastCL.AnimateToRestingPosition(true);
                        lastCL = null;
                    }

                    if (channelLayout != null) {

                        if (channelLayout.getDetailSliderEnabled()) {
                            Object obj = getItemAtPosition(pointToPosition((int) ev.getX(), (int) ev.getY()));

                            if (obj instanceof Cursor) {
                                ChannelBase cbase;

                                if (((ListViewCursorAdapter) getAdapter()).isGroup()) {
                                    cbase = new ChannelGroup();
                                } else {
                                    cbase = new Channel();
                                }

                                cbase.AssignCursorData((Cursor) obj);

                                if (getDetailLayout(cbase) != null) {
                                    detailTouchDown = true;
                                    getDetailLayout(cbase).setData(cbase);
                                }
                            }
                        }

                        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                            mChannelStateIconTouchPoint =
                                    channelLayout.stateIconTouched((int) ev.getX(), (int) ev.getY());
                            mChannelWarningIconTouchPoint =
                                    channelLayout.warningIconTouched((int) ev.getX(), (int) ev.getY());
                        }

                    }


                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (channelLayout.getButtonsEnabled()
                            && !detailSliding) {

                        if (!channelLayout.Sliding()
                                && deltaY >= deltaX) {
                            return super.onTouchEvent(ev);
                        }

                        if (X != LastXtouch) {
                            channelLayout.Slide((int) (X - LastXtouch));
                            buttonSliding = true;
                            if (channelLayout.percentOfSliding() > 3f) {
                                mChannelStateIconTouchPoint = null;
                                mChannelWarningIconTouchPoint = null;
                            }
                        }

                        LastXtouch = X;
                        LastYtouch = Y;

                        if (channelLayout.Sliding()) {
                            return true;
                        }

                    }
                }


            }


        }

        if (LastXtouch != -1
                && (detailTouchDown || isDetailVisible())
                && action == MotionEvent.ACTION_MOVE) {

            int delta = (int) (X - LastXtouch);
            int margin = getMargin();

            if (isDetailVisible()) {
                if (margin + delta < -getWidth())
                    delta = -(margin + getWidth());
            } else {
                if (margin + delta > -1)
                    delta -= (margin + delta) + 1;
            }


            if ((((!isDetailVisible() && X <= LastXtouch)
                    || (isDetailVisible() && X >= LastXtouch))
                    && deltaY < deltaX) || detailSliding) {

                setMargin(getMargin() + delta);

                if (!detailSliding) {

                    int color = Color.WHITE;

                    if (mDetailLayout.getBackground() instanceof ColorDrawable) {
                        color = ((ColorDrawable) mDetailLayout.getBackground().mutate()).getColor();
                    }

                    if (channelLayout != null)
                        channelLayout.setBackgroundColor(color);

                    setVisibility(View.VISIBLE);
                    mDetailLayout.setBackgroundColor(color);
                    mDetailLayout.setVisibility(View.VISIBLE);

                }

                detailSliding = true;


                return true;

            }
        }

        if (onChannelButtonClickListener != null
                && action == MotionEvent.ACTION_UP
                && channelLayout != null
                && channelLayout.getRemoteId() > 0) {

            if (mChannelStateIconTouchPoint != null
                    && Math.abs(mChannelStateIconTouchPoint.y-Y) < 30f
                    && Math.abs(mChannelStateIconTouchPoint.x-X) < 30f ) {
                onChannelButtonClickListener.onChannelStateButtonClick(this,
                        channelLayout.getRemoteId());
            } else if (mChannelWarningIconTouchPoint != null
                    && Math.abs(mChannelWarningIconTouchPoint.y-Y) < 30f
                    && Math.abs(mChannelWarningIconTouchPoint.x-X) < 30f ) {
                onChannelButtonClickListener.onChannelWarningButtonClick(this,
                        channelLayout.getRemoteId());
            }
        }

        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {

            AnimateDetailSliding(false);

            if (channelLayout != null) {
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

    public void hideButton(boolean immediately) {
        if (lastCL != null) {
            if (immediately) {
                lastCL.hideButtonImmediately();
            } else {
                lastCL.AnimateToRestingPosition(true);
            }

            lastCL = null;
        }
    }

    public void AnimateDetailSliding(boolean force) {

        final int margin = getMargin();

        if (detailAnim
                || mDetailLayout == null)
            return;

        if ((margin == 0
                || (isDetailVisible()
                && margin == -getWidth())) && !force)
            return;

        int offset;
        int m = margin;

        if (isDetailVisible())
            m += getWidth();

        if (Math.abs(m) > getWidth() / 3.5 || force) {

            if (isDetailVisible()) {
                offset = -margin;
            } else {
                offset = -(margin + getWidth());
            }

        } else {

            if (isDetailVisible()) {
                offset = -(getWidth() + margin);
            } else {
                offset = -margin;
            }

        }


        ValueAnimator varl = ValueAnimator.ofInt(offset);
        varl.setDuration(200);

        varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                setMargin(margin + (Integer) animation.getAnimatedValue());
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


                if (getMargin() != 0) {
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

        ListViewCursorAdapter adapter = (ListViewCursorAdapter) getAdapter();
        if (adapter != null) {

            int idx = adapter.getSectionIndexAtPosition(pointToPosition(0, 0));

            ListViewCursorAdapter.SectionItem section = adapter.getSectionAtIndex(idx);
            if (section != null
                    && section.getView() != null) {

                idx++;
                ListViewCursorAdapter.SectionItem next = adapter.getSectionAtIndex(idx);

                if (next != null
                        && next.getView() != null
                        && next.getView().getTop() > 0
                        && next.getView().getTop() <= section.getView().getHeight()) {

                    canvas.translate(0, (next.getView().getHeight() - next.getView().getTop()) * -1);
                }

                section.getView().draw(canvas);
            }
        }

        canvas.restore();

    }

    public void onSlideAnimationEnd() {

        if (_newCursor != null && !Slided()) {
            Cursor newCursor = _newCursor;
            _newCursor = null;
            Refresh(newCursor, true);
        }

    }

    private void onDetailShow() {
        mDetailLayout.onDetailShow();

        if (onDetailListener != null)
            onDetailListener.onChannelDetailShow(mDetailLayout.getChannelBase());
    }

    private void onDetailHide() {
        mDetailLayout.onDetailHide();

        if (onDetailListener != null)
            onDetailListener.onChannelDetailHide();
    }

    private void setChannelBackgroundColor(int color) {

        for (int i = 0; i <= getCount(); i++) {

            View v = getChildAt(i);

            if (v instanceof ChannelLayout) {
                v.setBackgroundColor(color);
            }

        }

    }

    public boolean isDetailSliding() {
        return detailSliding || detailAnim;
    }

    public boolean isChannelLayoutSlided() {
        return channelLayout.Slided() != 0;
    }

    public boolean Slided() {

        if (detailSliding)
            return true;

        int start = getFirstVisiblePosition();

        for (int i = start; i <= getLastVisiblePosition(); i++) {

            View v = getChildAt(i - start);


            if (v instanceof ChannelLayout
                    && ((ChannelLayout) v).Slided() > 0) {
                return true;
            }

        }


        return false;
    }

    public void Refresh(Cursor newCursor, boolean full) {

        if (_newCursor != null) {
            _newCursor.close();
            _newCursor = null;
        }

        if (getAdapter() == null)
            return;


        if (full
                || (!isDetailSliding()
                && !Slided()
                && getFirstVisiblePosition() == 0)) {
            newCursor.moveToFirst();
            ((ListViewCursorAdapter) getAdapter()).changeCursor(newCursor);
            return;
        }

        if (newCursor == null || newCursor.isClosed())
            return;

        _newCursor = newCursor;
        int start = getFirstVisiblePosition();

        for (int i = start; i <= getLastVisiblePosition(); i++) {

            View v = getChildAt(i - start);
            Object obj = getItemAtPosition(i);

            if (v instanceof ChannelLayout
                    && obj instanceof Cursor) {


                ChannelBase old_obj;
                if (((ListViewCursorAdapter) getAdapter()).isGroup()) {
                    old_obj = new ChannelGroup();
                } else {
                    old_obj = new Channel();
                }

                old_obj.AssignCursorData((Cursor) obj);

                _newCursor.moveToFirst();

                do {
                    ChannelBase new_obj;
                    if (((ListViewCursorAdapter) getAdapter()).isGroup()) {
                        new_obj = new ChannelGroup();
                    } else {
                        new_obj = new Channel();
                    }
                    new_obj.AssignCursorData(_newCursor);

                    if (new_obj.getId() == old_obj.getId()) {
                        requestLayout_Locked = true;
                        ((ListViewCursorAdapter) getAdapter()).setData((ChannelLayout) v, new_obj, ((ListViewCursorAdapter) getAdapter()).getSectionAtPosition(i));
                        requestLayout_Locked = false;
                    }

                } while (_newCursor.moveToNext());
            }

        }


    }

    public void setOnDetailListener(OnDetailListener onDetailListener) {
        this.onDetailListener = onDetailListener;
    }

    public OnChannelButtonClickListener getOnChannelButtonClickListener() {
        return onChannelButtonClickListener;
    }

    public void setOnChannelButtonClickListener(OnChannelButtonClickListener
                                                        onChannelButtonClickListener) {
        this.onChannelButtonClickListener = onChannelButtonClickListener;
    }

    public OnChannelButtonTouchListener getOnChannelButtonTouchListener() {
        return onChannelButtonTouchListener;
    }

    public void setOnChannelButtonTouchListener(OnChannelButtonTouchListener
                                                        onChannelButtonTouchListener) {
        this.onChannelButtonTouchListener = onChannelButtonTouchListener;
    }

    public OnCaptionLongClickListener getOnCaptionLongClickListener() {
        return onCaptionLongClickListener;
    }

    public void setOnCaptionLongClickListener(OnCaptionLongClickListener onCaptionLongClickListener) {
        this.onCaptionLongClickListener = onCaptionLongClickListener;
    }

    public OnSectionLayoutTouchListener getOnSectionLayoutTouchListener() {
        return onSectionLayoutTouchListener;
    }

    public void setOnSectionLayoutTouchListener(OnSectionLayoutTouchListener onSectionLayoutTouchListener) {
        this.onSectionLayoutTouchListener = onSectionLayoutTouchListener;
    }

    public boolean isDetailVisible() {
        return mDetailVisible && mDetailLayout != null;
    }

    public void onBackPressed() {
        if (mDetailLayout.onBackPressed()) {
            hideDetail(true);
        }
    }

    public void hideDetail(boolean animated, boolean offlineReason) {
        if (isDetailVisible()
                && mDetailLayout.detailWillHide(offlineReason))

            if (animated) {
                AnimateDetailSliding(true);
            } else {

                setMargin(0);

                mDetailLayout.setVisibility(View.INVISIBLE);
                mDetailVisible = false;
                setVisibility(View.VISIBLE);

                onDetailHide();
            }
    }

    public void hideDetail(boolean animated) {
        hideDetail(animated, false);
    }

    public ChannelBase detail_getChannel() {

        if (isDetailVisible()) {
            return mDetailLayout.getChannelFromDatabase();
        }

        return null;
    }

    ;

    public int detail_getRemoteId() {

        if (isDetailVisible()) {
            return mDetailLayout.getRemoteId();
        }

        return 0;
    }

    public void detail_OnChannelDataChanged() {
        if (isDetailVisible())
            mDetailLayout.OnChannelDataChanged();
    }

    public interface OnChannelButtonClickListener {
        void onChannelStateButtonClick(ChannelListView clv, int remoteId);
        void onChannelWarningButtonClick(ChannelListView clv, int remoteId);
    }

    public interface OnChannelButtonTouchListener {
        void onChannelButtonTouch(ChannelListView clv, boolean left, boolean up, int remoteId, int channelFunc);
    }

    public interface OnCaptionLongClickListener {
        void onChannelCaptionLongClick(ChannelListView clv, int remoteId);
        void onLocationCaptionLongClick(ChannelListView clv, int locationId);
    }

    public interface OnDetailListener {
        void onChannelDetailShow(ChannelBase channel);
        void onChannelDetailHide();
    }

    public interface OnSectionLayoutTouchListener {
        void onSectionClick(ChannelListView clv, String caption, int locationId);
    }
}
