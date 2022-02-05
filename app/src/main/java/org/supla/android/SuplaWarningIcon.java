package org.supla.android;

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

import android.app.AlertDialog;
import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;

public class SuplaWarningIcon extends AppCompatImageView implements View.OnClickListener {

    private Channel channel;
    private boolean initialized;

    private void init() {
        if (!initialized) {
            initialized = true;
            setVisibility(INVISIBLE);
            setOnClickListener(this);
        }
    }

    public SuplaWarningIcon(Context context) {
        super(context);
        init();
    }

    public SuplaWarningIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuplaWarningIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setChannel(ChannelBase channelBase) {
        channel = null;

        if (channelBase != null && channelBase instanceof Channel) {
            channel = (Channel) channelBase;
        }

        int warningIcon = 0;

        if (channel != null) {
            warningIcon = channel.getChannelWarningIcon();
        }

        if (warningIcon != 0) {
            setImageResource(warningIcon);
            setVisibility(VISIBLE);
        } else {
            setImageResource(0);
            setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view != this || channel == null) {
            return;
        }

        String warning = channel.getChannelWarningMessage(getContext());
        if (warning != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(warning);

            builder.setNeutralButton(R.string.ok, (dialog, id) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
