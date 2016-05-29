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

 Author: Przemyslaw Zygmunt przemek@supla.org
 */


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;


public abstract class DetailLayout extends FrameLayout {

    private ChannelListView cLV;
    private View mContentView;
    private int mChannelId;
    private DbHelper DBH;

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

        mChannelId = 0;
        DBH = new DbHelper(getContext());
        mContentView = getContentView();

        if ( mContentView != null ) {
            addView(mContentView);
        }

        setVisibility(View.INVISIBLE);

    }

    protected View inflateLayout(int id) {
        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(id, null);
    }


    public abstract View getContentView();
    public abstract void OnChannelDataChanged();
    public void setData(Channel channel) {
        mChannelId = channel.getChannelId();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return cLV.onTouchEvent(ev);
    }

    public Channel getChannelFromDatabase() {

        if ( getChannelId() != 0 )
           return DBH.getChannel(DBH.getCurrentAccessId(), mChannelId, false);

        return null;
    }

    public int getMargin() {

        ViewGroup.LayoutParams lp = getLayoutParams();
        return ((MarginLayoutParams)lp).leftMargin;

    }

    public void setMargin(int margin) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        ((ViewGroup.MarginLayoutParams)lp).setMargins(margin,0,-margin,0);
        setLayoutParams(lp);

    }

    public int getChannelId() {
        return mChannelId;
    }

    public boolean isDetailVisible() {
        return cLV.isDetailVisible();
    }





}
