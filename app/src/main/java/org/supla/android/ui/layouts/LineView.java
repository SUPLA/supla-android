package org.supla.android.ui.layouts;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import org.supla.android.R;

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

public class LineView extends View {

  public LineView(Context context) {
    super(context);

    setBackgroundColor(getResources().getColor(R.color.separator));

    RelativeLayout.LayoutParams lp =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.channel_separator_height));

    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

    setLayoutParams(lp);
  }
}
