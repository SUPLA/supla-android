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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;
import org.supla.android.core.networking.suplaclient.SuplaClientState.Locked;
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder;

@SuppressLint("Registered")
public abstract class WizardActivity extends NavigationActivity {

  private Button mBtnNextRightPart;
  private Button mBtnNextMiddlePart;
  private Button mBtnNextLeftPart;
  private Timer mBtnNextPreloaderTimer;
  private short mBtnNextPreloaderPos;

  private RelativeLayout mContent;
  private final ArrayList<View> mPages = new ArrayList<>();

  @Inject SuplaClientStateHolder suplaClientStateHolder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_wizard);

    mBtnNextRightPart = findViewById(R.id.wizard_next_right_part);
    mBtnNextRightPart.setOnClickListener(this);

    mBtnNextMiddlePart = findViewById(R.id.wizard_next_middle_part);
    mBtnNextMiddlePart.setOnClickListener(this);
    mBtnNextMiddlePart.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

    mBtnNextLeftPart = findViewById(R.id.wizard_next_left_part);
    mBtnNextLeftPart.setOnClickListener(this);

    mContent = findViewById(R.id.wizard_content);
    setBtnNextEnabled(false);
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (suplaClientStateHolder.stateOrNull() == Locked.INSTANCE) {
      finish();
    }
  }

  protected View addStepPage(int layoutResId, int pageId) {
    View stepPage = Inflate(layoutResId, null);
    if (stepPage != null) {
      stepPage.setVisibility(View.GONE);
      stepPage.setTag(pageId);
      mContent.addView(stepPage);
      mPages.add(stepPage);

      if (stepPage instanceof RelativeLayout) {
        stepPage.setLayoutParams(
            new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
      }
    }
    return stepPage;
  }

  protected void showPage(int pageId) {
    for (View page : mPages) {
      Integer i = pageId;
      page.setVisibility(page.getTag() == i ? View.VISIBLE : View.GONE);
    }
  }

  protected int getVisiblePageId() {
    for (View page : mPages) {
      if (page.getVisibility() == View.VISIBLE) {
        return (Integer) page.getTag();
      }
    }

    return 0;
  }

  protected void setBtnNextEnabled(boolean enabled) {
    mBtnNextLeftPart.setEnabled(enabled);
    mBtnNextMiddlePart.setEnabled(enabled);
    mBtnNextRightPart.setEnabled(enabled);
    mBtnNextMiddlePart.setTextColor(
        getResources().getColor(enabled ? R.color.primary : R.color.disabled));

    setArrowVisible(enabled);
  }

  protected void setBtnNextText(int resId) {
    mBtnNextMiddlePart.setText(resId, TextView.BufferType.NORMAL);
  }

  protected boolean isBtnNextPreloaderVisible() {
    return mBtnNextPreloaderTimer != null;
  }

  protected void setBtnNextPreloaderVisible(boolean visible) {

    if (mBtnNextPreloaderTimer != null) {
      mBtnNextPreloaderPos = -1;
      mBtnNextPreloaderTimer.cancel();
      mBtnNextPreloaderTimer = null;
    }

    setArrowVisible(!visible);

    if (visible) {

      mBtnNextPreloaderPos = 0;

      mBtnNextPreloaderTimer = new Timer();
      mBtnNextPreloaderTimer.scheduleAtFixedRate(
          new TimerTask() {
            @Override
            public void run() {

              runOnUiThread(
                  new Runnable() {
                    public void run() {

                      if (mBtnNextPreloaderPos == -1) {
                        return;
                      }

                      String txt = "";

                      for (int a = 0; a < 10; a++) {
                        txt += mBtnNextPreloaderPos == a ? "|" : ".";
                      }

                      mBtnNextPreloaderPos++;
                      if (mBtnNextPreloaderPos > 9) {
                        mBtnNextPreloaderPos = 0;
                      }

                      mBtnNextMiddlePart.setText(txt, TextView.BufferType.NORMAL);
                    }
                  });
            }
          },
          0,
          100);
    }
  }

  private void setArrowVisible(boolean visible) {
    if (visible) {
      mBtnNextRightPart.setBackgroundResource(R.drawable.btnnextr);
      ViewGroup.LayoutParams params = mBtnNextRightPart.getLayoutParams();
      params.width = getResources().getDimensionPixelSize(R.dimen.wizard_btnnextr_width);
      mBtnNextRightPart.setLayoutParams(params);
    } else {
      mBtnNextRightPart.setBackgroundResource(R.drawable.btnnextr2);

      ViewGroup.LayoutParams params = mBtnNextRightPart.getLayoutParams();
      params.width = getResources().getDimensionPixelSize(R.dimen.wizard_btnnextl_width);

      mBtnNextRightPart.setLayoutParams(params);
    }
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);

    if (v == mBtnNextLeftPart || v == mBtnNextMiddlePart || v == mBtnNextRightPart) {
      onBtnNextClick();
    }
  }

  protected abstract void onBtnNextClick();
}
