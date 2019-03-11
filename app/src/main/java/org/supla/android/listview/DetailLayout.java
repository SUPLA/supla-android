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


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.supla.android.SuplaApp;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.DbHelper;
import org.supla.android.lib.SuplaClient;


public abstract class DetailLayout extends FrameLayout {

    private ChannelListView cLV;
    private ChannelBase channelBase;
    private View mContentView;
    private int mRemoteId;
    private boolean Group;
    protected DbHelper DBH;

    public DetailLayout(Context context, ChannelListView cLV) {
        super(context);
        this.cLV = cLV;
        init();
    }

    public DetailLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetailLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetailLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {

        mRemoteId = 0;
        channelBase = null;

        DBH = new DbHelper(getContext());
        mContentView = getContentView();

        if (mContentView != null) {

            if (mContentView.getBackground() instanceof ColorDrawable) {
                setBackgroundColor(((ColorDrawable) mContentView.getBackground().mutate()).getColor());
            }

            addView(mContentView);
        }

        setVisibility(View.INVISIBLE);

    }

    protected View inflateLayout(int id) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater == null ? null : inflater.inflate(id, null);
    }


    public abstract View getContentView();

    public abstract void OnChannelDataChanged();

    public void setData(ChannelBase cbase) {
        channelBase = cbase;
        mRemoteId = cbase == null ? 0 : cbase.getRemoteId();
        Group = mRemoteId != 0 && cbase instanceof ChannelGroup;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return cLV.onTouchEvent(ev);
    }

    public ChannelBase getChannelFromDatabase() {

        if (getRemoteId() != 0) {
            return isGroup() ? DBH.getChannelGroup(mRemoteId)
                    : DBH.getChannel(getRemoteId());
        }

        return null;
    }

    public int getMargin() {

        ViewGroup.LayoutParams lp = getLayoutParams();
        return ((MarginLayoutParams) lp).leftMargin;

    }

    public void setMargin(int margin) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        ((ViewGroup.MarginLayoutParams) lp).setMargins(margin, 0, -margin, 0);
        setLayoutParams(lp);

    }

    public int getRemoteId() {
        return mRemoteId;
    }

    public ChannelBase getChannelBase() {
        return channelBase;
    }

    public boolean isGroup() { return Group; }

    public boolean isDetailVisible() {
        return cLV.isDetailVisible();
    }

    public void onDetailShow() {};
    public void onDetailHide() {};

    public void deviceCalCfgRequest(int cmd, int dataType, byte[] data) {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || !isDetailVisible()) {
            return;
        }

        client.DeviceCalCfgRequest(getRemoteId(), cmd, dataType, data);
    }

    public void deviceCalCfgRequest(int cmd, Byte data) {
        byte[] arr = new byte[1];
        arr[0] = data == null ? 0 : data.byteValue();

        deviceCalCfgRequest(cmd, 0, data == null ? null : arr);
    }

    public void deviceCalCfgRequest(int cmd) {
        deviceCalCfgRequest(cmd, 0, null);
    }

    public void deviceCalCfgRequest(int cmd, Short data) {
        byte[] arr = new byte[2];
        if (data!=null) {
            arr[0] = (byte)(data.shortValue() & 0x00FF);
            arr[1] = (byte)((data.shortValue() & 0xFF00) >> 8);
        }

        deviceCalCfgRequest(cmd, 0, data == null ? null : arr);
    }
}
